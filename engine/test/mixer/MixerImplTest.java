// TODO convert to C++

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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class MixerImplTest {

  MixerFactory mixerFactory;

  Mixer testMixer;

  @Mock
  ProjectManager projectManager;

  // FUTURE test compression settings
  // FUTURE test compression special getters for ahead/decay frames

  @BeforeEach
  public void setUp() throws Exception {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    AudioLoader audioLoader = new AudioLoaderImpl(projectManager);
    AudioCache audioCache = new AudioCacheImpl(projectManager, audioLoader);
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
