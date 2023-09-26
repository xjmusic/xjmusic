// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import io.xj.hub.util.StringUtils;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.telemetry.TelemetryMeasureGauge;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.ship.ShipException;
import io.xj.nexus.ship.ShipMode;
import jakarta.annotation.Nullable;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

/**
 Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
@Service
public class PlaylistPublisherImpl implements PlaylistPublisher {
  static final Logger LOG = LoggerFactory.getLogger(PlaylistPublisherImpl.class);
  final AtomicLong maxSequenceNumber = new AtomicLong(0);

  final AtomicLong toChainMicros = new AtomicLong(0);
  final ChunkFactory chunkFactory;
  final DecimalFormat df;
  final FileStoreProvider fileStore;
  final HttpClientProvider httpClientProvider;
  final Map<Long/* mediaSequence */, Chunk> items = new ConcurrentHashMap<>();
  final TelemetryMeasureGauge HLS_PLAYLIST_AHEAD_SECONDS;
  final TelemetryMeasureGauge HLS_PLAYLIST_SIZE;
  final Pattern rgxFilename = Pattern.compile("^([A-Za-z0-9_]*)-([0-9]*)\\.([A-Za-z0-9]*)");
  final Pattern rgxSecondsValue = Pattern.compile("#EXTINF:([0-9.]*)");
  final Predicate<? super String> isSegmentFilename;
  final String bucket;
  final String m3u8ContentType;
  final String m3u8Key;
  final MediaSeqNumProvider mediaSeqNumProvider;
  final TelemetryProvider telemetryProvider;
  final AtomicBoolean running = new AtomicBoolean(true);
  final int chunkDurationSeconds;
  final int m3u8MaxAgeSeconds;
  final int m3u8ServerControlHoldBackSeconds;

  @Nullable
  final String m3u8KeyAlias;

  @Autowired
  public PlaylistPublisherImpl(
    ChunkFactory chunkFactory,
    FileStoreProvider fileStoreProvider,
    HttpClientProvider httpClientProvider,
    MediaSeqNumProvider mediaSeqNumProvider,
    TelemetryProvider telemetryProvider,
    @Value("${ship.mode}") String shipMode,
    @Value("${stream.bucket}") String streamBucket,
    @Value("${ship.chunk.duration.seconds}") int chunkDurationSeconds,
    @Value("${ship.m3u8.content.type}") String shipM3u8ContentType,
    @Value("${ship.m3u8.max.age.seconds}") int shipM3u8MaxAgeSeconds,
    @Value("${ship.m3u8.server.control.hold.back.extra.seconds}") int shipM3u8ServerControlHoldBackExtraSeconds,
    @Value("${ship.chunk.audio.encoder}") String shipChunkAudioEncoder,
    @Value("${input.template.key}") String shipKey,
    @Value("${input.template.key.alias}") String shipKeyAlias
  ) {
    this.chunkFactory = chunkFactory;
    this.fileStore = fileStoreProvider;
    this.httpClientProvider = httpClientProvider;
    this.mediaSeqNumProvider = mediaSeqNumProvider;
    this.telemetryProvider = telemetryProvider;

    // Environment
    this.running.set(ShipMode.HLS.equals(shipMode));
    this.bucket = streamBucket;
    this.chunkDurationSeconds = chunkDurationSeconds;
    this.m3u8ContentType = shipM3u8ContentType;
    this.m3u8MaxAgeSeconds = shipM3u8MaxAgeSeconds;
    this.m3u8ServerControlHoldBackSeconds = 3 * this.chunkDurationSeconds + shipM3u8ServerControlHoldBackExtraSeconds;

    // Computed
    this.isSegmentFilename = m -> m.endsWith(String.format(".%s", shipChunkAudioEncoder));
    this.m3u8Key = computeM3u8Key(shipKey);
    this.m3u8KeyAlias = StringUtils.isNullOrEmpty(shipKeyAlias) ? String.valueOf(Optional.empty()) : Optional.of(shipKeyAlias).map(this::computeM3u8Key).orElse(null);

    // Decimal format for writing seconds values in .m3u8 playlist line items
    df = new DecimalFormat("#.######");
    df.setRoundingMode(RoundingMode.FLOOR);
    df.setMinimumFractionDigits(6);
    df.setMaximumFractionDigits(6);

    // Telemetry
    HLS_PLAYLIST_SIZE = telemetryProvider.gauge("hls_playlist_size", "HLS Playlist Size", "");
    HLS_PLAYLIST_AHEAD_SECONDS = telemetryProvider.gauge("hls_playlist_ahead_seconds", "HLS Playlist Ahead Seconds", "s");
  }

  @Override
  public Optional<Long> rehydrate(String streamBaseUrl, long initialSeqNum) {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", streamBaseUrl, m3u8Key)))
    ) {
      if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode())) {
        LOG.warn("Failed to get previously-shipped playlist {} because {} {}", m3u8Key, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        return Optional.empty();
      }

      LOG.debug("will check for last shipped playlist");
      var chunks = parseItems(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
      var maxSeqNum = chunks.stream().map(Chunk::getSequenceNumber).max(Long::compare).orElseThrow(() -> new ShipException("No chunks in playlist"));
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
      LOG.warn("Failed to retrieve previously streamed playlist {}/{} because {}", bucket, m3u8Key, e.getMessage());
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
    if (running.get())
      try {
        var mediaSequence = mediaSeqNumProvider.computeMediaSeqNum(System.currentTimeMillis());
        collectGarbage(mediaSequence);
        fileStore.putS3ObjectFromString(getPlaylistContent(mediaSequence), bucket, m3u8Key, m3u8ContentType, m3u8MaxAgeSeconds);
        LOG.debug("Shipped {}/{} ({}) @ {}", bucket, m3u8Key, m3u8ContentType, mediaSequence);
        if (Objects.nonNull(m3u8KeyAlias)) {
          fileStore.putS3ObjectFromString(getPlaylistContent(mediaSequence), bucket, m3u8KeyAlias, m3u8ContentType, m3u8MaxAgeSeconds);
          LOG.debug("Shipped alias {}/{} ({}) @ {}", bucket, m3u8KeyAlias, m3u8ContentType, mediaSequence);
        }

      } catch (FileStoreException e) {
        throw new ShipException("Failed to publish playlist!", e);
      }
  }

  @Override
  public void collectGarbage(long mediaSequence) {
    List<Long> toRemove = items.keySet().stream().filter(ms -> ms < mediaSequence).toList();
    for (long ms : toRemove) items.remove(ms);
    recomputeMaxSequenceNumber();
  }

  @Override
  public String getPlaylistContent(long mediaSequence) {
    var start = items.keySet().stream().min(Long::compare).orElse(mediaSequence);
    // build m3u8 header followed by the playlist item lines
    List<String> m3u8FinalLines = Stream.concat(
      Stream.of(
        "#EXTM3U",
        "#EXT-X-VERSION:4",
        String.format("#EXT-X-TARGETDURATION:%s", chunkDurationSeconds),
        String.format("#EXT-X-MEDIA-SEQUENCE:%d", start),
        String.format("#EXT-X-SERVER-CONTROL:HOLD-BACK=%d.0", m3u8ServerControlHoldBackSeconds)
      ),
      // FUTURE: media sequence numbers need to be a continuous unbroken sequence of integers
      items.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue)
        .flatMap(chunk -> Stream.of(
          String.format("#EXTINF:%s,", df.format(chunk.getDurationSeconds())),
          chunk.getFilename()
        ))
    ).toList();

    // publish custom .m3u8 file
    return StringUtils.formatMultiline(m3u8FinalLines.toArray());
  }

  @Override
  public List<Chunk> parseItems(String m3u8Content) throws ShipException {
    List<Chunk> chunks = new ArrayList<>();
    Chunk item;
    String ext;
    String filename;
    String shipKey;
    double actualDurationSeconds;
    long seqNum;
    long fromChainMicros;

    // read playlist file, then grab only the line pairs that are segment files
    String[] m3u8Lines = StringUtils.splitLines(m3u8Content);
    for (int i = 1; i < m3u8Lines.length; i++)
      if (isSegmentFilename.test(m3u8Lines[i])) {
        filename = m3u8Lines[i];

        var mS = rgxSecondsValue.matcher(m3u8Lines[i - 1]);
        if (!mS.find()) continue;
        actualDurationSeconds = Double.parseDouble(mS.group(1));

        var mF = rgxFilename.matcher(filename);
        if (!mF.find()) continue;
        shipKey = mF.group(1);
        seqNum = Integer.parseInt(mF.group(2));
        ext = mF.group(3);

        // this is an estimate
        fromChainMicros = mediaSeqNumProvider.computeChainMicros(seqNum);

        item = chunkFactory.build(shipKey, seqNum, fromChainMicros, actualDurationSeconds, ext);

        chunks.add(item);
      }

    return chunks;
  }

  @Override
  public boolean isHealthy() {
    var threshold = mediaSeqNumProvider.computeMediaSeqNum(toChainMicros.get());
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
    telemetryProvider.put(HLS_PLAYLIST_AHEAD_SECONDS, (double) (getMaxToChainMicros() - toChainMicros.get()) / MICROS_PER_SECOND);
  }

  @Override
  public Optional<Long> start(String streamBaseUrl, long initialSeqNum) {
    if (!running.get())
      return Optional.empty();

    return rehydrate(streamBaseUrl, initialSeqNum);
  }

  @Override
  public void setAtChainMicros(long atChainMicros) {
    toChainMicros.set(atChainMicros);
  }

  @Override
  public Long getMaxToChainMicros() {
    return items.values().stream().max(Chunk::compare).map(Chunk::getToChainMicros).orElse(0L);
  }

  /**
   Recompute the max sequence number given current items
   */
  void recomputeMaxSequenceNumber() {
    maxSequenceNumber.set(items.keySet().stream().max(Long::compare).orElse(0L));
  }

  /**
   Compute an M3U8 file key

   @param key for which to compute file key
   @return file key
   */
  String computeM3u8Key(String key) {
    return String.format("%s.m3u8", key);
  }
}
