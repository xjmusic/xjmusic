// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.ship.broadcast;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamPlayerImpl implements StreamPlayer {
  private static final Logger LOG = LoggerFactory.getLogger(StreamPlayer.class);
  private static final String THREAD_NAME = "StreamPlayer";
  private SourceDataLine line;
  private final ConcurrentLinkedQueue<ByteBuffer> queue;
  private final AtomicBoolean running = new AtomicBoolean(true);

  public StreamPlayerImpl(
    AudioFormat format
  ) {

    queue = new ConcurrentLinkedQueue<>();

    try {
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      if (!AudioSystem.isLineSupported(info)) {
        LOG.error("Line matching {} not supported.", info);
        line = null;
        return;
      }

      line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();
      LOG.info("Did open audio system line out: {}", info);

      CompletableFuture.supplyAsync(() -> {
        final Thread currentThread = Thread.currentThread();
        final String oldName = currentThread.getName();
        currentThread.setName(THREAD_NAME);
        try {
          while (running.get()) {
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
      LOG.error("Failed to initialize!", e);
      running.set(false);
    }
  }

  @Override
  public byte[] append(byte[] samples) {
    if (!running.get())
      return samples;

    queue.add(ByteBuffer.wrap(samples));
    return samples;

  }

  @Override
  public void close() {
    if (Objects.nonNull(line))
      line.close();
    running.set(false);
  }

}
