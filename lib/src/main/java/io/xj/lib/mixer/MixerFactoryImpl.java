package io.xj.lib.mixer;

import io.xj.lib.notification.NotificationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MixerFactoryImpl implements MixerFactory {
  private final EnvelopeProvider envelopeProvider;
  private final NotificationProvider notification;
  private final String environment;

  @Autowired
  public MixerFactoryImpl(
    EnvelopeProvider envelopeProvider,
    NotificationProvider notification,
    @Value("${environment}") String environment
  ) {
    this.envelopeProvider = envelopeProvider;
    this.notification = notification;
    this.environment = environment;
  }

  @Override
  public Mixer createMixer(MixerConfig mixerConfig) throws MixerException {
    return new MixerImpl(mixerConfig, this, envelopeProvider);
  }

  @Override
  public Put createPut(int bus, String sourceId, long startAtMicros, long stopAtMicros, double velocity, int attackMillis, int releaseMillis) {
    return new PutImpl(bus, attackMillis, releaseMillis, sourceId, startAtMicros, stopAtMicros, velocity);
  }

  @Override
  public Source createSource(String sourceId, String absolutePath, String description) {
    return new SourceImpl(notification, sourceId, absolutePath, description, environment);
  }
}
