package io.xj.ship.work;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.ship.persistence.ChunkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static io.xj.lib.util.Files.getFileSize;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public class ChunksPublisherImpl extends ChunksPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(ChunkPrinterImpl.class);
  private static final int AUDIO_CHANNELS = 2;
  private static final float QUALITY = 100;
  private final ChunkManager chunkManager;
  private final FileStoreProvider fileStoreProvider;
  private final String playlistKey;
  private final String shipKey;
  private final String streamBucket;
  private final int shipChunkSeconds;
  private final String m3u8ContentType;

  @Inject
  public ChunksPublisherImpl(
    @Assisted("shipKey") String shipKey,
    ChunkManager chunkManager,
    Environment env,
    FileStoreProvider fileStoreProvider
  ) {
    this.shipKey = shipKey;
    this.chunkManager = chunkManager;
    this.fileStoreProvider = fileStoreProvider;

    String mp2tsBitrate = env.getShipMp2tsBitrate();
    shipChunkSeconds = env.getShipChunkSeconds();
    streamBucket = env.getStreamBucket();
    playlistKey = String.format("%s.m3u8", shipKey);
    m3u8ContentType = env.getShipM3u8ContentType();
  }

  @Override
  public void compute() {
    List<String> lines = Lists.newArrayList();
    lines.add("#EXTM3U");
    lines.add(String.format("#EXT-X-TARGETDURATION:%d", shipChunkSeconds));
    lines.add("#EXT-X-VERSION:4");
    lines.add("#EXT-X-MEDIA-SEQUENCE:1");
    for (var chunk : chunkManager.computeAllContiguousDone(shipKey))
      for (var key : chunk.getStreamOutputKeys()) {
        lines.add("#EXTINF:6.0,");
        lines.add(key);
      }
    var content = String.join("\n", lines);
    try {
      fileStoreProvider.putS3ObjectFromString(content, streamBucket, playlistKey, m3u8ContentType);
      LOG.info("did ship {} bytes to s3://{}/{}", content.length(), playlistKey, streamBucket);
    } catch (FileStoreException e) {
      LOG.error("failed to ship {} bytes to s3://{}/{}", content.length(), streamBucket, playlistKey);
    }
  }
}
