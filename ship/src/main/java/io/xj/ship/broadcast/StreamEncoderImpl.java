// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.mixer.FormatException;
import io.xj.lib.util.StreamLogger;
import io.xj.lib.util.Values;
import io.xj.ship.ShipException;
import io.xj.ship.ShipMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.xj.lib.mixer.AudioStreamWriter.byteBufferOf;

public class StreamEncoderImpl implements StreamEncoder {
  private static final Logger LOG = LoggerFactory.getLogger(StreamEncoder.class);
  private static final String THREAD_NAME = "stream-encoder";
  private final AudioFormat format;
  private final ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();
  private final String playlistPath;
  private final int bitrate;
  private final int hlsSegmentSeconds;
  private final int hlsListSize;
  private final M3U8PlaylistManager m3U8PlaylistManager;
  private Process ffmpeg;
  private volatile boolean active = true;

  @Inject
  public StreamEncoderImpl(
    @Assisted("shipKey") String shipKey,
    @Assisted("audioFormat") AudioFormat format,
    Environment env,
    M3U8PlaylistManager m3U8PlaylistManager
  ) {
    this.format = format;
    this.m3U8PlaylistManager = m3U8PlaylistManager;

    bitrate = env.getShipBitrateHigh();
    hlsSegmentSeconds = env.getHlsSegmentSeconds();
    hlsListSize = env.getHlsListSize();
    playlistPath = String.format("%s%s.m3u8", env.getTempFilePathPrefix(), shipKey);

    if (ShipMode.HLS.equals(env.getShipMode()))
      CompletableFuture.runAsync(() -> {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        currentThread.setName(THREAD_NAME);
        try {
          int initialOffset = m3U8PlaylistManager.computeMediaSequence(System.currentTimeMillis());
          ProcessBuilder builder = new ProcessBuilder(List.of(
            "ffmpeg",
            "-v", env.getShipFFmpegVerbosity(),
            "-i", "pipe:0",
            "-f", "hls",
            "-ac", "2",
            "-c:a", env.getShipFfmpegAudioCompressor(),
            "-b:a", Values.k(bitrate),
            "-initial_offset", String.valueOf(initialOffset),
            "-maxrate", Values.k(bitrate),
            "-minrate", Values.k(bitrate),
            "-start_number", String.valueOf(initialOffset),
            "-hls_flags", "delete_segments",
            "-hls_list_size", String.valueOf(hlsListSize),
            "-hls_playlist_type", "event",
            "-hls_segment_filename", String.format("%s%s-%%d.%s", env.getTempFilePathPrefix(), shipKey, env.getShipFfmpegSegmentFilenameExtension()),
            "-hls_time", String.valueOf(hlsSegmentSeconds),
            playlistPath
          ));
          builder.redirectErrorStream(true);
          ffmpeg = builder.start();

          // Start consumer to read the stdout
          CompletableFuture.runAsync(new StreamLogger(LOG, ffmpeg.getInputStream(), "stdout"));

          // Start the audio stream of data to ffmpeg. Write the WAV header once
          try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(new byte[0]), format, 0)) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, ffmpeg.getOutputStream());
          }

          while (active) {
            var bytes = queue.poll();
            if (Objects.isNull(bytes)) continue;
            if (!ffmpeg.isAlive()) {
              LOG.error("Exited with code {}", ffmpeg.exitValue());
              active = false;
              continue;
            }

            ffmpeg.getOutputStream().write(bytes.array());
            LOG.info("received {} bytes of audio data", bytes.array().length);
          }
        } catch (IOException e) {
          LOG.error("Failed while streaming bytes to ffmpeg!", e);
        } finally {
          currentThread.setName(oldName);
          if (Objects.nonNull(ffmpeg)) ffmpeg.destroy();
        }
      });
    else {
      active = false;
    }
  }

  @Override
  public double[][] append(double[][] samples) throws ShipException {
    if (!active)
      return samples;

    try {
      queue.add(byteBufferOf(format, samples));
      return samples;

    } catch (FormatException e) {
      throw new ShipException("Failed to append data to stream!", e);
    }
  }

  @Override
  public void close() {
    if (Objects.nonNull(ffmpeg))
      ffmpeg.destroy();
    active = false;
  }

}
