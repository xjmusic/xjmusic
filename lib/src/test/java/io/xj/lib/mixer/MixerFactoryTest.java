// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class MixerFactoryTest {
  MixerFactory mixerFactory;

  @BeforeEach
  public void setUp() {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    mixerFactory = new MixerFactoryImpl(envelopeProvider, 1000000);
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
