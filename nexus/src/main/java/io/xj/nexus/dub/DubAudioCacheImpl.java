// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.dub;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
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
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public class DubAudioCacheImpl implements DubAudioCache {
  final static Logger LOG = LoggerFactory.getLogger(DubAudioCacheImpl.class);
  final HttpClientProvider httpClientProvider;
  private final LoadingCache<AudioCacheRequest, float[][]> cache;

  public DubAudioCacheImpl(
    HttpClientProvider httpClientProvider
  ) {
    this.httpClientProvider = httpClientProvider;

    cache = Caffeine.newBuilder()
      .maximumSize(10_000)
      .expireAfterWrite(Duration.ofMinutes(5))
      .refreshAfterWrite(Duration.ofMinutes(1))
      .build(this::compute);
    // TODO: clear (ensure garbage collection) of the aforementioned cache before exiting the application
  }

  @Override
  public float[][] load(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException {
    return cache.get(new AudioCacheRequest(instrumentId, waveformKey, contentStoragePathPrefix, audioBaseUrl, targetFrameRate, targetSampleBits, targetChannels));
  }

  private float[][] compute(AudioCacheRequest request) throws FileStoreException, IOException, NexusException {
    String cachePath = fetchAndPrepare(
      request.contentStoragePathPrefix,
      request.audioBaseUrl,
      request.instrumentId,
      request.waveformKey,
      request.targetFrameRate,
      request.targetSampleBits,
      request.targetChannels
    );
    /*
    // todo get the float[][] by reading the bytes from disk
    try (
      var fileInputStream = FileUtils.openInputStream(new File(source.getAbsolutePath()));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var frameSize = fmt.getFrameSize();
      var channels = fmt.getChannels();
      var isStereo = 2 == channels;
      var sampleSize = frameSize / channels;
      var expectBytes = audioInputStream.available();

      if (MAX_INT_LENGTH_ARRAY_SIZE == expectBytes)
        throw new MixerException("loading audio streams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");

      int expectFrames;
      if (expectBytes == source.getFrameLength()) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / fmt.getFrameSize();
      } else {
        expectFrames = (int) source.getFrameLength();
      }

      if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames)
        throw new MixerException("audio streams with unspecified frame size or length are unsupported");

      AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(fmt);

      int sf = 0; // current source frame
      int numBytesReadToBuffer;
      byte[] sampleBuffer = new byte[source.getSampleSize()];
      byte[] readBuffer = new byte[READ_BUFFER_BYTE_SIZE];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer; b += frameSize) {
          // FUTURE: skip frame if unnecessary (source rate higher than target rate)
          for (tc = 0; tc < outputChannels; tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            v = AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
            for (p = 0; p < srcPut.length; p++) {
              if (sf < srcPutSpan[p]) // attack phase
                ev = envelope.length(srcPut[p].getAttackMillis() * framesPerMilli).in(sf, v * srcPut[p].getVelocity());
              else // release phase
                ev = envelope.length(srcPut[p].getReleaseMillis() * framesPerMilli).out(sf - srcPutSpan[p], v * srcPut[p].getVelocity());

              ptf = srcPutFrom[p] + sf;
              if (ptf < 0 || ptf >= busBuf[0].length) continue;
              busBuf[srcPut[p].getBus()][ptf][tc] += ev;
            }
          }
          sf++;
        }
      }
    } catch (UnsupportedAudioFileException | IOException | FormatException e) {
      throw new MixerException(String.format("Failed to apply Source[%s]", source.getAudioId()), e);
    }


*/
  }

  private String fetchAndPrepare(String contentStoragePathPrefix, String audioBaseUrl, UUID instrumentId, String waveformKey, int targetFrameRate, int targetSampleBits, int targetChannels) throws FileStoreException, IOException, NexusException {
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
    } catch (NexusException e) {
      throw new RuntimeException(e);
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

  /**
   Request to load audio from cache
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
}
