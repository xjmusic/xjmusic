// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

import io.xj.mixer.impl.MixerImpl;
import io.xj.mixer.impl.PutImpl;
import io.xj.mixer.impl.SourceImpl;

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
