// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.work.impl.link_work;

import io.outright.xj.core.work.Leader;
import io.outright.xj.core.work.WorkFactory;
import io.outright.xj.core.work.Worker;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LinkWorkFactoryModule extends AbstractModule {
  protected void configure() {
    installWorkFactory();
  }

  private void installWorkFactory() {
    install(new FactoryModuleBuilder()
      .implement(Leader.class, LinkLeaderImpl.class)
      .implement(Worker.class, LinkWorkerImpl.class)
      .build(WorkFactory.class));
  }

}
