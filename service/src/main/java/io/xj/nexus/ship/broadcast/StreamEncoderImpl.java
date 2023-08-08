// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;


import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.FileUtils;
import io.xj.lib.util.StreamLogger;
import io.xj.lib.util.ValueUtils;
import io.xj.nexus.ship.ShipException;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamEncoderImpl implements StreamEncoder {
  static final Logger LOG = LoggerFactory.getLogger(StreamEncoderImpl.class);
  final ConcurrentLinkedQueue<ByteBuffer> queue = new ConcurrentLinkedQueue<>();
  final FileStoreProvider fileStore;
  final PlaylistPublisher playlist;
  final String bucket;
  final String contentTypeSegment;
  final String playlistPath;
  final String tempFilePathPrefix;
  Process ffmpeg;
  final AtomicBoolean running = new AtomicBoolean(true);

  public StreamEncoderImpl(
    String shipKey,
    AudioFormat format,
    FileStoreProvider fileStore,
    PlaylistPublisher playlist,
    int bitrate,
    String streamBucket,
    String shipChunkContentType,
    int chunkTargetDuration,
    String tempFilePathPrefix,
    String shipFFmpegVerbosity,
    String shipChunkAudioEncoder
  ) {
    this.fileStore = fileStore;
    this.playlist = playlist;

    this.bucket = streamBucket;
    this.contentTypeSegment = shipChunkContentType;
    this.tempFilePathPrefix = tempFilePathPrefix;

    String m3u8Key = String.format("%s.m3u8", shipKey);
    playlistPath = String.format("%s%s", tempFilePathPrefix, m3u8Key);

    try {
      ProcessBuilder builder = new ProcessBuilder(List.of(
        "ffmpeg",
        "-v", shipFFmpegVerbosity,
        "-i", "pipe:0",
        "-ac", "2",
        "-c:a", shipChunkAudioEncoder,
        "-b:a", ValueUtils.k(bitrate),
        "-maxrate", ValueUtils.k(bitrate),
        "-minrate", ValueUtils.k(bitrate),
        // HLS
        "-f", "hls",
        "-start_number", "0",
        "-initial_offset", "0",
        "-hls_flags", "delete_segments",
        "-hls_playlist_type", "event",
        "-hls_segment_filename", String.format("%s%s-%%d.%s", tempFilePathPrefix, shipKey, shipChunkAudioEncoder),
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

      while (running.get()) {
        var bytes = queue.poll();
        if (Objects.isNull(bytes)) continue;
        if (!ffmpeg.isAlive()) {
          LOG.error("Exited with code {}", ffmpeg.exitValue());
          running.set(false);
          continue;
        }

        ffmpeg.getOutputStream().write(bytes.array());
        LOG.debug("received {} bytes of audio data", bytes.array().length);
      }
    } catch (IOException e) {
      LOG.error("Failed while streaming bytes to ffmpeg!", e);
    } finally {
      if (Objects.nonNull(ffmpeg)) ffmpeg.destroy();
    }
  }

  @Override
  public void append(byte[] samples) throws ShipException {
    queue.add(ByteBuffer.wrap(samples));

  }

  @Override
  public void publish(long atMillis) throws ShipException {
    if (running.get())
      try {
        // test for existence of playlist file; skip if nonexistent
        if (!new File(playlistPath).exists()) return;

        // parse ffmpeg .m3u8 content
        var chunks = playlist.parseItems(FileUtils.getFileContent(playlistPath));
        for (var chunk : chunks)
          if (playlist.putNext(chunk)) {
            uploadMediaSegment(chunk.getFilename(), contentTypeSegment);
          } else
            LOG.debug("Skipped Chunk[{}]", chunk.getKey());

      } catch (IOException | FileStoreException e) {
        throw new ShipException("Failed to publish media segment!", e);
      }
  }

  @Override
  public void close() {
    if (Objects.nonNull(ffmpeg))
      ffmpeg.destroy();
    running.set(false);
  }

  @Override
  public boolean isHealthy() {
    if (!running.get()) return notHealthy("Not Active!");
    if (!ffmpeg.isAlive()) return notHealthy("FFMPEG is not alive!");
    return true;
  }

  @Override
  public void setPlaylistAtChainMicros(long atChainMicros) {
    playlist.setAtChainMicros(atChainMicros);
  }

  /**
   * Return false after logging a warning message
   *
   * @param message to warn
   * @return false
   */
  boolean notHealthy(String message) {
    LOG.warn(message);
    return false;
  }

  /**
   * Stream a file from temp path to S3
   *
   * @param key         of file (in temp folder and on S3 target)
   * @param contentType content-type
   * @throws FileStoreException on failure
   */
  void uploadMediaSegment(String key, String contentType) throws FileStoreException, ShipException {
    var path = String.format("%s%s", tempFilePathPrefix, key);
    fileStore.putS3ObjectFromTempFile(path, bucket, key, contentType, null);
    LOG.info("Shipped {}/{} ({})", bucket, key, contentType);
    playlist.publish();
  }

}
