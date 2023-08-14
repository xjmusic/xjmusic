// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.dub;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.WaveformWriter;
import be.tarsos.dsp.resample.RateTransposer;
import io.xj.hub.util.StringUtils;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.http.HttpClientProvider;
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
import java.io.BufferedInputStream;
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

  @Autowired
  public DubAudioCacheImpl(
    @Value("${audio.cache.file-prefix}") String audioCacheFilePrefix,
    HttpClientProvider httpClientProvider,
    @Value("${audio.file.bucket}") String audioFileBucket,
    @Value("${audio.base.url}") String audioBaseUrl
  ) {
    this.httpClientProvider = httpClientProvider;
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
  public String load(String waveformKey, int targetFrameRate) throws FileStoreException, IOException, NexusException {
    if (StringUtils.isNullOrEmpty(waveformKey)) throw new FileStoreException("Can't load null or empty audio key!");
    var absolutePath = String.format("%s%s", pathPrefix, waveformKey);
    if (existsOnDisk(absolutePath)) {
      Files.delete(Path.of(absolutePath));
    }
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", audioBaseUrl, waveformKey)))
    ) {
      writeFrom(response.getEntity().getContent(), targetFrameRate, absolutePath);
    } catch (IOException e) {
      throw new NexusException(String.format("Dub audio cache failed to stream audio from s3://%s/%s", audioFileBucket, waveformKey), e);
    }
    return absolutePath;
  }

  /**
   @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  boolean existsOnDisk(String absolutePath) {
    return new File(absolutePath).exists();
  }

  /**
   write underlying cache data on disk, of stream

   @param sourceData      source bytes
   @param targetFrameRate to resample if necessary
   @param targetPath      write target path
   @throws IOException on failure
   */
  public void writeFrom(InputStream sourceData, int targetFrameRate, String targetPath) throws IOException, NexusException {
    if (Objects.isNull(sourceData))
      throw new NexusException(String.format("Unable to write bytes to disk cache: %s", targetPath));

    var tempPath = Files.createTempFile("dub-audio-cache-item", ".wav").toString();

    try (OutputStream toFile = FileUtils.openOutputStream(new File(tempPath))) {
      var size = IOUtils.copy(sourceData, toFile); // stores number of bytes copied
      LOG.debug("Did write media item to disk cache: {} ({} bytes)", tempPath, size);
    }

    // Check if the audio file has the target frame rate
    int currentFrameRate = getAudioFrameRate(tempPath);
    if (currentFrameRate != targetFrameRate) {
      LOG.debug("Will resample audio file from {} to {}", currentFrameRate, targetFrameRate);
      convertAudio(tempPath, targetPath, targetFrameRate);
    } else {
      LOG.debug("Will move audio file from {} to {}", currentFrameRate, targetFrameRate);
      Files.move(Path.of(tempPath), Path.of(targetPath));
    }
  }

  /**
   Get the frame rate of the audio file

   @param inputAudioFilePath path to audio file
   @return frame rate of audio file
   @throws NexusException if unable to get frame rate
   */
  private static int getAudioFrameRate(String inputAudioFilePath) throws NexusException {
    try (
      var fileInputStream = FileUtils.openInputStream(new File(inputAudioFilePath));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var _audioFormat = audioInputStream.getFormat();
      return (int) _audioFormat.getFrameRate();

    } catch (UnsupportedAudioFileException | IOException e) {
      LOG.error("Unable to get audio frame rate from file: {}", inputAudioFilePath, e);
      throw new NexusException(String.format("Unable to get audio frame rate from file: %s", inputAudioFilePath), e);
    }
  }

  /**
   Convert the audio file to the target sample rate

   @param inputAudioFilePath  path to input audio file
   @param outputAudioFilePath path to output audio file
   @param targetSampleRate    target sample rate
   @throws NexusException if unable to convert audio
   */
  private static void convertAudio(String inputAudioFilePath, String outputAudioFilePath, int targetSampleRate) throws NexusException {
    try {
      AudioFormat sourceFormat = AudioSystem.getAudioFileFormat(new File(inputAudioFilePath)).getFormat();

      float resampleRatio = targetSampleRate / sourceFormat.getSampleRate();

      var targetFormat = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        targetSampleRate,
        sourceFormat.getSampleSizeInBits(),
        sourceFormat.getChannels(),
        sourceFormat.getFrameSize(),
        targetSampleRate,
        false);

      AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(new File(inputAudioFilePath), 2048, 1024);
      RateTransposer resampler = new RateTransposer(resampleRatio);
      WaveformWriter writer = new WaveformWriter(targetFormat, outputAudioFilePath);

      dispatcher.addAudioProcessor(resampler);
      dispatcher.addAudioProcessor(writer);

      dispatcher.run();
      LOG.info("Did resample audio file {} ({}Hz -> {}Hz)", outputAudioFilePath, sourceFormat.getSampleRate(), targetFormat.getSampleRate());

    } catch (UnsupportedAudioFileException | IOException e) {
      LOG.error("Unable to get audio frame rate from file: {}", inputAudioFilePath, e);
      throw new NexusException(String.format("Unable to get audio frame rate from file: %s", inputAudioFilePath), e);
    }
  }
}
