// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;


import io.xj.lib.http.HttpClientProvider;
import io.xj.nexus.NexusException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Load audio from disk to memory, or if necessary, from S3 to disk (for future caching), then to memory.
 * <p>
 * NO LONGER using Caffeine in-memory caching-- just caching on disk originally loading from S3
 * <p>
 * Advanced audio caching during fabrication https://www.pivotaltracker.com/story/show/176642679
 */
public class DubAudioCacheItem {
  final Logger log = LoggerFactory.getLogger(DubAudioCacheItem.class);
  private final String key;
  private final String absolutePath;
  private int size; // # of bytes

  /**
   * @param httpClientProvider to use for http requests
   * @param key                of audio file
   * @param absolutePath       of audio file
   * @param audioFileBucket    of audio file
   * @param audioBaseUrl       of audio file
   * @throws NexusException on failure
   */
  public DubAudioCacheItem(
    HttpClientProvider httpClientProvider,
    String key,
    String absolutePath,
    @Value("${audio.file.bucket}") String audioFileBucket,
    @Value("${audio.base.url}") String audioBaseUrl
  ) throws NexusException {
    this.key = key;
    this.absolutePath = absolutePath;
    if (existsOnDisk()) return;
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", audioBaseUrl, key)))
    ) {
      writeFrom(response.getEntity().getContent());
    } catch (IOException e) {
      throw new NexusException(String.format("Dub audio cache failed to stream audio from s3://%s/%s", audioFileBucket, key), e);
    }
  }

  /**
   * key of stored data
   *
   * @return key
   */
  public String key() {
    return key;
  }

  /**
   * @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  private boolean existsOnDisk() {
    return new File(absolutePath).exists();
  }

  /**
   * write underlying cache data on disk, of stream
   *
   * @param data to save to file
   * @throws IOException on failure
   */
  public void writeFrom(InputStream data) throws IOException, NexusException {
    if (Objects.isNull(data))
      throw new NexusException(String.format("Unable to write bytes to disk cache: %s", absolutePath));

    try (OutputStream toFile = FileUtils.openOutputStream(new File(absolutePath))) {
      size = IOUtils.copy(data, toFile); // stores number of bytes copied
      log.debug("Did write media item to disk cache: {} ({} bytes)", absolutePath, size);
    }
  }

  /**
   * @return path to stored data file
   */
  public String path() {
    return absolutePath;
  }

  /**
   * @return size in # of bytes, of waveform audio loaded from disk into memory
   */
  public int size() {
    return size;
  }

  /**
   * @return absolute path to file in disk
   */
  public String getAbsolutePath() {
    return absolutePath;
  }
}
