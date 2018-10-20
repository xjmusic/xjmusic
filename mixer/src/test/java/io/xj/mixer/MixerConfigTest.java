//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.mixer;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;

import java.time.Duration;

import static org.junit.Assert.*;

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
  public void getCompressResolutionFrames() {
    config.setCompressResolutionRate(20.0);
    assertEquals("compute frames based on rate", 2400, config.getCompressResolutionFrames());
  }
}
