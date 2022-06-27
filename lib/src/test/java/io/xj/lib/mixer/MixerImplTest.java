// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.notification.NotificationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sound.sampled.AudioFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MixerImplTest {

  @Mock
  private NotificationProvider mockNotificationProvider;

  private MixerFactory mixerFactory;

  private Mixer testMixer;

  // FUTURE test compression settings
  // FUTURE test compression special getters for ahead/decay frames

  @Before
  public void setUp() throws Exception {
    mixerFactory = Guice.createInjector(Modules.override(new MixerModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(NotificationProvider.class).toInstance(mockNotificationProvider);
      }
    })).getInstance(MixerFactory.class);

    testMixer = mixerFactory.createMixer(
      new MixerConfig(
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 2, 8, 48000, false)
      ));
  }

  @After
  public void tearDown() {
  }

  @Test(expected = MixerException.class)
  public void Mixer_unsupportedOutputChannel_moreThanStereo() throws Exception {
    try {
      mixerFactory.createMixer(
        new MixerConfig(
          new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
            48000, 32, 4, 8, 48000, false)
        ));

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
            48000, 32, 0, 8, 48000, false)
        ));

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
    testMixer.loadSource("F32LSB_48kHz_Stereo", internalResource.getFile().getAbsolutePath(), "test audio");
    assertEquals(1, testMixer.getSourceCount());
  }

  @Test
  public void hasLoadedSource() throws Exception {
    InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav");
    testMixer.loadSource("F32LSB_48kHz_Stereo", internalResource.getFile().getAbsolutePath(), "test audio");

    assertTrue(testMixer.hasLoadedSource("F32LSB_48kHz_Stereo"));
    assertFalse(testMixer.hasLoadedSource("bonkers"));
  }

  @Test
  public void loadSource_failsIfMoreThan2InputChannels() throws Exception {
    InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_6ch.wav");
    testMixer.loadSource("F32LSB_48kHz_6ch", internalResource.getFile().getAbsolutePath(), "test audio");

    verify(mockNotificationProvider).publish(eq("Failure"),eq("Failed to load source for Audio[F32LSB_48kHz_6ch] \"test audio\" because more than 2 input audio channels not allowed"));
  }
}
