// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker.erase;

import io.xj.worker.erase.impl.AudioEraseWorkerImpl;
import io.xj.worker.erase.impl.ChainEraseWorkerImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class EraseworkerModule extends AbstractModule {
  protected void configure() {
    installEraseFactory();
  }

  private void installEraseFactory() {
    install(new FactoryModuleBuilder()
      .implement(AudioEraseWorker.class, AudioEraseWorkerImpl.class)
      .implement(ChainEraseWorker.class, ChainEraseWorkerImpl.class)
      .build(EraseWorkerFactory.class));
  }

}
