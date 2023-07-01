// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Strings;
import io.xj.lib.mixer.FormatException;
import io.xj.lib.util.Files;
import io.xj.lib.util.Text;
import io.xj.ship.ShipException;
import io.xj.ship.ShipMode;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.xj.lib.mixer.AudioStreamWriter.byteBufferOf;
import static java.nio.file.Files.deleteIfExists;

public class StreamWriterImpl implements StreamWriter {
  private static final Logger LOG = LoggerFactory.getLogger(StreamWriterImpl.class);
  private static final String THREAD_NAME = "StreamWriter";
  private final AudioFormat format;
  private final ConcurrentLinkedQueue<ByteBuffer> queue;
  private final long targetByteCount;
  private final String outPath;
  private final String tempPath;
  private final AtomicLong appendedByteCount = new AtomicLong(0);
  private final boolean enabled;
  private FileOutputStream tempOut;
  private final AtomicBoolean active;

  public StreamWriterImpl(
    AudioFormat format,
    @Value("${ship.wav.path}") String shipWavPath,
    @Value("${ship.wav.seconds}") int shipWavSeconds,
    @Value("${ship.wav.mode}") String shipWavMode
  ) {
    this.format = format;
    this.outPath = shipWavPath;
    this.tempPath = Files.getUniqueTempFilename("stream.pcm");
    int outSeconds = shipWavSeconds;
    targetByteCount = (long) (outSeconds * format.getFrameRate() * format.getFrameSize());

    queue = new ConcurrentLinkedQueue<>();

    enabled = ShipMode.WAV.equals(shipWavMode);
    active = new AtomicBoolean(enabled);
    if (active.get())
      try {
        if (Strings.isNullOrEmpty(outPath)) {
          LOG.error("Cannot write stream to WAV without path to output file!");
          active.set(false);
          return;
        } else if (0 >= targetByteCount) {
          LOG.error("Cannot write stream to WAV without specific # of seconds!");
          active.set(false);
          return;
        }

        deleteIfExists(Path.of(tempPath));
        tempOut = new FileOutputStream(tempPath, true);
        LOG.info("Will write {} seconds of PCM data to temp file: {}", outSeconds, tempPath);

        CompletableFuture.supplyAsync(() -> {
          final Thread currentThread = Thread.currentThread();
          final String oldName = currentThread.getName();
          currentThread.setName(THREAD_NAME);
          try {
            while (active.get()) {
              var bytes = queue.poll();
              if (Objects.isNull(bytes)) continue;
              // make sure we append the exact # of samples for the expected # of seconds of WAV output
              if (appendedByteCount.get() + bytes.array().length >= targetByteCount) {
                var lastBytes = bytes.slice(0, (int) (targetByteCount - appendedByteCount.get()));
                this.tempOut.write(lastBytes.array(), 0, lastBytes.array().length);
                tempOut.flush();
                tempOut.close();
                appendedByteCount.addAndGet(lastBytes.array().length);
                LOG.info("Did write last {} bytes, adding up to {} out of {} target bytes ({})",
                  lastBytes.array().length, appendedByteCount.get(), targetByteCount, Text.percentage((float) appendedByteCount.get() / targetByteCount));
                //
                File outputFile = new File(outPath);
                var fileInputStream = FileUtils.openInputStream(new File(tempPath));
                var bufferedInputStream = new BufferedInputStream(fileInputStream);
                AudioInputStream ais = new AudioInputStream(bufferedInputStream, format, targetByteCount);
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
                LOG.info("Did write final output WAV container to {}", outPath);
                active.set(false);
              } else {
                this.tempOut.write(bytes.array(), 0, bytes.array().length);
                appendedByteCount.addAndGet(bytes.array().length);
                LOG.info("Did write next {} bytes, adding up to {} out of {} target bytes ({})",
                  bytes.array().length, appendedByteCount.get(), targetByteCount, Text.percentage((float) appendedByteCount.get() / targetByteCount));
                tempOut.flush();
              }
            }
          } catch (IOException e) {
            LOG.error("Failed to write bytes to output file!", e);

            tempOut = null;
            active.set(false);
          } finally {
            try {
              if (Objects.nonNull(tempOut)) tempOut.close();
            } catch (IOException e) {
              LOG.error("Failed to close output file stream!", e);
            }
            currentThread.setName(oldName);
          }
          return false;
        });

      } catch (IOException e) {
        LOG.error("Failed to initialize!", e);
        active.set(false);
      }
    else {
      tempOut = null;
      active.set(false);
    }
  }

  @Override
  public boolean isActive() {
    return active.get();
  }

  @Override
  public double[][] append(double[][] samples) throws ShipException {
    if (!active.get())
      return samples;

    try {
      queue.add(byteBufferOf(format, samples));
      LOG.debug("Did append {} samples to write-out queue", samples.length);
      return samples;

    } catch (FormatException e) {
      LOG.error("Failed to append data to stream!", e);
      throw new ShipException("Failed to append data to stream!", e);
    }
  }

  @Override
  public void close() {
    if (Objects.nonNull(tempOut)) {
      try {
        tempOut.close();
      } catch (IOException e) {
        LOG.error("Failed to close output file stream!", e);
      }
    }
    active.set(false);
  }

  @Override
  public boolean enabledAndDoneWithOutput() {
    return enabled && !active.get();
  }

}
