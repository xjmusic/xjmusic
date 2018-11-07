//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.mixer;

import io.xj.mixer.impl.exception.MixerException;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class MixerConfigTest {
  MixerConfig config;

  @Before
  public void setUp() {
    config = new MixerConfig(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, true),
      Duration.ofSeconds(60));
  }

  @Test
  public void getCompressAheadFrames() {
    config.setCompressAheadSeconds(1.5);
    assertEquals("compute frames based on seconds", 72000, config.getCompressAheadFrames());
  }

  @Test
  public void getCompressDecayFrames() {
    config.setCompressDecaySeconds(2.0);
    assertEquals("compute frames based on seconds", 96000, config.getCompressDecayFrames());
  }

  @Test
  public void getDSPBufferSize() throws MixerException {
    config.setDSPBufferSize(1024);
    assertEquals("frames per cycle of compressor computation", Integer.valueOf(1024), config.getDSPBufferSize());
  }

  @Test(expected =  MixerException.class)
  public void getDSPBufferSize_failsNotPowerOfTwo() throws MixerException {
    config.setDSPBufferSize(1023);
  }
}
