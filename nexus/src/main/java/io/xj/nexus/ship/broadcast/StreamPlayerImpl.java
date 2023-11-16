// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamPlayerImpl implements StreamPlayer {
  static final Logger LOG = LoggerFactory.getLogger(StreamPlayer.class);
  SourceDataLine line;
  final AtomicBoolean running = new AtomicBoolean(true);

  public StreamPlayerImpl(
    AudioFormat format
  ) {
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

    } catch (LineUnavailableException e) {
      LOG.error("Failed to initialize!", e);
      running.set(false);
    }
  }

  @Override
  public byte[] append(byte[] samples) {
    if (!running.get())
      return samples;

    line.write(samples, 0, samples.length);
    return samples;

  }

  @Override
  public void finish() {
    if (Objects.nonNull(line))
      line.close();
    running.set(false);
  }

  @Override
  public long getHeardAtChainMicros() {
    return line.getMicrosecondPosition();
  }

}
