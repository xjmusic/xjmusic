// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.api.Segment;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.Command;
import io.xj.lib.util.Files;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Singleton
class SegmentAudioCacheImpl implements SegmentAudioCache {
  private final Logger LOG = LoggerFactory.getLogger(SegmentAudioCacheImpl.class);
  private final String pathPrefix;
  private final String shipBucket;
  private final FileStoreProvider fileStoreProvider;

  @Inject
  SegmentAudioCacheImpl(
    Environment env,
    FileStoreProvider fileStoreProvider) {
    shipBucket = env.getShipBucket();
    pathPrefix = 0 < env.getAudioCacheFilePrefix().length() ?
      env.getAudioCacheFilePrefix() :
      Files.getTempFilePathPrefix() + "cache" + File.separator;
    this.fileStoreProvider = fileStoreProvider;

    try {
      // make directory for cache files
      File dir = new File(pathPrefix);
      if (!dir.exists()) {
        FileUtils.forceMkdir(dir);
      }
      LOG.debug("Initialized audio cache directory: {}", pathPrefix);

    } catch (IOException e) {
      LOG.error("Failed to initialize audio cache directory: {}", pathPrefix, e);
    }
  }

  @Override
  public String downloadAndDecompress(Segment segment) throws ShipException, IOException, InterruptedException {
    if (Strings.isNullOrEmpty(segment.getStorageKey()))
      throw new ShipException("Can't load null or empty audio key!");

    collectGarbage(segment);

    var pathOGG = downloadOriginal(segment);
    var pathWAV = computeAbsolutePathToUncompressed(segment);
    Command.execute("to decode WAV from source OGG", List.of(
      "ffmpeg",
      "-i", pathOGG,
      pathWAV));

    return pathWAV;
  }

  @Override
  public void collectGarbage(Segment segment) {
    try {
      java.nio.file.Files.deleteIfExists(Path.of(computeAbsolutePathToOriginal(segment)));
      java.nio.file.Files.deleteIfExists(Path.of(computeAbsolutePathToUncompressed(segment)));

    } catch (IOException e) {
      LOG.error("Failed to collect garbage for Segment[{}]", Segments.getIdentifier(segment), e);
    }
  }

  /**
   Download the original audio from the segment

   @param segment for which to download audio
   @return absolute path on disk where the audio is now stored
   */
  private String downloadOriginal(Segment segment) throws ShipException {
    var key = Segments.getStorageFilename(segment);
    var absolutePath = computeAbsolutePathToOriginal(segment);
    try (InputStream stream = fileStoreProvider.streamS3Object(shipBucket, key)) {
      if (Objects.isNull(stream))
        throw new ShipException(String.format("Unable to write bytes to disk cache: %s", absolutePath));

      try (OutputStream toFile = FileUtils.openOutputStream(new File(absolutePath))) {
        var size = IOUtils.copy(stream, toFile); // stores number of bytes copied
        LOG.debug("Did write original Segment Audio item to disk cache: {} ({} bytes)", absolutePath, size);
      }

      return absolutePath;

    } catch (FileStoreException | IOException e) {
      throw new ShipException(String.format("Failed to stream audio from s3://%s/%s", shipBucket, key), e);
    }
  }

  /**
   Compute the absolute path to the original segment audio

   @param segment for which to get audio path
   @return absolute path
   */
  private String computeAbsolutePathToOriginal(Segment segment) {
    return String.format("%s%s", pathPrefix, Segments.getStorageFilename(segment));
  }

  /**
   Compute the absolute path to the uncompressed segment audio

   @param segment for which to get audio path
   @return absolute path
   */
  private String computeAbsolutePathToUncompressed(Segment segment) {
    return String.format("%s%s", pathPrefix, Segments.getUncompressedStorageFilename(segment));
  }
}
