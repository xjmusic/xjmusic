// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.ship.ShipException;
import io.xj.ship.ShipMode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class PlaylistPublisherImpl implements PlaylistPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(PlaylistPublisherImpl.class);
  private final FileStoreProvider fileStore;
  private final HttpClientProvider httpClientProvider;
  private final PlaylistManager playlistManager;
  private final String bucket;
  private final String contentTypeM3U8;
  private final String m3u8Key;
  private final String streamBaseUrl;
  private final boolean active;
  private final int hlsStartPlaylistBehindSegments;

  @Inject
  public PlaylistPublisherImpl(
    @Assisted("shipKey") String shipKey,
    Environment env,
    FileStoreProvider fileStore,
    HttpClientProvider httpClientProvider,
    PlaylistManager playlistManager
  ) {
    this.fileStore = fileStore;
    this.httpClientProvider = httpClientProvider;
    this.playlistManager = playlistManager;

    active = ShipMode.HLS.equals(env.getShipMode());
    bucket = env.getStreamBucket();
    contentTypeM3U8 = env.getShipM3u8ContentType();
    hlsStartPlaylistBehindSegments = env.getHlsStartPlaylistBehindSegments();
    streamBaseUrl = env.getStreamBaseUrl();

    m3u8Key = String.format("%s.m3u8", shipKey);
  }

  @Override
  public void rehydratePlaylist() {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", streamBaseUrl, m3u8Key)))
    ) {
      LOG.debug("will check for last shipped playlist");
      var added = playlistManager.parseAndLoadItems(new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
      for (var item : added)
        LOG.info("Did rehydrate {}/{} @ media sequence {}", bucket, item.getFilename(), item.getSequenceNumber());
      LOG.info("Rehydrated {} items OK from playlist {}/{}", added.size(), bucket, m3u8Key);

    } catch (ClassCastException | IOException e) {
      LOG.error("Failed to retrieve previously streamed playlist {}/{} because {}", bucket, m3u8Key, e.getMessage());
    }
  }

  @Override
  public void publish(long atMillis) throws ShipException {
    if (!active) return;
    try {
      var mediaSequence = playlistManager.computeMediaSequence(System.currentTimeMillis()) - hlsStartPlaylistBehindSegments;
      fileStore.putS3ObjectFromString(playlistManager.getPlaylistContent(mediaSequence), bucket, m3u8Key, contentTypeM3U8);
      LOG.info("Did stream {}/{} ({}) @ media sequence {}", bucket, m3u8Key, contentTypeM3U8, mediaSequence);

    } catch (FileStoreException e) {
      throw new ShipException("Failed ot publish playlist!", e);
    }
  }
}
