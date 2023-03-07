package io.xj.lib.mixer;

import io.xj.lib.app.AppEnvironment;
import io.xj.lib.notification.NotificationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MixerFactoryImpl implements MixerFactory {
  private final AppEnvironment env;
  private final EnvelopeProvider envelopeProvider;
  private final NotificationProvider notification;

  @Autowired
  public MixerFactoryImpl(
    AppEnvironment env,
    EnvelopeProvider envelopeProvider,
    NotificationProvider notification
  ) {
    this.env = env;
    this.envelopeProvider = envelopeProvider;
    this.notification = notification;
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
    return new SourceImpl(env, notification, sourceId, absolutePath, description);
  }
}
