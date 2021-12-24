// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.xj.lib.telemetry.MultiStopwatch.MILLIS_PER_SECOND;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
@Singleton
public class M3U8PlaylistManagerImpl implements M3U8PlaylistManager {
  private final Map<Long/* mediaSequence */, M3U8PlaylistItem> items = Maps.newConcurrentMap();
  private final int hlsSegmentSeconds;
  private final String shipSegmentFilenameEndsWith;
  private final Predicate<? super String> isSegmentFilename;
  private final Pattern rgxFilenameSegSeqNum;
  private final Pattern rgxSecondsValue = Pattern.compile("#EXTINF:([0-9.]*)");

  @Inject
  public M3U8PlaylistManagerImpl(
    Environment env
  ) {
    hlsSegmentSeconds = env.getHlsSegmentSeconds();
    shipSegmentFilenameEndsWith = String.format(".%s", env.getShipFfmpegSegmentFilenameExtension());
    isSegmentFilename = m -> m.endsWith(shipSegmentFilenameEndsWith);
    rgxFilenameSegSeqNum = Pattern.compile(String.format("-([0-9]*)\\.%s", env.getShipFfmpegSegmentFilenameExtension()));
  }

  @Override
  public Optional<M3U8PlaylistItem> get(long mediaSequence) {
    if (items.containsKey(mediaSequence)) return Optional.of(items.get(mediaSequence));
    return Optional.empty();
  }

  @Override
  public boolean put(M3U8PlaylistItem item) {
    if (items.containsKey(item.getMediaSequence())) return false;
    items.put(item.getMediaSequence(), item);
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
        "#EXT-X-PLAYLIST-TYPE:EVENT",
        "#EXT-X-INDEPENDENT-SEGMENTS"
      ),
      items.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .flatMap(e -> e.getValue().toLines().stream())
    ).toList();

    // publish custom .m3u8 file
    return String.join("\n", m3u8FinalLines) + "\n";
  }

  @Override
  public List<M3U8PlaylistItem> parseAndLoadItems(String m3u8Content) {
    List<M3U8PlaylistItem> added = Lists.newArrayList();
    String filename;
    float seconds;
    long mediaSequence;
    M3U8PlaylistItem item;

    // read playlist file, then grab only the line pairs that are segment files
    String[] m3u8Lines = Text.splitLines(m3u8Content);
    for (int i = 1; i < m3u8Lines.length; i++)
      if (isSegmentFilename.test(m3u8Lines[i])) {
        filename = m3u8Lines[i];

        var mS = rgxSecondsValue.matcher(m3u8Lines[i - 1]);
        if (!mS.find()) continue;
        seconds = Float.parseFloat(mS.group(1));

        var mF = rgxFilenameSegSeqNum.matcher(filename);
        if (!mF.find()) continue;
        mediaSequence = Integer.parseInt(mF.group(1));

        item = new M3U8PlaylistItem(mediaSequence, seconds, filename);
        if (put(item)) added.add(item);
      }

    return added;
  }
}

