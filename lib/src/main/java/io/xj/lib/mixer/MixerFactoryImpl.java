package io.xj.lib.mixer;

import io.xj.lib.notification.NotificationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MixerFactoryImpl implements MixerFactory {
  private final EnvelopeProvider envelopeProvider;
  private final NotificationProvider notification;
  private final String environment;

  private int mixerOutputPipeSize;

  @Autowired
  public MixerFactoryImpl(
    EnvelopeProvider envelopeProvider,
    NotificationProvider notification,
    @Value("${environment}") String environment,
    @Value("${mixer.output.pipe.size}") int mixerOutputPipeSize
  ) {
    this.envelopeProvider = envelopeProvider;
    this.notification = notification;
    this.environment = environment;
    this.mixerOutputPipeSize = mixerOutputPipeSize;
  }

  @Override
  public Mixer createMixer(MixerConfig mixerConfig) throws MixerException {
    return new MixerImpl(mixerConfig, this, envelopeProvider, mixerOutputPipeSize);
  }

  @Override
  public Put createPut(UUID id, UUID audioId, int bus, long startAtMicros, long stopAtMicros, double velocity, int attackMillis, int releaseMillis) {
    return new PutImpl(id, audioId, bus, attackMillis, releaseMillis, startAtMicros, stopAtMicros, velocity);
  }

  @Override
  public Source createSource(UUID audioId, String absolutePath, String description) {
    return new SourceImpl(notification, audioId, absolutePath, description, environment);
  }
}
