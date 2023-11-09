// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.mixer;


import io.xj.nexus.audio_cache.DubAudioCache;

import java.util.UUID;

public class MixerFactoryImpl implements MixerFactory {
  final EnvelopeProvider envelopeProvider;
  final DubAudioCache dubAudioCache;

  final int MIXER_OUTPUT_PIPE_SIZE = 10000000;

  public MixerFactoryImpl(
    EnvelopeProvider envelopeProvider,
    DubAudioCache dubAudioCache
  ) {
    this.envelopeProvider = envelopeProvider;
    this.dubAudioCache = dubAudioCache;
  }

  @Override
  public Mixer createMixer(MixerConfig mixerConfig) throws MixerException {
    return new MixerImpl(dubAudioCache, mixerConfig, this, envelopeProvider, MIXER_OUTPUT_PIPE_SIZE);
  }

  @Override
  public Put createPut(UUID id, UUID audioId, int bus, long startAtMicros, long stopAtMicros, double velocity, int attackMillis, int releaseMillis) {
    return new PutImpl(id, audioId, bus, attackMillis, releaseMillis, startAtMicros, stopAtMicros, velocity);
  }

  @Override
  public Source createSource(UUID audioId, String absolutePath, String description) {
    return new SourceImpl(audioId, absolutePath, description);
  }
}
