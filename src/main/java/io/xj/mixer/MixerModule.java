// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class MixerModule extends AbstractModule {

  protected void configure() {
    installMixFactory();
  }

  private void installMixFactory() {
    install(new FactoryModuleBuilder()
      .implement(Mixer.class, MixerImpl.class)
      .implement(Source.class, SourceImpl.class)
      .implement(Put.class, PutImpl.class)
      .build(MixerFactory.class));
  }

}
