// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Load audio from disk to memory, or if necessary, from S3 to disk (for future caching), then to memory.
 * <p>
 * NO LONGER using Caffeine in-memory caching-- just caching on disk originally loading from S3
 * <p>
 * Advanced audio caching during fabrication https://www.pivotaltracker.com/story/show/176642679
 */
public class DubAudioCacheItem {
  final Logger LOG = LoggerFactory.getLogger(DubAudioCacheItem.class);
  final String waveformKey;
  final String absolutePath;
  int size; // # of bytes

  /**
   * @param audioBaseUrl    is the base URL for audio files
   * @param audioFileBucket is the bucket for audio files
   * @param waveformKey     ot this item
   * @param cacheFilePrefix to this item's waveform data on disk
   * @param targetFrameRate to resample if necessary
   */
  public DubAudioCacheItem(
    String audioBaseUrl,
    String audioFileBucket,
    HttpClientProvider httpClientProvider,
    String waveformKey,
    String cacheFilePrefix,
    int targetFrameRate
  ) throws NexusException {
    this.waveformKey = waveformKey;
    this.absolutePath = String.format("%s%s", cacheFilePrefix, this.waveformKey);
    if (existsOnDisk()) {
      return;
    }
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", audioBaseUrl, waveformKey)))
    ) {
      writeFrom(response.getEntity().getContent(), targetFrameRate);
    } catch (IOException e) {
      throw new NexusException(String.format("Dub audio cache failed to stream audio from s3://%s/%s", audioFileBucket, waveformKey), e);
    }
  }

  /**
   * key of stored data
   *
   * @return key
   */
  public String key() {
    return waveformKey;
  }

  /**
   * @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  boolean existsOnDisk() {
    return new File(absolutePath).exists();
  }

  /**
   * write underlying cache data on disk, of stream
   *
   * @param data            to save to file
   * @param targetFrameRate to resample if necessary
   * @throws IOException on failure
   */
  public void writeFrom(InputStream data, int targetFrameRate) throws IOException, NexusException {
    if (Objects.isNull(data))
      throw new NexusException(String.format("Unable to write bytes to disk cache: %s", absolutePath));

    var tempPath = Files.createTempFile("dub-audio-cache-item", ".wav").toString();

    try (OutputStream toFile = FileUtils.openOutputStream(new File(tempPath))) {
      size = IOUtils.copy(data, toFile); // stores number of bytes copied
      LOG.debug("Did write media item to disk cache: {} ({} bytes)", tempPath, size);
    }

    // Check if the audio file has the target frame rate
    int currentFrameRate = getAudioFrameRate(tempPath);
    if (currentFrameRate != targetFrameRate) {
      convertAudio(tempPath, absolutePath, targetFrameRate);
      LOG.info("Did resample audio file {} ({}Hz -> {}Hz)", absolutePath, currentFrameRate, targetFrameRate);
    } else {
      Files.move(Path.of(tempPath), Path.of(absolutePath));
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


  private static int getAudioFrameRate(String inputFile) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder("ffprobe", "-v", "error", "-select_streams", "a:0",
      "-show_entries", "stream=sample_rate", "-of", "default=noprint_wrappers=1:nokey=1", inputFile);
    Process process = processBuilder.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line = reader.readLine();
      if (line != null) {
        return Integer.parseInt(line.trim());
      }
    }

    return -1; // Error occurred or frame rate not found
  }

  private static void convertAudio(String inputFile, String outputFile, int targetFrameRate) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg-failz", "-i", inputFile, "-ar", String.valueOf(targetFrameRate), outputFile); // todo fix this
    Process process = processBuilder.start();

    try {
      process.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
