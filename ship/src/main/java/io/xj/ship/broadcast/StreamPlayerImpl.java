// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.mixer.FormatException;
import io.xj.ship.ShipException;
import io.xj.ship.ShipMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.xj.lib.mixer.AudioStreamWriter.byteBufferOf;

public class StreamPlayerImpl implements StreamPlayer {
  private static final Logger LOG = LoggerFactory.getLogger(StreamPlayer.class);
  private final AudioFormat format;
  private final SourceDataLine line;
  private final ConcurrentLinkedQueue<ByteBuffer> queue;
  private final String THREAD_NAME = "playback";
  private volatile boolean active = true;

  @Inject
  public StreamPlayerImpl(
    @Assisted("audioFormat") AudioFormat format,
    Environment env
  ) throws ShipException {
    this.format = format;

    queue = new ConcurrentLinkedQueue<>();

    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
    if (!AudioSystem.isLineSupported(info)) {
      LOG.error("Line matching {} not supported.", info);
      line = null;
      return;
    }

    if (ShipMode.Playback.equals(env.getShipMode()))
      try {
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        LOG.info("Did open audio system line out: {}", info);

        CompletableFuture.supplyAsync(() -> {
          final Thread currentThread = Thread.currentThread();
          final String oldName = currentThread.getName();
          currentThread.setName(THREAD_NAME);
          try {
            while (active) {
              var bytes = queue.poll();
              if (Objects.isNull(bytes)) continue;
              LOG.info("Playing next {} bytes", bytes.array().length);
              line.write(bytes.array(), 0, bytes.array().length);
            }
          } finally {
            currentThread.setName(oldName);
          }
          return false;
        });

      } catch (LineUnavailableException e) {
        throw new ShipException("Failed to initialize!", e);
      }
    else {
      line = null;
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
    line.close();
    active = false;
  }

}
