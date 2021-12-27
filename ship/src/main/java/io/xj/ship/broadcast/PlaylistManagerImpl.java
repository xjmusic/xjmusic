// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Text;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
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
public class PlaylistManagerImpl implements PlaylistManager {
  private final Map<Long/* mediaSequence */, Chunk> items = Maps.newConcurrentMap();
  private final BroadcastFactory broadcast;
  private final int hlsSegmentSeconds;
  private final String shipSegmentFilenameEndsWith;
  private final Predicate<? super String> isSegmentFilename;
  private final Pattern rgxFilename = Pattern.compile("([A-Za-z0-9]*)-([0-9]*)\\.([A-Za-z0-9]*)");
  private final Pattern rgxSecondsValue = Pattern.compile("#EXTINF:([0-9.]*)");
  private final DecimalFormat df;
  private final AtomicLong maxSequenceNumber = new AtomicLong(0);

  @Inject
  public PlaylistManagerImpl(
    BroadcastFactory broadcast,
    Environment env
  ) {
    this.broadcast = broadcast;
    hlsSegmentSeconds = env.getHlsSegmentSeconds();
    shipSegmentFilenameEndsWith = String.format(".%s", env.getShipChunkAudioEncoder());
    isSegmentFilename = m -> m.endsWith(shipSegmentFilenameEndsWith);

    // Decimal format for writing seconds values in .m3u8 playlist line items
    df = new DecimalFormat("#.######");
    df.setRoundingMode(RoundingMode.FLOOR);
    df.setMinimumFractionDigits(6);
    df.setMaximumFractionDigits(6);
  }

  @Override
  public Optional<Chunk> get(long mediaSequence) {
    if (items.containsKey(mediaSequence)) return Optional.of(items.get(mediaSequence));
    return Optional.empty();
  }

  @Override
  public boolean putNext(Chunk item) {
    if (0 < maxSequenceNumber.get() && item.getSequenceNumber() != maxSequenceNumber.get() + 1) return false;
    items.put(item.getSequenceNumber(), item);
    maxSequenceNumber.set(items.keySet().stream().max(Long::compare).get());
    return true;
  }

  @Override
  public void collectGarbageBefore(long mediaSequence) {
    List<Long> toRemove = items.keySet().stream().filter(ms -> ms < mediaSequence).toList();
    for (long ms : toRemove) items.remove(ms);
  }

  @Override
  public int computeMediaSequence(long epochMillis) {
    return (int) (Math.floor((double) epochMillis / (MILLIS_PER_SECOND * hlsSegmentSeconds)));
  }

  @Override
  public String getPlaylistContent(long mediaSequence) {
    // always collect garbage before exporting the playlist content
    collectGarbageBefore(mediaSequence);

    // build m3u8 header followed by the playlist item lines
    List<String> m3u8FinalLines = Stream.concat(
      Stream.of(
        "#EXTM3U",
        "#EXT-X-VERSION:3",
        String.format("#EXT-X-TARGETDURATION:%s", hlsSegmentSeconds),
        "#EXT-X-DISCONTINUITY",
        String.format("#EXT-X-MEDIA-SEQUENCE:%d", mediaSequence),
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
  public List<Chunk> parseAndLoadItems(String m3u8Content) {
    List<Chunk> added = Lists.newArrayList();
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

        if (putNext(item)) added.add(item);
      }

    return added;
  }
}

