// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.mixer;

import io.outright.xj.mixer.impl.exception.MixerException;

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
public class MixerModuleTest {
  private MixerFactory mixerFactory;

  @Before
  public void setUp() {
    mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);
  }

  @After
  public void tearDown() {
    mixerFactory = null;
  }

  @Test
  public void createMixerNotNull() throws Exception {
    Mixer mixer = mixerFactory.createMixer(
      OutputContainer.WAV,
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, false),
      Duration.ofSeconds(60)
    );
    assertNotNull(mixer);
  }

  @Test(expected = MixerException.class)
  public void createMixerNotNull_failsToSupportBigEndian() throws Exception {
    try {
      mixerFactory.createMixer(
        OutputContainer.WAV,
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
