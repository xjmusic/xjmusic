// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;


import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.InternalResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sound.sampled.AudioFormat;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MixerImplTest {
  @Mock
  NotificationProvider notificationProvider;

  MixerFactory mixerFactory;

  Mixer testMixer;


  // FUTURE test compression settings
  // FUTURE test compression special getters for ahead/decay frames

  @Before
  public void setUp() throws Exception {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    mixerFactory = new MixerFactoryImpl(envelopeProvider, notificationProvider, "production", 1000000);
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
    testMixer.loadSource(UUID.randomUUID(), internalResource.getFile().getAbsolutePath(), "test audio");
    assertEquals(1, testMixer.getSourceCount());
  }

  @Test
  public void hasLoadedSource() throws Exception {
    InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav");
    var audioId = UUID.randomUUID();
    testMixer.loadSource(audioId, internalResource.getFile().getAbsolutePath(), "test audio");

    assertTrue(testMixer.hasLoadedSource(audioId));
    assertFalse(testMixer.hasLoadedSource(UUID.randomUUID()));
  }

  @Test
  public void loadSource_failsIfMoreThan2InputChannels() throws Exception {
    InternalResource internalResource = new InternalResource("test_audio/F32LSB_48kHz_6ch.wav");
    var audioId = UUID.randomUUID();
    testMixer.loadSource(audioId, internalResource.getFile().getAbsolutePath(), "test audio");

    verify(notificationProvider).publish(eq("Production-Chain Mix Source Failure"), eq("Failed to load source for Audio[" + audioId + "] \"test audio\" because more than 2 input audio channels not allowed"));
  }
}
