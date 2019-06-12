// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl;

import com.google.inject.Guice;
import io.xj.mixer.Mixer;
import io.xj.mixer.MixerConfig;
import io.xj.mixer.MixerFactory;
import io.xj.mixer.MixerModule;
import io.xj.mixer.impl.exception.MixerException;
import io.xj.mixer.impl.exception.SourceException;
import io.xj.mixer.util.InternalResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MixerImplTest {

  private final MixerFactory mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);

  private Mixer testMixer;

  // FUTURE test compression settings
  // FUTURE test compression special getters for ahead/decay frames

  @Before
  public void setUp() throws Exception {
    testMixer = mixerFactory.createMixer(
      new MixerConfig(
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 2, 8, 48000, false),
        Duration.ofSeconds(60)
      )
    );
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = MixerException.class)
  public void Mixer_unsupportedOutputChannel_moreThanStereo() throws Exception {
    try {
      mixerFactory.createMixer(
        new MixerConfig(
          new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
            48000, 32, 4, 8, 48000, false),
          Duration.ofSeconds(60)
        )
      );

    } catch (Exception e) {
      assertTrue(e.getCause().getMessage().contains("more than 2 output audio channels not allowed"));
      throw e;
    }
  }

  @Test(expected = MixerException.class)
  public void Mixer_unsupportedOutputChannel_lessThanMono() throws Exception {
    try {
      mixerFactory.createMixer(
        new MixerConfig(
          new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
            48000, 32, 0, 8, 48000, false),
          Duration.ofSeconds(60)
        )
      );

    } catch (Exception e) {
      assertTrue(e.getCause().getMessage().contains("less than 1 output audio channels not allowed"));
      throw e;
    }
  }

  @Test
  public void put() {
    // FUTURE test: MixerImpl.put(...)
  }

  @Test
  public void loadSource() throws Exception {
    InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav");
    testMixer.loadSource("F32LSB_48kHz_Stereo", new BufferedInputStream(new FileInputStream(internalResource.getFile())));
    assertEquals(1, testMixer.getSourceCount());
  }

  @Test
  public void hasLoadedSource() throws Exception {
    InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav");
    testMixer.loadSource("F32LSB_48kHz_Stereo", new BufferedInputStream(new FileInputStream(internalResource.getFile())));

    assertTrue( testMixer.hasLoadedSource("F32LSB_48kHz_Stereo"));
    assertFalse(testMixer.hasLoadedSource("bonkers"));
  }

  @Test(expected = SourceException.class)
  public void loadSource_failsIfMoreThan2InputChannels() throws Exception {
    try {
      InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_6ch.wav");
      testMixer.loadSource("F32LSB_48kHz_6ch", new BufferedInputStream(new FileInputStream(internalResource.getFile())));

    } catch (Exception e) {
      assertTrue(e.getMessage().contains("more than 2 input audio channels not allowed"));
      throw e;
    }
  }

  @Test
  public void mix() {
    // FUTURE test: MixerImpl.mix(...)
  }

  @Test
  public void setMixCycleDuration() {
    // FUTURE test: MixerImpl.setMixCycleDuration(...)
  }

  @Test
  public void getSourceCount() {
    // FUTURE test: MixerImpl.getSourceCount(...)
  }

  @Test
  public void getPutCount() {
    // FUTURE test: MixerImpl.getPutCount(...)
  }

  @Test
  public void getNowAt() {
    // FUTURE test: MixerImpl.getNowAt(...)
  }

  @Test
  public void getState() {
    // FUTURE test: MixerImpl.getState(...)
  }

  @Test
  public void mixOutToFile() {
    // FUTURE test: MixerImpl.mixToFile(...)
  }

  @Test
  public void setCycleMicros() {
    // FUTURE test: MixerImpl.setCycleMicros(...)
  }

  @Test
  public void getPutReadyCount() {
    // FUTURE test: MixerImpl.getPutReadyCount(...)
  }

  @Test
  public void getPutLiveCount() {
    // FUTURE test: MixerImpl.getPutLiveCount(...)
  }

  @Test
  public void getPutDoneCount() {
    // FUTURE test: MixerImpl.getPutDoneCount(...)
  }

  @Test
  public void getFrameRate() {
    // FUTURE test: MixerImpl.getFrameRate(...)
  }

  @Test
  public void getOutputFormat() {
    // FUTURE test: MixerImpl.getOutputFormat(...)
  }

  @Test
  public void isDebugging() {
    // FUTURE test: MixerImpl.isDebugging(...)
  }

  @Test
  public void setDebugging() {
    // FUTURE test: MixerImpl.setDebugging(...)
  }

}
