// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import io.xj.nexus.mixer.AudioFileWriter;
import io.xj.nexus.mixer.AudioFileWriterImpl;

import javax.sound.sampled.AudioFormat;

public class BroadcastFactoryImpl implements BroadcastFactory {
  public BroadcastFactoryImpl(
  ) {
  }

  @Override
  public StreamPlayer player(AudioFormat format) {
    return new StreamPlayerImpl(format);
  }

  @Override
  public AudioFileWriter writer(AudioFormat format) {
    return new AudioFileWriterImpl(format);
  }
}
