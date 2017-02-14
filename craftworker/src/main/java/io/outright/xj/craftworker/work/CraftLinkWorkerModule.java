// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker.work;

import io.outright.xj.core.work.Leader;
import io.outright.xj.core.work.WorkFactory;
import io.outright.xj.core.work.Worker;
import io.outright.xj.core.work.WorkerOperation;
import io.outright.xj.core.work.impl.link_work.LinkLeaderImpl;
import io.outright.xj.core.work.impl.link_work.LinkWorkerImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CraftLinkWorkerModule extends AbstractModule {
  protected void configure() {
    bindWorker();
  }

  private void bindWorker() {
    bind(WorkerOperation.class).to(CraftLinkWorkerOperation.class);
  }

}
