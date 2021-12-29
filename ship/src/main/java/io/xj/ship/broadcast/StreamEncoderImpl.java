// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.FormatException;
import io.xj.lib.util.Files;
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
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.xj.lib.mixer.AudioStreamWriter.byteBufferOf;

public class StreamEncoderImpl implements StreamEncoder {
  private static final Logger LOG = LoggerFactory.getLogger(StreamEncoderImpl.class);
  private static final String THREAD_NAME = "stream-encoder";
  private final AudioFormat format;
  private final ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();
  private final FileStoreProvider fileStore;
  private final PlaylistPublisher playlist;
  private final String bucket;
  private final String contentTypeSegment;
  private final String playlistPath;
  private final String tempFilePathPrefix;
  private final int bitrate;
  private final int playlistTargetSize;
  private final int chunkTargetDuration;
  private int initialSeqNum;
  private Process ffmpeg;
  private volatile boolean active = true;

  @Inject
  public StreamEncoderImpl(
    @Assisted("shipKey") String shipKey,
    @Assisted("audioFormat") AudioFormat format,
    Environment env,
    FileStoreProvider fileStore,
    PlaylistPublisher playlist
  ) {
    this.format = format;
    this.fileStore = fileStore;
    this.playlist = playlist;

    bitrate = env.getShipBitrateHigh();
    bucket = env.getStreamBucket();
    contentTypeSegment = env.getShipChunkContentType();
    playlistTargetSize = env.getShipPlaylistTargetSize();
    chunkTargetDuration = env.getShipChunkTargetDuration();
    tempFilePathPrefix = env.getTempFilePathPrefix();

    String m3u8Key = String.format("%s.m3u8", shipKey);
    playlistPath = String.format("%s%s", tempFilePathPrefix, m3u8Key);

    if (ShipMode.HLS.equals(env.getShipMode()))
      CompletableFuture.runAsync(() -> {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        currentThread.setName(THREAD_NAME);
        try {
          initialSeqNum = playlist.computeMediaSequence(System.currentTimeMillis());
          ProcessBuilder builder = new ProcessBuilder(List.of(
            "ffmpeg",
            "-v", env.getShipFFmpegVerbosity(),
            "-i", "pipe:0",
            "-ac", "2",
            "-c:a", env.getShipChunkAudioEncoder(),
            "-b:a", Values.k(bitrate),
            "-maxrate", Values.k(bitrate),
            "-minrate", Values.k(bitrate),
            // HLS
            "-f", "hls",
            "-start_number", String.valueOf(initialSeqNum),
            "-initial_offset", String.valueOf(initialSeqNum),
            "-hls_flags", "delete_segments",
            "-hls_list_size", String.valueOf(playlistTargetSize),
            "-hls_playlist_type", "event",
            "-hls_segment_filename", String.format("%s%s-%%d.%s", env.getTempFilePathPrefix(), shipKey, env.getShipChunkAudioEncoder()),
            "-hls_time", String.valueOf(chunkTargetDuration),
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
            LOG.debug("received {} bytes of audio data", bytes.array().length);
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
  public void publish(long atMillis) throws ShipException {
    try {
      // test for existence of playlist file; skip if nonexistent
      if (!new File(playlistPath).exists()) return;

      // parse ffmpeg .m3u8 content into playlist manager
      var added = playlist.parseAndLoadItems(Files.getFileContent(playlistPath));

      // publish new filenames
      for (Chunk item : added)
        // skip the first generated media segment; it begins with priming samples
        if (item.getSequenceNumber() > initialSeqNum)
          uploadMediaSegment(item.getFilename(), contentTypeSegment);

    } catch (IOException | FileStoreException e) {
      throw new ShipException("Failed to publish media segment!", e);
    }
  }

  @Override
  public void close() {
    if (Objects.nonNull(ffmpeg))
      ffmpeg.destroy();
    active = false;
  }

  @Override
  public boolean isHealthy() {
    if (!active) return notHealthy("Not Active!");
    if (!ffmpeg.isAlive()) return notHealthy("FFMPEG is not alive!");
    return true;
  }

  /**
   Return false after logging a warning message

   @param message to warn
   @return false
   */
  private boolean notHealthy(String message) {
    LOG.warn(message);
    return false;
  }

  /**
   Stream a file from temp path to S3

   @param key         of file (in temp folder and on S3 target)
   @param contentType content-type
   @throws FileStoreException on failure
   */
  private void uploadMediaSegment(String key, String contentType) throws FileStoreException, ShipException {
    var path = String.format("%s%s", tempFilePathPrefix, key);
    fileStore.putS3ObjectFromTempFile(path, bucket, key, contentType);
    LOG.info("Shipped {}/{} ({})", bucket, key, contentType);
    playlist.publish();
  }

}
