// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.dub;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DubModule extends AbstractModule {

  protected void configure() {
    installDubFactory();
  }

  private void installDubFactory() {
    install(new FactoryModuleBuilder()
      .implement(Master.class, MasterImpl.class)
      .implement(Ship.class, ShipImpl.class)
      .build(DubFactory.class));
  }

}
