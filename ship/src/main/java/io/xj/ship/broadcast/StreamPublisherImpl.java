// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.Files;
import io.xj.lib.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class StreamPublisherImpl implements StreamPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(StreamPublisherImpl.class);
  private static final String TRANSPORT_STREAM_FILENAME_ENDS_WITH = ".ts";
  private final FileStoreProvider fileStore;
  private final String contentTypeM3U8;
  private final String contentTypeTS;
  private final String m3u8Key;
  private final String playlistPath;
  private final String bucket;
  private final String tempFilePathPrefix;

  @Inject
  public StreamPublisherImpl(
    @Assisted("shipKey") String shipKey,
    Environment env,
    FileStoreProvider fileStore) {
    this.fileStore = fileStore;

    contentTypeM3U8 = env.getShipM3u8ContentType();
    contentTypeTS = env.getShipTsContentType();
    tempFilePathPrefix = env.getTempFilePathPrefix();
    m3u8Key = String.format("%s.m3u8", shipKey);
    playlistPath = String.format("%s%s", tempFilePathPrefix, m3u8Key);
    bucket = env.getStreamBucket();
  }

  @Override
  public void publish(long nowMillis) {
    try {
      // test for existence of playlist file; skip if nonexistent
      if (!new File(playlistPath).exists()) return;

      // read playlist file and scan for .ts filenames
      var m3u8Lines = Text.splitLines(Files.getFileContent(playlistPath));
      var tsKeys = Arrays.stream(m3u8Lines)
        .filter(m -> m.endsWith(TRANSPORT_STREAM_FILENAME_ENDS_WITH))
        .toList();

      // publish .ts files
      for (String ts : tsKeys) stream(ts, contentTypeTS, true);

      // publish .m3u8 file
      stream(m3u8Key, contentTypeM3U8, false);

    } catch (IOException e) {
      LOG.error("Failed to publish!", e);

    } catch (FileStoreException e) {
      LOG.error("Failed to put file to Amazon S3!", e);
    }
  }

  /**
   Stream a file from temp path to S3

   @param key          of file (in temp folder and on S3 target)
   @param contentType  content-type
   @param skipExisting if already found to exist in S3
   @throws FileStoreException on failure
   */
  private void stream(String key, String contentType, Boolean skipExisting) throws FileStoreException {
    if (skipExisting && fileStore.doesS3ObjectExist(bucket, key)) return;
    var path = String.format("%s%s", tempFilePathPrefix, key);
    fileStore.putS3ObjectFromTempFile(path, bucket, key, contentType);
    LOG.info("Did stream {}/{} ({})", bucket, key, contentType);
  }
}
