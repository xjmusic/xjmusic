// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.mixer.FormatException;
import io.xj.lib.util.Files;
import io.xj.ship.ShipException;
import io.xj.ship.ShipMode;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private volatile boolean active;

  @Inject
  public StreamWriterImpl(
    @Assisted("audioFormat") AudioFormat format,
    Environment env
  ) throws ShipException {
    this.format = format;
    outPath = env.getShipWavPath();
    tempPath = Files.getUniqueTempFilename("stream.wav");
    int outSeconds = env.getShipWavSeconds();
    targetByteCount = (long) (outSeconds * format.getFrameRate() * format.getFrameSize());

    queue = new ConcurrentLinkedQueue<>();

    enabled = ShipMode.WAV.equals(env.getShipMode());
    active = enabled;
    if (active)
      try {
        if (Strings.isNullOrEmpty(outPath))
          throw new ShipException("Cannot write stream to WAV without path to output file!");
        if (0 >= targetByteCount)
          throw new ShipException("Cannot write stream to WAV without specific # of seconds!");

        deleteIfExists(Path.of(tempPath));
        tempOut = new FileOutputStream(tempPath, true);
        LOG.info("Will write {} seconds of PCM data to temp file: {}", outSeconds, tempPath);

        CompletableFuture.supplyAsync(() -> {
          final Thread currentThread = Thread.currentThread();
          final String oldName = currentThread.getName();
          currentThread.setName(THREAD_NAME);
          try {
            while (active) {
              var bytes = queue.poll();
              if (Objects.isNull(bytes)) continue;
              // make sure we append the exact # of samples for the expected # of seconds of WAV output
              if (appendedByteCount.get() + bytes.array().length >= targetByteCount) {
                var lastBytes = bytes.slice(0, (int) (targetByteCount - appendedByteCount.get()));
                this.tempOut.write(lastBytes.array(), 0, lastBytes.array().length);
                tempOut.flush();
                tempOut.close();
                appendedByteCount.addAndGet(lastBytes.array().length);
                LOG.info("Did write last {} bytes, adding up to {} out of {} target bytes", lastBytes.array().length, appendedByteCount.get(), targetByteCount);
                //
                File outputFile = new File(outPath);
                var fileInputStream = FileUtils.openInputStream(new File(tempPath));
                var bufferedInputStream = new BufferedInputStream(fileInputStream);
                AudioInputStream ais = new AudioInputStream(bufferedInputStream, format, targetByteCount);
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
                LOG.info("Did write final output WAV container to {}", outPath);
                active = false;
              } else {
                this.tempOut.write(bytes.array(), 0, bytes.array().length);
                appendedByteCount.addAndGet(bytes.array().length);
                LOG.info("Did write next {} bytes, adding up to {} out of {} target bytes", bytes.array().length, appendedByteCount.get(), targetByteCount);
                tempOut.flush();
              }
            }
          } catch (IOException e) {
            LOG.error("Failed to write bytes to output file!", e);

            tempOut = null;
            active = false;
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
        throw new ShipException("Failed to initialize!", e);
      }
    else {
      tempOut = null;
      active = false;
    }
  }

  @Override
  public double[][] append(double[][] samples) throws ShipException {
    if (!active)
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
    active = false;
  }

  @Override
  public boolean enabledAndDoneWithOutput() {
    return enabled && !active;
  }

}
