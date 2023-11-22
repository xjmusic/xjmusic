// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.audio_cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.filestore.FileStoreException;
import io.xj.nexus.http.HttpClientProvider;
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
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public class AudioCacheImpl implements AudioCache {
  private final static Logger LOG = LoggerFactory.getLogger(AudioCacheImpl.class);
  private static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  private static final int MAX_CACHE_SIZE = 10_000;
  private static final int READ_BUFFER_BYTE_SIZE = 1024;
  private final HttpClientProvider httpClientProvider;
  private final LoadingCache<AudioCacheRequest, CachedAudio> cache;

  public AudioCacheImpl(
    HttpClientProvider httpClientProvider
  ) {
    this.httpClientProvider = httpClientProvider;

    cache = Caffeine.newBuilder()
      .maximumSize(MAX_CACHE_SIZE)
      .expireAfterWrite(Duration.ofMinutes(5))
      .refreshAfterWrite(Duration.ofMinutes(1))
      .build(this::compute);
  }

  @Override
  public CachedAudio load(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException {
    return cache.get(new AudioCacheRequest(instrumentId, waveformKey, contentStoragePathPrefix, audioBaseUrl, targetFrameRate, targetSampleBits, targetChannels));
  }

  @Override
  public void prepare(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException {
    fetchAndPrepareOnDisk(contentStoragePathPrefix, audioBaseUrl, instrumentId, waveformKey, targetFrameRate, targetSampleBits, targetChannels);
  }

  @Override
  public void invalidateAll() {
    cache.invalidateAll();
  }

  private AudioPreparedOnDisk fetchAndPrepareOnDisk(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException {
    if (StringUtils.isNullOrEmpty(waveformKey)) throw new FileStoreException("Can't load null or empty audio key!");

    // compute a key based on the target frame rate, sample bits, channels, and waveform key.
    String originalCachePath = computeCachePath(contentStoragePathPrefix, instrumentId, waveformKey);
    String finalCachePath = computeCachePath(contentStoragePathPrefix, instrumentId, String.format("%d-%d-%d-%s", targetFrameRate, targetSampleBits, targetChannels, waveformKey));
    if (existsOnDisk(finalCachePath)) {
      LOG.debug("Found fully prepared audio at {}", finalCachePath);
      var currentFormat = getAudioFormat(finalCachePath);
      return new AudioPreparedOnDisk(finalCachePath, currentFormat);
    }

    // Create the directory if it doesn't exist
    Files.createDirectories(Path.of(computeCachePath(contentStoragePathPrefix, instrumentId, "")));

    // Fetch via HTTP if original does not exist
    if (!existsOnDisk(originalCachePath)) {
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
      } catch (IOException e) {
        throw new NexusException(String.format("Dub audio cache failed to stream audio from %s%s", audioBaseUrl, waveformKey), e);
      } catch (NexusException e) {
        throw new RuntimeException(e);
      }
    }

    // If the audio format does not match, resample. FUTURE: don't increase # of channels unnecessarily
    var currentFormat = getAudioFormat(originalCachePath);
    if (!matchesAudioFormat(currentFormat, targetFrameRate, targetSampleBits, targetChannels)) {
      LOG.debug("Will resample audio file to {}Hz {}-bit {}-channel", targetFrameRate, targetSampleBits, targetChannels);
      FFmpegUtils.resampleAudio(originalCachePath, finalCachePath, targetFrameRate, targetSampleBits, targetChannels);
    } else {
      LOG.debug("Will copy audio file from {} to {}", originalCachePath, finalCachePath);
      Files.copy(Path.of(originalCachePath), Path.of(finalCachePath));
    }
    var finalFormat = getAudioFormat(finalCachePath);
    return new AudioPreparedOnDisk(finalCachePath, finalFormat);
  }

  private CachedAudio compute(AudioCacheRequest request) throws FileStoreException, IOException, NexusException {
    var fileSpec = fetchAndPrepareOnDisk(
      request.contentStoragePathPrefix,
      request.audioBaseUrl,
      request.instrumentId,
      request.waveformKey,
      request.targetFrameRate,
      request.targetSampleBits,
      request.targetChannels
    );
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
      float[][] audio = new float[expectFrames][channels];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer && sf < audio.length; b += frameSize) {
          for (tc = 0; tc < fileSpec.format.getChannels(); tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            audio[sf][tc] = (float) AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
          }
          sf++;
        }
      }
      return new CachedAudio(audio, fileSpec.format, fileSpec.path);
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
  private static AudioFormat getAudioFormat(String inputAudioFilePath) throws NexusException {
    try {
      return AudioSystem.getAudioFileFormat(new File(inputAudioFilePath)).getFormat();

    } catch (UnsupportedAudioFileException | IOException e) {
      throw new NexusException(String.format("Unable to get audio format from %s", inputAudioFilePath), e);
    }
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
      && currentFormat.getChannels() == targetChannels;
  }

  /**
   Record of a request to load audio from cache
   */
  private record AudioCacheRequest(
    UUID instrumentId,
    String waveformKey,
    String contentStoragePathPrefix,
    String audioBaseUrl,
    int targetFrameRate,
    int targetSampleBits,
    int targetChannels
  ) {
    public String toString() {
      return String.format("%s-%s-%s-%s-%d-%d-%d", contentStoragePathPrefix, audioBaseUrl, instrumentId, waveformKey, targetFrameRate, targetSampleBits, targetChannels);
    }
  }

  /**
   Record of an audio file prepared on disk for caching
   */
  private record AudioPreparedOnDisk(String path, AudioFormat format) {
  }
}
