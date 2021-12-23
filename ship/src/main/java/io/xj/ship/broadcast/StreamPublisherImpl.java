// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.Files;
import io.xj.lib.util.Text;
import io.xj.ship.ShipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 <p>
 Ship #EXT-X-INDEPENDENT-SEGMENTS #180669689
 */
public class StreamPublisherImpl implements StreamPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(StreamPublisherImpl.class);
  private final FileStoreProvider fileStore;
  private final Pattern rgxFilenameSegSeqNum;
  private final Predicate<? super String> isSegmentFilename;
  private final String bucket;
  private final String contentTypeM3U8;
  private final String contentTypeSegment;
  private final String m3u8Key;
  private final String playlistPath;
  private final String shipSegmentFilenameEndsWith;
  private final String tempFilePathPrefix;
  private final int hlsListSize;
  private final int hlsSegmentSeconds;

  @Inject
  public StreamPublisherImpl(
    @Assisted("shipKey") String shipKey,
    Environment env,
    FileStoreProvider fileStore) {
    this.fileStore = fileStore;

    contentTypeM3U8 = env.getShipM3u8ContentType();
    contentTypeSegment = env.getShipSegmentContentType();
    tempFilePathPrefix = env.getTempFilePathPrefix();
    hlsListSize = env.getHlsListSize();
    hlsSegmentSeconds = env.getHlsSegmentSeconds();
    m3u8Key = String.format("%s.m3u8", shipKey);
    playlistPath = String.format("%s%s", tempFilePathPrefix, m3u8Key);
    shipSegmentFilenameEndsWith = String.format(".%s", env.getShipFfmpegSegmentFilenameExtension());
    rgxFilenameSegSeqNum = Pattern.compile(String.format("-([0-9]*)\\.%s", env.getShipFfmpegSegmentFilenameExtension()));
    isSegmentFilename = m -> m.endsWith(shipSegmentFilenameEndsWith);
    bucket = env.getStreamBucket();
  }

  @Override
  public void publish(long nowMillis) {
    try {
      // test for existence of playlist file; skip if nonexistent
      if (!new File(playlistPath).exists()) return;

      // read playlist file, then grab only the line pairs that are segment files
      String[] m3u8Lines = Text.splitLines(Files.getFileContent(playlistPath));
      List<String> m3u8FileLines = Lists.newArrayList();
      for (int i = 1; i < m3u8Lines.length; i++)
        if (isSegmentFilename.test(m3u8Lines[i])) {
          m3u8FileLines.add(m3u8Lines[i - 1]);
          m3u8FileLines.add(m3u8Lines[i]);
        }

      // only publish the last N entries
      var m3u8LmtLines = m3u8FileLines
        .subList(Math.max(0, m3u8FileLines.size() - 2 * hlsListSize), m3u8FileLines.size());

      // scan for filenames and publish them
      for (String fn : m3u8LmtLines.stream().filter(isSegmentFilename).toList())
        stream(fn, contentTypeSegment);

      // determine the media sequence from the first filename
      var firstKey = m3u8LmtLines.stream()
        .filter(isSegmentFilename)
        .findFirst();
      if (firstKey.isEmpty()) return;
      var matcher = rgxFilenameSegSeqNum.matcher(firstKey.get());
      if (!matcher.find())
        throw new ShipException(String.format("Failed to match a media sequence number in filename: %s", firstKey));
      var mediaSequence = Integer.valueOf(matcher.group(1));

      // build m3u8 header followed by limited lines
      List<String> m3u8FinalLines = Stream.concat(Stream.of(
        "#EXTM3U",
        "#EXT-X-VERSION:3",
        String.format("#EXT-X-TARGETDURATION:%s", hlsSegmentSeconds),
        "#EXT-X-DISCONTINUITY",
        String.format("#EXT-X-MEDIA-SEQUENCE:%d", mediaSequence),
        "#EXT-X-PLAYLIST-TYPE:EVENT",
        "#EXT-X-INDEPENDENT-SEGMENTS"
      ), m3u8LmtLines.stream()).toList();

      // publish custom .m3u8 file
      var m3u8Content = String.join("\n", m3u8FinalLines) + "\n";
      fileStore.putS3ObjectFromString(m3u8Content, bucket, m3u8Key, contentTypeM3U8);
      LOG.info("Did stream {}/{} ({}) @ media sequence {}", bucket, m3u8Key, contentTypeM3U8, mediaSequence);

    } catch (IOException | ShipException | FileStoreException e) {
      LOG.error("Failed during stream publication!", e);
    }
  }

  /**
   Stream a file from temp path to S3

   @param key         of file (in temp folder and on S3 target)
   @param contentType content-type
   @throws FileStoreException on failure
   */
  private void stream(String key, String contentType) throws FileStoreException {
    if (fileStore.doesS3ObjectExist(bucket, key)) return;
    var path = String.format("%s%s", tempFilePathPrefix, key);
    fileStore.putS3ObjectFromTempFile(path, bucket, key, contentType);
    LOG.info("Did stream {}/{} ({})", bucket, key, contentType);
  }
}
