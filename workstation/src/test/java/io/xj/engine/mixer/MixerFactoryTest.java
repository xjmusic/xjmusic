// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.mixer;


import io.xj.engine.audio.AudioCache;
import io.xj.engine.audio.AudioCacheImpl;
import io.xj.engine.audio.AudioLoader;
import io.xj.engine.audio.AudioLoaderImpl;
import io.xj.gui.project.ProjectManager;
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
  ProjectManager projectManager;

  @BeforeEach
  public void setUp() {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    AudioLoader audioLoader = new AudioLoaderImpl(projectManager);
    AudioCache audioCache = new AudioCacheImpl(projectManager, audioLoader);
    mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
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
