// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamPlayerImpl implements StreamPlayer {
  static final Logger LOG = LoggerFactory.getLogger(StreamPlayer.class);
  static final String THREAD_NAME = "StreamPlayer";
  SourceDataLine line;
  final ConcurrentLinkedQueue<ByteBuffer> queue;
  final AtomicBoolean running = new AtomicBoolean(true);

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
  public void finish() {
    if (Objects.nonNull(line))
      line.close();
    running.set(false);
  }

}
