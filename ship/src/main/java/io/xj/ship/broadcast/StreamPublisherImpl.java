// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 <p>
 Ship #EXT-X-INDEPENDENT-SEGMENTS #180669689
 */
public class StreamPublisherImpl implements StreamPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(StreamPublisherImpl.class);
  private final FileStoreProvider fileStore;
  private final String bucket;
  private final String contentTypeM3U8;
  private final String contentTypeSegment;
  private final String m3u8Key;
  private final M3U8PlaylistManager m3u8PlaylistManager;
  private final String playlistPath;
  private final String tempFilePathPrefix;
  private final int hlsStartPlaylistBehindSegments;

  @Inject
  public StreamPublisherImpl(
    @Assisted("shipKey") String shipKey,
    Environment env,
    FileStoreProvider fileStore,
    M3U8PlaylistManager m3u8PlaylistManager
  ) {
    this.fileStore = fileStore;
    this.m3u8PlaylistManager = m3u8PlaylistManager;

    contentTypeM3U8 = env.getShipM3u8ContentType();
    contentTypeSegment = env.getShipSegmentContentType();
    tempFilePathPrefix = env.getTempFilePathPrefix();
    hlsStartPlaylistBehindSegments = env.getHlsStartPlaylistBehindSegments();
    m3u8Key = String.format("%s.m3u8", shipKey);
    playlistPath = String.format("%s%s", tempFilePathPrefix, m3u8Key);
    bucket = env.getStreamBucket();
  }

  @Override
  public Boolean rehydratePlaylist() {
    try {
      LOG.debug("will check for last shipped playlist");
      if (!fileStore.doesS3ObjectExist(bucket, m3u8Key)) {
        LOG.info("Playlist {}/{} has not been shipped; will not rehydrate.", bucket, m3u8Key);
        return false;
      }
      var added = m3u8PlaylistManager.parseAndLoadItems(new String(fileStore.streamS3Object(bucket, m3u8Key).readAllBytes(), StandardCharsets.UTF_8));
      for (var item : added)
        LOG.info("Did rehydrate {}/{} @ media sequence {}", bucket, item.getFilename(), item.getMediaSequence());
      LOG.info("Rehydrated {} items OK from playlist {}/{}", added.size(), bucket, m3u8Key);
      return true;

    } catch (FileStoreException | ClassCastException | IOException e) {
      LOG.error("Failed to retrieve previously streamed playlist {}/{} because {}", bucket, m3u8Key, e.getMessage());
      return false;
    }
  }

  @Override
  public void publish(long nowMillis) {
    try {
      // test for existence of playlist file; skip if nonexistent
      if (!new File(playlistPath).exists()) return;

      // parse ffmpeg .m3u8 content into playlist manager
      var added = m3u8PlaylistManager.parseAndLoadItems(Files.getFileContent(playlistPath));

      // publish new filenames
      for (M3U8PlaylistItem item : added)
        stream(item.getFilename(), contentTypeSegment);

      // publish custom .m3u8 file
      var mediaSequence = m3u8PlaylistManager.computeMediaSequence(System.currentTimeMillis()) - hlsStartPlaylistBehindSegments;
      fileStore.putS3ObjectFromString(m3u8PlaylistManager.getPlaylistContent(mediaSequence), bucket, m3u8Key, contentTypeM3U8);
      LOG.info("Did stream {}/{} ({}) @ media sequence {}", bucket, m3u8Key, contentTypeM3U8, mediaSequence);

    } catch (IOException | FileStoreException e) {
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
