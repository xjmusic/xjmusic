// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.dub;

import io.xj.hub.util.StringUtils;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.mixer.FFmpegUtils;
import io.xj.nexus.NexusException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class DubAudioCacheImpl implements DubAudioCache {
  final static Logger LOG = LoggerFactory.getLogger(DubAudioCacheImpl.class);
  final HttpClientProvider httpClientProvider;

  public DubAudioCacheImpl(
    HttpClientProvider httpClientProvider
  ) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public String load(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException {
    if (StringUtils.isNullOrEmpty(waveformKey)) throw new FileStoreException("Can't load null or empty audio key!");

    // compute a key based on the target frame rate, sample bits, channels, and waveform key.
    String originalCachePath = computeCachePath(contentStoragePathPrefix, instrumentId, waveformKey);
    String finalCachePath = computeCachePath(contentStoragePathPrefix, instrumentId, String.format("%d-%d-%d-%s", targetFrameRate, targetSampleBits, targetChannels, waveformKey));
    if (existsOnDisk(originalCachePath) && existsOnDisk(finalCachePath)) {
      LOG.debug("Found dub cache audio existing {} and final {}", originalCachePath, finalCachePath);
      return finalCachePath;
    }
    Files.createDirectories(Path.of(computeCachePath(contentStoragePathPrefix, instrumentId, "")));

    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", audioBaseUrl, waveformKey)))
    ) {
      if (Objects.isNull(response.getEntity().getContent()))
        throw new NexusException(String.format("Unable to write bytes to disk cache: %s", originalCachePath));

      try (OutputStream toFile = FileUtils.openOutputStream(new File(originalCachePath))) {
        var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
        LOG.debug("Did write media item to disk cache: {} ({} bytes)", originalCachePath, size);
      }

      // Check if the audio file has the target frame rate
      var currentFormat = getAudioFormat(originalCachePath);
      if (currentFormat.getFrameRate() != targetFrameRate) {
        LOG.debug("Will resample audio file to {}Hz {}-bit {}-channel", targetFrameRate, targetSampleBits, targetChannels);
        FFmpegUtils.resampleAudio(originalCachePath, finalCachePath, targetFrameRate, targetSampleBits, targetChannels);
      } else {
        LOG.debug("Will copy audio file from {} to {}", originalCachePath, finalCachePath);
        Files.copy(Path.of(originalCachePath), Path.of(finalCachePath));
      }
    } catch (IOException e) {
      throw new NexusException(String.format("Dub audio cache failed to stream audio from %s%s", audioBaseUrl, waveformKey), e);
    }
    return finalCachePath;
  }

  /**
   compute the cache path for this audio item

   @param contentStoragePathPrefix content storage path prefix
   @param instrumentId             instrument id
   @param key                      key
   @return cache path
   */
  private String computeCachePath(String contentStoragePathPrefix, UUID instrumentId, String key) {
    return contentStoragePathPrefix + "instrument" + File.separator + instrumentId.toString() + File.separator + key;
  }

  /**
   @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  boolean existsOnDisk(String absolutePath) {
    return new File(absolutePath).exists();
  }

  /**
   Get the frame rate of the audio file

   @param inputAudioFilePath path to audio file
   @return frame rate of audio file
   @throws NexusException if unable to get frame rate
   */
  static AudioFormat getAudioFormat(String inputAudioFilePath) throws NexusException {
    try {
      return AudioSystem.getAudioFileFormat(new File(inputAudioFilePath)).getFormat();

    } catch (UnsupportedAudioFileException | IOException e) {
      LOG.error("Unable to get audio format from file: {}", inputAudioFilePath, e);
      throw new NexusException(String.format("Unable to get audio frame rate from file: %s", inputAudioFilePath), e);
    }
  }

}
