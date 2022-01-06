// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.opencensus.stats.Measure;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.ship.ShipException;
import io.xj.ship.ShipMode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.xj.lib.util.Values.MILLIS_PER_SECOND;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
@Singleton
public class PlaylistPublisherImpl implements PlaylistPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(PlaylistPublisherImpl.class);
  private final AtomicLong maxSequenceNumber = new AtomicLong(0);
  private final BroadcastFactory broadcast;
  private final DecimalFormat df;
  private final FileStoreProvider fileStore;
  private final HttpClientProvider httpClientProvider;
  private final Map<Long/* mediaSequence */, Chunk> items = Maps.newConcurrentMap();
  private final Measure.MeasureDouble HLS_PLAYLIST_SIZE;
  private final Pattern rgxFilename = Pattern.compile("^([A-Za-z0-9_]*)-([0-9]*)\\.([A-Za-z0-9]*)");
  private final Pattern rgxSecondsValue = Pattern.compile("#EXTINF:([0-9.]*)");
  private final Predicate<? super String> isSegmentFilename;
  private final String bucket;
  private final String m3u8ContentType;
  private final String m3u8Key;
  private final String streamBaseUrl;
  private final TelemetryProvider telemetryProvider;
  private final boolean active;
  private final int chunkTargetDuration;
  private final int initialMediaSeqNumOffset;
  private final int m3u8MaxAgeSeconds;
  private final int playlistBackSeconds;
  private final int m3u8ServerControlHoldBackSeconds;

  @Inject
  public PlaylistPublisherImpl(
    BroadcastFactory broadcast,
    Environment env,
    FileStoreProvider fileStore,
    HttpClientProvider httpClientProvider,
    TelemetryProvider telemetryProvider
  ) {
    this.broadcast = broadcast;
    this.fileStore = fileStore;
    this.httpClientProvider = httpClientProvider;
    this.telemetryProvider = telemetryProvider;

    // Environment
    active = ShipMode.HLS.equals(env.getShipMode());
    bucket = env.getStreamBucket();
    chunkTargetDuration = env.getShipChunkTargetDuration();
    m3u8ContentType = env.getShipM3u8ContentType();
    m3u8MaxAgeSeconds = env.getShipM3u8MaxAgeSeconds();
    m3u8ServerControlHoldBackSeconds = 3 * chunkTargetDuration + env.getShipM3u8ServerControlHoldBackExtraSeconds();
    playlistBackSeconds = env.getShipPlaylistBackSeconds();
    streamBaseUrl = env.getStreamBaseUrl();

    // Computed
    initialMediaSeqNumOffset = env.getShipInitialMediaSequenceNumberOffset();
    isSegmentFilename = m -> m.endsWith(String.format(".%s", env.getShipChunkAudioEncoder()));
    m3u8Key = String.format("%s.m3u8", env.getShipKey());

    // Decimal format for writing seconds values in .m3u8 playlist line items
    df = new DecimalFormat("#.######");
    df.setRoundingMode(RoundingMode.FLOOR);
    df.setMinimumFractionDigits(6);
    df.setMaximumFractionDigits(6);

    // Telemetry
    HLS_PLAYLIST_SIZE = telemetryProvider.gauge("hls_playlist_size", "HLS Playlist Size", "");
  }

  @Override
  public Optional<Long> rehydrate(long initialSeqNum) {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", streamBaseUrl, m3u8Key)))
    ) {
      if (!Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode())) {
        LOG.error("Failed to get previously playlist {} because {} {}", m3u8Key, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        return Optional.empty();
      }

      LOG.debug("will check for last shipped playlist");
      var chunks = parseItems(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
      var maxSeqNum = chunks.stream().map(Chunk::getSequenceNumber).max(Long::compare).orElseThrow();
      if (initialSeqNum > maxSeqNum) {
        LOG.warn("Will not rehydrate from stale playlist with last sequence number {} (< {})", maxSeqNum, initialSeqNum);
        return Optional.empty();
      }
      for (var item : chunks)
        if (putNext(item))
          LOG.info("Did rehydrate {}/{} @ media sequence {}", bucket, item.getFilename(), item.getSequenceNumber());
        else
          LOG.warn("Skipped {}/{} @ media sequence {}", bucket, item.getFilename(), item.getSequenceNumber());

      Long actualMaxSeqNum = items.keySet().stream().max(Long::compare).orElseThrow();
      LOG.info("Rehydrated {} items OK to media sequence {} from playlist {}/{}", items.size(), actualMaxSeqNum, bucket, m3u8Key);
      return Optional.of(actualMaxSeqNum);

    } catch (ClassCastException | IOException | ShipException e) {
      LOG.error("Failed to retrieve previously streamed playlist {}/{} because {}", bucket, m3u8Key, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<Chunk> get(long mediaSequence) {
    if (items.containsKey(mediaSequence)) return Optional.of(items.get(mediaSequence));
    return Optional.empty();
  }

  @Override
  public boolean putNext(Chunk chunk) {
    if (0 < maxSequenceNumber.get() && chunk.getSequenceNumber() != maxSequenceNumber.get() + 1) return false;
    items.put(chunk.getSequenceNumber(), chunk);
    recomputeMaxSequenceNumber();
    return true;
  }

  @Override
  public void publish() throws ShipException {
    if (active)
      try {
        var mediaSequence = computeMediaSeqNum(System.currentTimeMillis());
        collectGarbage(mediaSequence);
        fileStore.putS3ObjectFromString(getPlaylistContent(mediaSequence), bucket, m3u8Key, m3u8ContentType, m3u8MaxAgeSeconds);
        LOG.debug("Shipped {}/{} ({}) @ {}", bucket, m3u8Key, m3u8ContentType, mediaSequence);

      } catch (FileStoreException e) {
        throw new ShipException("Failed ot publish playlist!", e);
      }
  }

  @Override
  public void collectGarbage(long mediaSequence) {
    long floor = mediaSequence - playlistBackSeconds / chunkTargetDuration;
    List<Long> toRemove = items.keySet().stream().filter(ms -> ms < floor).toList();
    for (long ms : toRemove) items.remove(ms);
    recomputeMaxSequenceNumber();
  }

  @Override
  public int computeMediaSeqNum(long epochMillis) {
    return (int) (Math.floor((double) epochMillis / (MILLIS_PER_SECOND * chunkTargetDuration)));
  }

  @Override
  public int computeInitialMediaSeqNum(long epochMillis) {
    return computeMediaSeqNum(System.currentTimeMillis()) - initialMediaSeqNumOffset;
  }

  @Override
  public String getPlaylistContent(long mediaSequence) {
    var start = items.keySet().stream().min(Long::compare).orElse(mediaSequence);
    // build m3u8 header followed by the playlist item lines
    List<String> m3u8FinalLines = Stream.concat(
      Stream.of(
        "#EXTM3U",
        "#EXT-X-VERSION:7",
        String.format("#EXT-X-TARGETDURATION:%s", chunkTargetDuration),
        String.format("#EXT-X-MEDIA-SEQUENCE:%d", start),
        String.format("#EXT-X-SERVER-CONTROL:HOLD-BACK=%d.0", m3u8ServerControlHoldBackSeconds),
        "#EXT-X-PLAYLIST-TYPE:EVENT"
      ),
      // FUTURE: media sequence numbers need to be a continuous unbroken sequence of integers
      items.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue)
        .flatMap(chunk -> Stream.of(
          String.format("#EXTINF:%s,", df.format(chunk.getActualDuration())),
          chunk.getFilename()
        ))
    ).toList();

    // publish custom .m3u8 file
    return String.join("\n", m3u8FinalLines) + "\n";
  }

  @Override
  public List<Chunk> parseItems(String m3u8Content) throws ShipException {
    List<Chunk> chunks = Lists.newArrayList();
    Chunk item;
    String ext;
    String filename;
    String shipKey;
    double actualDuration;
    long seqNum;

    // read playlist file, then grab only the line pairs that are segment files
    String[] m3u8Lines = Text.splitLines(m3u8Content);
    for (int i = 1; i < m3u8Lines.length; i++)
      if (isSegmentFilename.test(m3u8Lines[i])) {
        filename = m3u8Lines[i];

        var mS = rgxSecondsValue.matcher(m3u8Lines[i - 1]);
        if (!mS.find()) continue;
        actualDuration = Double.parseDouble(mS.group(1));

        var mF = rgxFilename.matcher(filename);
        if (!mF.find()) continue;
        shipKey = mF.group(1);
        seqNum = Integer.parseInt(mF.group(2));
        ext = mF.group(3);

        item = broadcast.chunk(shipKey, seqNum, ext, actualDuration);

        chunks.add(item);
      }

    return chunks;
  }

  @Override
  public boolean isHealthy() {
    var threshold = computeInitialMediaSeqNum(System.currentTimeMillis());
    if (maxSequenceNumber.get() >= threshold) return true;
    LOG.warn("Max sequence number {} below threshold {}", maxSequenceNumber, threshold);
    return false;
  }

  @Override
  public long getMaxSequenceNumber() {
    return maxSequenceNumber.get();
  }

  @Override
  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public void sendTelemetry() {
    telemetryProvider.put(HLS_PLAYLIST_SIZE, (double) items.size());
  }

  private void recomputeMaxSequenceNumber() {
    maxSequenceNumber.set(items.keySet().stream().max(Long::compare).orElse(0L));
  }
}
