// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.mixer;

import io.outright.xj.mixer.impl.MixerImpl;
import io.outright.xj.mixer.impl.PutImpl;
import io.outright.xj.mixer.impl.SourceImpl;

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
