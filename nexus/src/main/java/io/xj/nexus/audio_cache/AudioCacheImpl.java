// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.audio_cache;

import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.mixer.ActiveAudio;
import io.xj.nexus.mixer.AudioSampleFormat;
import io.xj.nexus.mixer.FFmpegUtils;
import io.xj.nexus.mixer.FormatException;
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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AudioCacheImpl implements AudioCache {
  private final static Logger LOG = LoggerFactory.getLogger(AudioCacheImpl.class);
  private static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  private static final int READ_BUFFER_BYTE_SIZE = 1024;
  private final HttpClientProvider httpClientProvider;
  private final Map<String, CachedAudio> cache;
  private String contentStoragePathPrefix;
  private String audioBaseUrl;
  private int targetFrameRate;
  private int targetSampleBits;
  private int targetChannels;

  public AudioCacheImpl(
    HttpClientProvider httpClientProvider
  ) {
    this.httpClientProvider = httpClientProvider;

    cache = new ConcurrentHashMap<>();
  }

  @Override
  public CachedAudio load(InstrumentAudio audio) throws AudioCacheException, IOException, NexusException {
    if (!cache.containsKey(audio.getId().toString())) {
      cache.put(audio.getId().toString(), compute(audio));
    }
    return cache.get(audio.getId().toString());
  }

  @Override
  public void loadTheseAndForgetTheRest(List<InstrumentAudio> audios) {
    for (InstrumentAudio audio : audios) {
      if (!cache.containsKey(audio.getId().toString())) {
        try {
          cache.put(audio.getId().toString(), compute(audio));
        } catch (AudioCacheException | IOException e) {
          LOG.error("Failed to load audio into cache: {}", audio.getId(), e);
        }
      }
    }
    Set<String> audioIds = audios.stream().map(InstrumentAudio::getId).map(UUID::toString).collect(Collectors.toSet());
    for (String key : cache.keySet()) {
      if (!audioIds.contains(key)) {
        cache.remove(key);
      }
    }
  }

  @Override
  public void prepare(InstrumentAudio audio) throws NexusException {
    fetchAndPrepareOnDiskThreeAttempts(audio);
  }

  @Override
  public void initialize(String contentStoragePathPrefix, String audioBaseUrl, int targetFrameRate, int targetSampleBits, int targetChannels) {
    this.contentStoragePathPrefix = contentStoragePathPrefix;
    this.audioBaseUrl = audioBaseUrl;
    this.targetFrameRate = targetFrameRate;
    this.targetSampleBits = targetSampleBits;
    this.targetChannels = targetChannels;
  }

  @Override
  public void invalidateAll() {
    cache.clear();
  }

  @Override
  public boolean areAllReady(List<ActiveAudio> activeAudios) {
    return activeAudios.stream().allMatch(activeAudio -> cache.containsKey(activeAudio.getAudio().getId().toString()));
  }

  private void fetchAndPrepareOnDiskThreeAttempts(InstrumentAudio audio) throws NexusException {
    int attempts = 0;
    while (attempts < 3) {
      try {
        fetchAndPrepareOnDisk(audio);
        return;

      } catch (Exception e) {
        attempts++;
        if (attempts == 3) {
          throw new NexusException(String.format("Failed to fetch and prepare audio on disk (attempt %d of 3, contentStoragePathPrefix=%s, audioBaseUrl=%s, instrumentId=%s, waveformKey=%s, targetFrameRate=%d, targetSampleBits=%d, targetChannels=%d): %s", attempts,
            contentStoragePathPrefix, audioBaseUrl, audio.getInstrumentId(), audio.getWaveformKey(), targetFrameRate, targetSampleBits, targetChannels, e.getMessage()));
        } else {
          LOG.warn("Failed to fetch and prepare audio on disk (attempt {} of 3)", attempts, e);
        }
      }
    }
    throw new NexusException(String.format("Failed to fetch and prepare audio on disk (attempt %d of 3): %s", attempts, "Unknown error"));
  }

  private AudioPreparedOnDisk fetchAndPrepareOnDisk(InstrumentAudio audio) throws AudioCacheException {
    // TODO no HTTP fetching, only source from project source audio folder and resample into render audio folder

    if (StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
      LOG.error("Can't load null or empty audio key! (contentStoragePathPrefix={}, audioBaseUrl={}, instrumentId={}, waveformKey={}, targetFrameRate={}, targetSampleBits={}, targetChannels={})",
        contentStoragePathPrefix, audioBaseUrl, audio.getInstrumentId(), audio.getWaveformKey(), targetFrameRate, targetSampleBits, targetChannels);
      throw new AudioCacheException("Can't load null or empty audio key!");
    }

    // compute a key based on the target frame rate, sample bits, channels, and waveform key.
    String originalCachePath = computeCachePath(contentStoragePathPrefix, audio.getInstrumentId(), audio.getWaveformKey());
    String resampledCachePrefix = computeResampledCachePrefix(contentStoragePathPrefix, audio.getInstrumentId(), targetFrameRate, targetSampleBits, targetChannels);
    String finalCachePath = computeResampledCachePath(resampledCachePrefix, targetFrameRate, targetSampleBits, targetChannels, audio.getWaveformKey());
    if (existsOnDisk(finalCachePath)) {
      LOG.debug("Found fully prepared audio at {}", finalCachePath);
      try {
        AudioFormat currentFormat = getAudioFormat(finalCachePath);
        return new AudioPreparedOnDisk(finalCachePath, currentFormat);
      } catch (UnsupportedAudioFileException | IOException e) {
        deleteIfExists(finalCachePath);
      }
    }

    // Create the directory if it doesn't exist
    try {
      Files.createDirectories(Path.of(resampledCachePrefix));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Fetch via HTTP if original does not exist
    if (!existsOnDisk(originalCachePath)) {
      CloseableHttpClient client = httpClientProvider.getClient();
      try (
        CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", audioBaseUrl, audio.getWaveformKey())))
      ) {
        if (Objects.isNull(response.getEntity().getContent()))
          throw new NexusException(String.format("Unable to write bytes to disk cache: %s", originalCachePath));

        try (OutputStream toFile = FileUtils.openOutputStream(new File(originalCachePath))) {
          var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
          LOG.debug("Did write media item to disk cache: {} ({} bytes)", originalCachePath, size);
        }
      } catch (NexusException | IOException e) {
        throw new RuntimeException(e);
      }
    }

    // If the audio format does not match, resample. FUTURE: don't increase # of channels unnecessarily
    AudioFormat currentFormat;
    try {
      currentFormat = getAudioFormat(originalCachePath);
    } catch (UnsupportedAudioFileException | IOException e) {
      deleteIfExists(originalCachePath);
      throw new RuntimeException(e);
    }

    try {
      if (!matchesAudioFormat(currentFormat, targetFrameRate, targetSampleBits, targetChannels)) {
        LOG.debug("Will resample audio file to {}Hz {}-bit {}-channel", targetFrameRate, targetSampleBits, targetChannels);
        FFmpegUtils.resampleAudio(originalCachePath, finalCachePath, targetFrameRate, targetSampleBits, targetChannels);
      } else {
        LOG.debug("Will copy audio file from {} to {}", originalCachePath, finalCachePath);
        Files.copy(Path.of(originalCachePath), Path.of(finalCachePath));
      }
      var finalFormat = getAudioFormat(finalCachePath);
      return new AudioPreparedOnDisk(finalCachePath, finalFormat);
    } catch (RuntimeException | UnsupportedAudioFileException | IOException e) {
      deleteIfExists(finalCachePath);
      throw new RuntimeException(e);
    }
  }

  private void deleteIfExists(String path) {
    try {
      Files.deleteIfExists(Path.of(path));
    } catch (IOException e) {
      LOG.error("Unable to delete file: {}", path, e);
    }
  }

  private CachedAudio compute(InstrumentAudio audio) throws AudioCacheException, IOException {
    var fileSpec = fetchAndPrepareOnDisk(audio);
    try (
      var fileInputStream = FileUtils.openInputStream(new File(fileSpec.path));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var frameSize = fileSpec.format.getFrameSize();
      var channels = fileSpec.format.getChannels();
      var isStereo = 2 == channels;
      var sampleSize = frameSize / channels;
      var expectBytes = audioInputStream.available();

      if (MAX_INT_LENGTH_ARRAY_SIZE == expectBytes)
        throw new IOException("loading audio streams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");

      int expectFrames;
      if (expectBytes == audioInputStream.getFrameLength()) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / fileSpec.format.getFrameSize();
      } else {
        expectFrames = (int) audioInputStream.getFrameLength();
      }

      if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames)
        throw new IOException("audio streams with unspecified frame size or length are unsupported");

      AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(fileSpec.format);

      // buffer size always a multiple of frame size
      int actualReadBufferSize = (int) (Math.floor((double) READ_BUFFER_BYTE_SIZE / frameSize) * frameSize);

      int b; // iterator: byte
      int tc; // iterators: source channel, target channel
      int sf = 0; // current source frame
      int numBytesReadToBuffer;
      byte[] sampleBuffer = new byte[sampleSize];
      byte[] readBuffer = new byte[actualReadBufferSize];
      float[][] data = new float[expectFrames][channels];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer && sf < data.length; b += frameSize) {
          for (tc = 0; tc < fileSpec.format.getChannels(); tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            data[sf][tc] = (float) AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
          }
          sf++;
        }
      }
      return new CachedAudio(data, fileSpec.format, fileSpec.path);

    } catch (UnsupportedAudioFileException | FormatException e) {
      throw new IOException(String.format("Failed to read and compute float array for file %s", fileSpec.path), e);
    }
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
   compute the cache path for this audio item

   @param contentStoragePathPrefix content storage path prefix
   @param instrumentId             instrument id
   @param targetFrameRate          target frame rate
   @param targetSampleBits         target sample bits
   @param targetChannels           target channels
   @return cache path
   */
  private String computeResampledCachePrefix(String contentStoragePathPrefix, UUID instrumentId, int targetFrameRate, int targetSampleBits, int targetChannels) {
    return contentStoragePathPrefix + "instrument" + File.separator + instrumentId.toString() + File.separator + String.format("%d-%d-%d", targetFrameRate, targetSampleBits, targetChannels) + File.separator;
  }

  /**
   compute the cache path for this audio item

   @param resampledCachePrefix resampled cache prefix
   @param targetFrameRate      target frame rate
   @param targetSampleBits     target sample bits
   @param targetChannels       target channels
   @param waveformKey          waveform key
   @return cache path
   */
  private String computeResampledCachePath(String resampledCachePrefix, int targetFrameRate, int targetSampleBits, int targetChannels, String waveformKey) {
    return resampledCachePrefix + String.format("%d-%d-%d-%s", targetFrameRate, targetSampleBits, targetChannels, waveformKey);
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
   */
  private static AudioFormat getAudioFormat(String inputAudioFilePath) throws UnsupportedAudioFileException, IOException {
    return AudioSystem.getAudioFileFormat(new File(inputAudioFilePath)).getFormat();
  }

  /**
   Whether the audio format matches the target frame rate, sample bits, and channels

   @param currentFormat    current audio format
   @param targetFrameRate  target frame rate
   @param targetSampleBits target sample bits
   @param targetChannels   target channels
   @return true if matches
   */
  private boolean matchesAudioFormat(AudioFormat currentFormat, int targetFrameRate, int targetSampleBits, int targetChannels) {
    return currentFormat.getFrameRate() == targetFrameRate
      && currentFormat.getSampleSizeInBits() == targetSampleBits

      // Mono source audio should be resampled and cached as mono
      // https://www.pivotaltracker.com/story/show/186551859
      && currentFormat.getChannels() <= targetChannels;
  }

  /**
   Record of an audio file prepared on disk for caching
   */
  private record AudioPreparedOnDisk(String path, AudioFormat format) {
  }
}
