// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.mix;

import io.outright.xj.mix.impl.exception.MixerException;

import com.google.inject.Guice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class MixModuleTest {
  private MixFactory mixFactory;

  @Before
  public void setUp() {
    mixFactory = Guice.createInjector(new MixModule()).getInstance(MixFactory.class);
  }

  @After
  public void tearDown() {
    mixFactory = null;
  }

  @Test
  public void createMixerNotNull() throws Exception {
    Mixer mixer = mixFactory.createMixer(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, false),
      Duration.ofSeconds(60)
    );
    assertNotNull(mixer);
  }

  @Test(expected = MixerException.class)
  public void createMixerNotNull_failsToSupportBigEndian() throws Exception {
    try {
      mixFactory.createMixer(
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 2, 8, 48000, true),
        Duration.ofSeconds(60)
      );

    } catch (Exception e) {
      assertTrue(e.getMessage().contains("big-endian"));
      assertTrue(e.getMessage().contains("unsupported"));
      throw e;
    }
  }

}
