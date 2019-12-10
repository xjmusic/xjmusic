// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub;

import io.xj.dub.master.MasterDubImpl;
import io.xj.dub.ship.ShipDubImpl;
import io.xj.dub.master.MasterDub;
import io.xj.dub.ship.ShipDub;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DubModule extends AbstractModule {

  protected void configure() {
    installDubFactory();
  }

  private void installDubFactory() {
    install(new FactoryModuleBuilder()
      .implement(MasterDub.class, MasterDubImpl.class)
      .implement(ShipDub.class, ShipDubImpl.class)
      .build(DubFactory.class));
  }

}
