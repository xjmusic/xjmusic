// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.ship.broadcast;

import io.xj.engine.mixer.AudioFileWriter;
import io.xj.engine.mixer.AudioFileWriterImpl;

import javax.sound.sampled.AudioFormat;

public class BroadcastFactoryImpl implements BroadcastFactory {
  public BroadcastFactoryImpl(
  ) {
  }

  @Override
  public StreamPlayer player(AudioFormat format, int bufferSize) {
    return new StreamPlayerImpl(format, bufferSize);
  }

  @Override
  public AudioFileWriter writer(AudioFormat format) {
    return new AudioFileWriterImpl(format);
  }
}
