// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.dub;

import io.humble.video.Codec;
import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.Encoder;
import io.humble.video.MediaAudio;
import io.humble.video.MediaAudioResampler;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.Muxer;
import io.xj.lib.http.HttpClientProvider;
import io.xj.nexus.NexusException;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
  final static Logger LOG = LoggerFactory.getLogger(DubAudioCacheItem.class);
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


  private static int getAudioFrameRate(String inputFile) throws NexusException {
    final Demuxer demuxer = Demuxer.make();
    try {
      demuxer.open(inputFile, null, false, true, null, null);
      int numStreams = demuxer.getNumStreams();
      for (int i = 0; i < numStreams; i++) {
        final Decoder codec = demuxer.getStream(i).getDecoder();
        if (codec != null && codec.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
          return codec.getSampleRate();
        }
      }
    } catch (Exception e) {
      LOG.error("Unable to get audio frame rate from file: {}", inputFile, e);
    } finally {
      if (demuxer != null) {
        try {
          demuxer.close();
        } catch (InterruptedException | IOException e) {
          LOG.error("Unable to close demuxer", e);
        }
      }
    }
    throw new NexusException(String.format("Unable to get audio frame rate from file: %s", inputFile));
  }

  private static void convertAudio(String inputFile, String outputFile, int targetSampleRate) throws NexusException {
    Demuxer demuxer = Demuxer.make();
    Muxer muxer = Muxer.make(outputFile, null, null);
    try {
      demuxer.open(inputFile, null, false, true, null, null);
      var decoder = getAudioStreamDecoder(demuxer);

      @Nullable
      Encoder encoder;

      if (decoder.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
        // Create a resampler
        MediaAudioResampler resampler = MediaAudioResampler.make(decoder.getChannelLayout(), targetSampleRate,
          decoder.getSampleFormat(), decoder.getChannelLayout(), decoder.getSampleRate(),
          decoder.getSampleFormat());

        // Create an encoder for the output
        encoder = Encoder.make(Codec.findEncodingCodec(decoder.getCodecID()));
        encoder.setChannelLayout(decoder.getChannelLayout());
        encoder.setSampleRate(targetSampleRate);
        encoder.setSampleFormat(decoder.getSampleFormat());

        // Open the encoder
        encoder.open(null, null);

        // Add the stream to the muxer
        muxer.addNewStream(encoder);
        muxer.open(null, null);

        // Process the audio, resampling as we go
        final MediaPacket packet = MediaPacket.make();
        while (demuxer.read(packet) >= 0) {
          final MediaAudio output = MediaAudio.make(
            decoder.getFrameSize(),
            decoder.getSampleRate(),
            decoder.getChannels(),
            decoder.getChannelLayout(),
            decoder.getSampleFormat());
          decoder.decodeAudio(output, packet, 0);

          final MediaAudio resampledSamples = MediaAudio.make(output, false);
          resampler.resample(resampledSamples, output);

          do {
            encoder.encode(packet, resampledSamples);
            muxer.write(packet, false);
          } while (packet.isComplete());
        }

        // Finish processing
        do {
          muxer.write(packet, true);
        } while (packet.isComplete());
      }

    } catch (InterruptedException | IOException e) {
      LOG.error("Unable to open demuxer", e);
      throw new NexusException(String.format("Unable to open demuxer: %s", inputFile));
    } finally {
      if (muxer != null) {
        muxer.close();
      }
      try {
        demuxer.close();
      } catch (InterruptedException | IOException e) {
        LOG.error("Unable to close demuxer", e);
      }
    }
  }

  private static Decoder getAudioStreamDecoder(Demuxer demuxer) throws NexusException {
    try {
      int numStreams = demuxer.getNumStreams();
      for (int i = 0; i < numStreams; i++) {
        final Decoder decoder = demuxer.getStream(i).getDecoder();
        if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
          return decoder;
        }
      }
    } catch (InterruptedException | IOException e) {
      LOG.error("Unable to get audio stream decoder", e);
      throw new NexusException("Unable to get audio stream decoder");
    }
    throw new NexusException("Unable to find audio stream in file");
  }

}
