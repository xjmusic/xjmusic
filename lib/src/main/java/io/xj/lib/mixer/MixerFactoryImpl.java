// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.mixer;


import java.util.UUID;

public class MixerFactoryImpl implements MixerFactory {
  final EnvelopeProvider envelopeProvider;

  final int MIXER_OUTPUT_PIPE_SIZE = 10000000;

  public MixerFactoryImpl(
    EnvelopeProvider envelopeProvider
  ) {
    this.envelopeProvider = envelopeProvider;
  }

  @Override
  public Mixer createMixer(MixerConfig mixerConfig) throws MixerException {
    return new MixerImpl(mixerConfig, this, envelopeProvider, MIXER_OUTPUT_PIPE_SIZE);
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
