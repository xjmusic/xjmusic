// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.dubworker;

import io.outright.xj.core.chain_gang.ChainGangOperation;
import io.outright.xj.dubworker.dub.DubFactory;
import io.outright.xj.dubworker.dub.MasterDub;
import io.outright.xj.dubworker.dub.ShipDub;
import io.outright.xj.dubworker.dub.impl.MasterDubImpl;
import io.outright.xj.dubworker.dub.impl.ShipDubImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DubworkerModule extends AbstractModule {
  protected void configure() {
    bindWorker();
    installDubFactory();
  }

  private void bindWorker() {
    bind(ChainGangOperation.class).to(DubChainGangOperation.class);
  }

  private void installDubFactory() {
    install(new FactoryModuleBuilder()
      .implement(MasterDub.class, MasterDubImpl.class)
      .implement(ShipDub.class, ShipDubImpl.class)
      .build(DubFactory.class));
  }

}
