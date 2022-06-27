// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.notification.NotificationModule;

public class MixerModule extends AbstractModule {

  protected void configure() {
    installMixFactory();
  }

  private void installMixFactory() {
    install(new NotificationModule());
    install(new FactoryModuleBuilder()
      .implement(Mixer.class, MixerImpl.class)
      .implement(Source.class, SourceImpl.class)
      .implement(Put.class, PutImpl.class)
      .build(MixerFactory.class));
  }

}
