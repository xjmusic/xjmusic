// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.mixer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;

import static org.junit.Assert.assertEquals;

public class MixerConfigTest {
  MixerConfig config;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Before
  public void setUp() {
    config = new MixerConfig(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, true),
      Duration.ofSeconds(60));
  }

  @Test
  public void getCompressAheadFrames() {
    assertEquals("compute frames based on seconds", 72000, config.setCompressAheadSeconds(1.5).getCompressAheadFrames());
  }

  @Test
  public void getCompressDecayFrames() {
    assertEquals("compute frames based on seconds", 96000, config.setCompressDecaySeconds(2.0).getCompressDecayFrames());
  }

  @Test
  public void getDSPBufferSize() throws MixerException {
    assertEquals("frames per cycle of compressor computation", Integer.valueOf(1024), config.setDSPBufferSize(1024).getDSPBufferSize());
  }

  @Test
  public void getDSPBufferSize_failsNotPowerOfTwo() throws MixerException {
    failure.expect(MixerException.class);
    failure.expectMessage("Compressor resolution frames must be a power of 2");

    config.setDSPBufferSize(1023);
  }

  @Test
  public void setLogPrefix() {
    assertEquals("test", config.setLogPrefix("test").getLogPrefix());
  }
}
