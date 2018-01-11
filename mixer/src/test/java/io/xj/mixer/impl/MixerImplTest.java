// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl;

import io.xj.mixer.MixerFactory;
import io.xj.mixer.MixerModule;
import io.xj.mixer.Mixer;
import io.xj.mixer.OutputContainer;
import io.xj.mixer.impl.exception.MixerException;
import io.xj.mixer.impl.exception.SourceException;
import io.xj.mixer.util.InternalResource;

import com.google.inject.Guice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MixerImplTest {

  private MixerFactory mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);

  private Mixer testMixer;

  @Before
  public void setUp() throws Exception {
    testMixer = mixerFactory.createMixer(
      OutputContainer.WAV,
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, false),
      Duration.ofSeconds(60)
    );
  }

  @After
  public void tearDown() throws Exception {
    testMixer = null;
  }

  @Test(expected = MixerException.class)
  public void Mixer_unsupportedOutputChannel_moreThanStereo() throws Exception {
    try {
      mixerFactory.createMixer(
        OutputContainer.WAV,
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 4, 8, 48000, false),
        Duration.ofSeconds(60)
      );

    } catch (Exception e) {
      assertTrue(e.getMessage().contains("more than 2 output audio channels not allowed"));
      throw e;
    }
  }

  @Test(expected = MixerException.class)
  public void Mixer_unsupportedOutputChannel_lessThanMono() throws Exception {
    try {
      mixerFactory.createMixer(
        OutputContainer.WAV,
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 0, 8, 48000, false),
        Duration.ofSeconds(60)
      );

    } catch (Exception e) {
      assertTrue(e.getMessage().contains("less than 1 output audio channels not allowed"));
      throw e;
    }
  }

  @Test
  public void put() throws Exception {
    // future test: MixerImpl.put(...)
  }

  @Test
  public void loadSource() throws Exception {
    InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav");
    testMixer.loadSource("F32LSB_48kHz_Stereo", new BufferedInputStream(new FileInputStream(internalResource.getFile())));
    assertEquals(1, testMixer.getSourceCount());
  }

  @Test(expected = SourceException.class)
  public void loadSource_failsIfMoreThan2InputChannels() throws Exception {
    try {
      InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_6ch.wav");
      testMixer.loadSource("F32LSB_48kHz_6ch", new BufferedInputStream(new FileInputStream(internalResource.getFile())));
      assertEquals(1, testMixer.getSourceCount());

    } catch (Exception e) {
      assertTrue(e.getMessage().contains("more than 2 input audio channels not allowed"));
      throw e;
    }
  }

  @Test
  public void mix() throws Exception {
    // future test: MixerImpl.mix(...)
  }

  @Test
  public void setMixCycleDuration() throws Exception {
    // future test: MixerImpl.setMixCycleDuration(...)
  }

  @Test
  public void getSourceCount() throws Exception {
    // future test: MixerImpl.getSourceCount(...)
  }

  @Test
  public void getPutCount() throws Exception {
    // future test: MixerImpl.getPutCount(...)
  }

  @Test
  public void getNowAt() throws Exception {
    // future test: MixerImpl.getNowAt(...)
  }

  @Test
  public void getState() throws Exception {
    // future test: MixerImpl.getState(...)
  }

  @Test
  public void mixOutToFile() throws Exception {
    // future test: MixerImpl.mixToFile(...)
  }

  @Test
  public void setCycleMicros() throws Exception {
    // future test: MixerImpl.setCycleMicros(...)
  }

  @Test
  public void getPutReadyCount() throws Exception {
    // future test: MixerImpl.getPutReadyCount(...)
  }

  @Test
  public void getPutLiveCount() throws Exception {
    // future test: MixerImpl.getPutLiveCount(...)
  }

  @Test
  public void getPutDoneCount() throws Exception {
    // future test: MixerImpl.getPutDoneCount(...)
  }

  @Test
  public void getFrameRate() throws Exception {
    // future test: MixerImpl.getFrameRate(...)
  }

  @Test
  public void getOutputFormat() throws Exception {
    // future test: MixerImpl.getOutputFormat(...)
  }

  @Test
  public void isDebugging() throws Exception {
    // future test: MixerImpl.isDebugging(...)
  }

  @Test
  public void setDebugging() throws Exception {
    // future test: MixerImpl.setDebugging(...)
  }

}
