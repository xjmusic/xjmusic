package io.xj.ship.work;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.ValueException;
import io.xj.ship.ShipException;
import io.xj.ship.broadcast.PlaylistProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public class PlaylistPublisherImpl implements PlaylistPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(ChunkPrinterImpl.class);
  private final FileStoreProvider fileStoreProvider;
  private final PlaylistProvider playlistProvider;
  private final String mpdMimeType;
  private final String playlistKey;
  private final String shipKey;
  private final String shipSource;
  private final String shipTitle;
  private final String streamBucket;

  @Inject
  public PlaylistPublisherImpl(
    @Assisted("shipKey") String shipKey,
    @Assisted("shipTitle") String shipTitle,
    @Assisted("shipSource") String shipSource,
    Environment env,
    FileStoreProvider fileStoreProvider,
    PlaylistProvider playlistProvider
  ) {
    this.shipKey = shipKey;
    this.shipTitle = shipTitle;
    this.shipSource = shipSource;
    this.fileStoreProvider = fileStoreProvider;

    streamBucket = env.getStreamBucket();
    playlistKey = String.format("%s.mpd", shipKey);
    mpdMimeType = env.getShipMpdMimeType();
    this.playlistProvider = playlistProvider;
  }

  @Override
  public void publish(long nowMillis) {
    String content = "";
    try {
      content = playlistProvider.computeMpdXML(shipKey, shipTitle, shipSource, nowMillis);
      fileStoreProvider.putS3ObjectFromString(content, streamBucket, playlistKey, mpdMimeType);
      LOG.info("did ship {} bytes to s3://{}/{}", content.length(), streamBucket, playlistKey);
    } catch (FileStoreException | IOException | ShipException | ValueException e) {
      LOG.error("failed to ship {} bytes to s3://{}/{}", content.length(), streamBucket, playlistKey);
    }
  }
}
