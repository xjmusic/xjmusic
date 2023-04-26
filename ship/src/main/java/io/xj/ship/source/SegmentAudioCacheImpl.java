// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.common.base.Strings;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.util.Command;
import io.xj.lib.util.Files;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Service
public class SegmentAudioCacheImpl implements SegmentAudioCache {
  private final Logger LOG = LoggerFactory.getLogger(SegmentAudioCacheImpl.class);
  private final HttpClientProvider httpClientProvider;
  private final String pathPrefix;
  private final String shipBaseUrl;
  private final String shipBucket;

  @Autowired
  public SegmentAudioCacheImpl(
    AppEnvironment env,
    HttpClientProvider httpClientProvider
  ) {
    shipBucket = env.getShipBucket();
    shipBaseUrl = env.getShipBaseUrl();
    pathPrefix = 0 < env.getAudioCacheFilePrefix().length() ?
      env.getAudioCacheFilePrefix() :
      Files.getTempFilePathPrefix() + "cache" + File.separator;
    this.httpClientProvider = httpClientProvider;

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
   * Download the original audio from the segment
   *
   * @param segment for which to download audio
   * @return absolute path on disk where the audio is now stored
   */
  private String downloadOriginal(Segment segment) throws ShipException {
    var key = Segments.getStorageFilename(segment);
    var absolutePath = computeAbsolutePathToOriginal(segment);
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, key)))
    ) {
      if (!Objects.equals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode()))
        throw new ShipException(String.format("Failed to get SegmentAudio[%s] because %d %s", key, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));

      if (Objects.isNull(response.getEntity().getContent()))
        throw new ShipException(String.format("Unable to read segment audio: %s", absolutePath));

      try (OutputStream toFile = FileUtils.openOutputStream(new File(absolutePath))) {
        var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
        LOG.debug("Did write original Segment Audio item to disk cache: {} ({} bytes)", absolutePath, size);
      }

      return absolutePath;

    } catch (IOException e) {
      throw new ShipException(String.format("Failed to stream audio from s3://%s/%s", shipBucket, key), e);
    }
  }

  /**
   * Compute the absolute path to the original segment audio
   *
   * @param segment for which to get audio path
   * @return absolute path
   */
  private String computeAbsolutePathToOriginal(Segment segment) {
    return String.format("%s%s", pathPrefix, Segments.getStorageFilename(segment));
  }

  /**
   * Compute the absolute path to the uncompressed segment audio
   *
   * @param segment for which to get audio path
   * @return absolute path
   */
  private String computeAbsolutePathToUncompressed(Segment segment) {
    return String.format("%s%s", pathPrefix, Segments.getUncompressedStorageFilename(segment));
  }
}
