// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;


import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.audio_cache.AudioCacheImpl;
import io.xj.nexus.http.HttpClientProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MixerImplTest {

  MixerFactory mixerFactory;

  Mixer testMixer;

  @Mock
  HttpClientProvider httpClientProvider;

  // FUTURE test compression settings
  // FUTURE test compression special getters for ahead/decay frames

  @BeforeEach
  public void setUp() throws Exception {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    AudioCache audioCache = new AudioCacheImpl(projectManager);
    mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
    testMixer = mixerFactory.createMixer(
      new MixerConfig(
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 2, 8, 48000, false)
      ));
  }

  @Test
  public void Mixer_unsupportedOutputChannel_moreThanStereo() {
    var e = assertThrows(MixerException.class, () -> mixerFactory.createMixer(
      new MixerConfig(
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 4, 8, 48000, false)
      )));

    assertTrue(e.getCause().getMessage().contains("more than 2 output audio channels not allowed"));
  }

  @Test
  public void Mixer_unsupportedOutputChannel_lessThanMono() {
    var e = assertThrows(MixerException.class, () -> mixerFactory.createMixer(
      new MixerConfig(
        new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
          48000, 32, 0, 8, 48000, false)
      )));

    assertTrue(e.getCause().getMessage().contains("less than 1 output audio channels not allowed"));
  }
}
