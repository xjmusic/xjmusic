// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.mixer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MixerConfigTest {
  MixerConfig config;

  @BeforeEach
  public void setUp() {
    config = new MixerConfig(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, true));
  }

  @Test
  public void getCompressAheadFrames() {
    assertEquals(72000, config.setCompressAheadSeconds(1.5).getCompressAheadFrames());
  }

  @Test
  public void getCompressDecayFrames() {
    assertEquals(96000, config.setCompressDecaySeconds(2.0).getCompressDecayFrames());
  }

  @Test
  public void getDSPBufferSize() throws MixerException {
    assertEquals(Integer.valueOf(1024), config.setDSPBufferSize(1024).getDSPBufferSize());
  }

  @Test
  public void getDSPBufferSize_failsNotPowerOfTwo() {
    var e = assertThrows(MixerException.class, () -> config.setDSPBufferSize(1023));

    assertEquals("Compressor resolution frames must be a power of 2", e.getMessage());
  }

  @Test
  public void setLogPrefix() {
    assertEquals("test", config.setLogPrefix("test").getLogPrefix());
  }
}
