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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
public class DubAudioCacheImpl implements DubAudioCache {
  final static Logger LOG = LoggerFactory.getLogger(DubAudioCacheImpl.class);
  String pathPrefix;
  final HttpClientProvider httpClientProvider;
  final String audioFileBucket;
  final String audioBaseUrl;
  final CloseableHttpClient client;

  @Autowired
  public DubAudioCacheImpl(
    @Value("${audio.cache.file-prefix}") String audioCacheFilePrefix,
    HttpClientProvider httpClientProvider,
    @Value("${audio.file.bucket}") String audioFileBucket,
    @Value("${audio.base.url}") String audioBaseUrl
  ) {
    this.httpClientProvider = httpClientProvider;
    this.client = httpClientProvider.getClient();
    this.audioFileBucket = audioFileBucket;
    this.audioBaseUrl = audioBaseUrl;

    try {
      pathPrefix = 0 < audioCacheFilePrefix.length() ?
        audioCacheFilePrefix :
        Files.createTempDirectory("cache").toAbsolutePath().toString();
      // make directory for cache files
      File dir = new File(pathPrefix);
      if (!dir.exists()) {
        FileUtils.forceMkdir(dir);
      }
      LOG.debug("Initialized audio cache directory: {}", pathPrefix);

    } catch (IOException e) {
      LOG.error("Failed to initialize audio cache directory", e);
    }
  }

  @Override
  public String load(String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException {
    if (StringUtils.isNullOrEmpty(waveformKey)) throw new FileStoreException("Can't load null or empty audio key!");
    var absolutePath = String.format("%s%s", pathPrefix, waveformKey);
    if (existsOnDisk(absolutePath)) {
      Files.delete(Path.of(absolutePath));
    }
    try (
      CloseableHttpResponse response = this.client.execute(new HttpGet(String.format("%s%s", audioBaseUrl, waveformKey)))
    ) {
      writeFrom(response.getEntity().getContent(), targetFrameRate, targetSampleBits, targetChannels, absolutePath);
    } catch (IOException e) {
      throw new NexusException(String.format("Dub audio cache failed to stream audio from s3://%s/%s", audioFileBucket, waveformKey), e);
    }
    return absolutePath;
  }

  /**
   * @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  boolean existsOnDisk(String absolutePath) {
    return new File(absolutePath).exists();
  }

  /**
   * write underlying cache data on disk, of stream
   *
   * @param sourceData       source bytes
   * @param targetFrameRate  to resample if necessary
   * @param targetSampleBits to resample if necessary
   * @param targetChannels   to resample if necessary
   * @param targetPath       write target path
   * @throws IOException on failure
   */
  public void writeFrom(InputStream sourceData, int targetFrameRate, int targetSampleBits, int targetChannels, String targetPath) throws IOException, NexusException {
    if (Objects.isNull(sourceData))
      throw new NexusException(String.format("Unable to write bytes to disk cache: %s", targetPath));

    var tempPath = Files.createTempFile("dub-audio-cache-item", ".wav").toString();

    try (OutputStream toFile = FileUtils.openOutputStream(new File(tempPath))) {
      var size = IOUtils.copy(sourceData, toFile); // stores number of bytes copied
      LOG.debug("Did write media item to disk cache: {} ({} bytes)", tempPath, size);
    }

    // Check if the audio file has the target frame rate
    var currentFormat = getAudioFormat(tempPath);
    if (currentFormat.getFrameRate() != targetFrameRate
      || currentFormat.getChannels() != targetChannels
      || currentFormat.getSampleSizeInBits() != targetSampleBits) {
      LOG.debug("Will resample audio file to {}Hz {}-bit {}-channel", targetFrameRate, targetSampleBits, targetChannels);
      FFmpegUtils.resampleAudio(tempPath, targetPath, targetFrameRate, targetSampleBits, targetChannels);
    } else {
      LOG.debug("Will move audio file from {} to {}", tempPath, targetFrameRate);
      Files.move(Path.of(tempPath), Path.of(targetPath));
    }

  }

  /**
   * Get the frame rate of the audio file
   *
   * @param inputAudioFilePath path to audio file
   * @return frame rate of audio file
   * @throws NexusException if unable to get frame rate
   */
  private static AudioFormat getAudioFormat(String inputAudioFilePath) throws NexusException {
    try {
      return AudioSystem.getAudioFileFormat(new File(inputAudioFilePath)).getFormat();

    } catch (UnsupportedAudioFileException | IOException e) {
      LOG.error("Unable to get audio format from file: {}", inputAudioFilePath, e);
      throw new NexusException(String.format("Unable to get audio frame rate from file: %s", inputAudioFilePath), e);
    }
  }

}
