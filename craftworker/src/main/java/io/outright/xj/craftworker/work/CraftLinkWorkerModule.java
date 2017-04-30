// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker.work;

import io.outright.xj.core.work.WorkerOperation;

import com.google.inject.AbstractModule;

public class CraftLinkWorkerModule extends AbstractModule {
  protected void configure() {
    bindWorker();
  }

  private void bindWorker() {
    bind(WorkerOperation.class).to(CraftLinkWorkerOperation.class);
  }

}
