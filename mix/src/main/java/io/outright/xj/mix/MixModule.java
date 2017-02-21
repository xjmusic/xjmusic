// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.mix;

import io.outright.xj.mix.impl.MixerImpl;
import io.outright.xj.mix.impl.PutImpl;
import io.outright.xj.mix.impl.SourceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class MixModule extends AbstractModule {

  protected void configure() {
    installMixFactory();
  }

  private void installMixFactory() {
    install(new FactoryModuleBuilder()
      .implement(Mixer.class, MixerImpl.class)
      .implement(Source.class, SourceImpl.class)
      .implement(Put.class, PutImpl.class)
      .build(MixFactory.class));
  }

}
