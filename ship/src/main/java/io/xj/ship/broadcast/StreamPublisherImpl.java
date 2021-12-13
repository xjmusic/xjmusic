// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

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
 */
public class StreamPublisherImpl implements StreamPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(StreamPublisherImpl.class);
  private static final String TRANSPORT_STREAM_FILENAME_ENDS_WITH = ".ts";
  private static final Pattern rgxTsFilenameSeqNum = Pattern.compile("-([0-9]*)\\.ts");
  private final FileStoreProvider fileStore;
  private final Predicate<? super String> isTransportStreamFilename = m -> m.endsWith(TRANSPORT_STREAM_FILENAME_ENDS_WITH);
  private final String bucket;
  private final String contentTypeM3U8;
  private final String contentTypeTS;
  private final String m3u8Key;
  private final String playlistPath;
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
    contentTypeTS = env.getShipTsContentType();
    tempFilePathPrefix = env.getTempFilePathPrefix();
    hlsListSize = env.getHlsListSize();
    hlsSegmentSeconds = env.getHlsSegmentSeconds();
    m3u8Key = String.format("%s.m3u8", shipKey);
    playlistPath = String.format("%s%s", tempFilePathPrefix, m3u8Key);
    bucket = env.getStreamBucket();
  }

  @Override
  public void publish(long nowMillis) {
    try {
      // test for existence of playlist file; skip if nonexistent
      if (!new File(playlistPath).exists()) return;

      // read playlist file, but only publish the last N entries
      var m3u8Lines = Text.splitLines(Files.getFileContent(playlistPath));
      var m3u8LmtLines = List.of(m3u8Lines)
        .subList(Math.max(0, m3u8Lines.length - 2 * hlsListSize), m3u8Lines.length);

      // scan for .ts filenames and publish them
      for (String ts : m3u8LmtLines.stream().filter(isTransportStreamFilename).toList())
        stream(ts, contentTypeTS);

      // determine the media sequence from the first TS key
      var firstTsKey = m3u8LmtLines.stream()
        .filter(isTransportStreamFilename)
        .findFirst()
        .orElseThrow(() -> new ShipException("Unable to get first TS key from limited playlist filenames!"));
      var matcher = rgxTsFilenameSeqNum.matcher(firstTsKey);
      if (!matcher.find())
        throw new ShipException(String.format("Failed to match a media sequence number in filename: %s", firstTsKey));
      var mediaSequence = Integer.valueOf(matcher.group(1));

      // build m3u8 header followed by limited lines
      List<String> m3u8FinalLines = Stream.concat(Stream.of(
        "#EXTM3U",
        "#EXT-X-VERSION:3",
        String.format("#EXT-X-TARGETDURATION:%s", hlsSegmentSeconds),
        String.format("#EXT-X-MEDIA-SEQUENCE:%d", mediaSequence),
        "#EXT-X-PLAYLIST-TYPE:EVENT"
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
