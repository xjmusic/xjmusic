// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.mixer.FormatException;
import io.xj.ship.ShipException;
import io.xj.ship.ShipMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.xj.lib.mixer.AudioStreamWriter.byteBufferOf;

public class StreamWriterImpl implements StreamWriter {
  private static final Logger LOG = LoggerFactory.getLogger(StreamWriter.class);
  private static final String THREAD_NAME = StreamWriter.class.getName();
  private final AudioFormat format;
  private final ConcurrentLinkedQueue<ByteBuffer> queue;
  private BufferedWriter outFile;
  private volatile boolean active = true;

  @Inject
  public StreamWriterImpl(
    @Assisted("audioFormat") AudioFormat format,
    Environment env
  ) throws ShipException {
    this.format = format;
    String outFilePath = env.getShipToWavPath();

    queue = new ConcurrentLinkedQueue<>();

    if (ShipMode.WAV.equals(env.getShipMode()))
      try {
        if (Strings.isNullOrEmpty(outFilePath))
          throw new ShipException("Cannot write stream without path to output file!");
        outFile = new BufferedWriter(new FileWriter(outFilePath, true));
        LOG.info("Did open output file path for writing: {}", outFilePath);

        CompletableFuture.supplyAsync(() -> {
          final Thread currentThread = Thread.currentThread();
          final String oldName = currentThread.getName();
          currentThread.setName(THREAD_NAME);
          try {
            while (active) {
              var bytes = queue.poll();
              if (Objects.isNull(bytes)) continue;
              LOG.info("Writing next {} bytes", bytes.array().length);
              this.outFile.write(bytes.asCharBuffer().array(), 0, bytes.array().length);
            }
          } catch (IOException e) {
            LOG.error("Failed to write bytes to output file!", e);

            outFile = null;
            active = false;
          } finally {
            try {
              if (Objects.nonNull(outFile)) outFile.close();
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
      outFile = null;
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
    if (Objects.nonNull(outFile)) {
      try {
        outFile.close();
      } catch (IOException e) {
        LOG.error("Failed to close output file stream!", e);
      }
    }
    active = false;
  }

}
