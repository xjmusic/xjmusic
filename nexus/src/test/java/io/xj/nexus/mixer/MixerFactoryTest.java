// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;


import io.xj.nexus.audio_cache.DubAudioCache;
import io.xj.nexus.audio_cache.DubAudioCacheImpl;
import io.xj.nexus.http.HttpClientProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class MixerFactoryTest {
  MixerFactory mixerFactory;

  @Mock
  HttpClientProvider httpClientProvider;

  @BeforeEach
  public void setUp() {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    DubAudioCache dubAudioCache = new DubAudioCacheImpl(httpClientProvider);
    mixerFactory = new MixerFactoryImpl(envelopeProvider, dubAudioCache);
  }

  @Test
  public void createMixerNotNull() throws Exception {
    Mixer mixer = mixerFactory.createMixer(new MixerConfig(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, false)
    ));
    assertNotNull(mixer);
  }

  /**
   [#339] Mix module should support Big-Endian audio files
   */
  @Test
  public void createMixerNotNull_supportBigEndian() throws Exception {
    var mixer = mixerFactory.createMixer(new MixerConfig(
      new AudioFormat(AudioFormat.Encoding.PCM_FLOAT,
        48000, 32, 2, 8, 48000, true)
    ));
    assertNotNull(mixer);
  }

}
