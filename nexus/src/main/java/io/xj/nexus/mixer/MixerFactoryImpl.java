// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.mixer;


import io.xj.nexus.audio_cache.AudioCache;

public class MixerFactoryImpl implements MixerFactory {
  final EnvelopeProvider envelopeProvider;
  final AudioCache audioCache;

  final int MIXER_OUTPUT_PIPE_SIZE = 10000000;

  public MixerFactoryImpl(
    EnvelopeProvider envelopeProvider,
    AudioCache audioCache
  ) {
    this.envelopeProvider = envelopeProvider;
    this.audioCache = audioCache;
  }

  @Override
  public Mixer createMixer(MixerConfig mixerConfig) throws MixerException {
    return new MixerImpl(audioCache, mixerConfig, this, envelopeProvider, MIXER_OUTPUT_PIPE_SIZE);
  }
}