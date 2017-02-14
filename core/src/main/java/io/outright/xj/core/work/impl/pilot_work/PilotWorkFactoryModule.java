// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.work.impl.pilot_work;

import io.outright.xj.core.work.Leader;
import io.outright.xj.core.work.WorkFactory;
import io.outright.xj.core.work.Worker;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class PilotWorkFactoryModule extends AbstractModule {
  protected void configure() {
    installWorkFactory();
  }

  private void installWorkFactory() {
    install(new FactoryModuleBuilder()
      .implement(Leader.class, PilotLeaderImpl.class)
      .implement(Worker.class, PilotWorkerImpl.class)
      .build(WorkFactory.class));
  }

}
