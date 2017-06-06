// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.eraseworker;

import io.xj.eraseworker.erase.AudioEraseWorker;
import io.xj.eraseworker.erase.ChainEraseWorker;
import io.xj.eraseworker.erase.EraseWorkerFactory;
import io.xj.eraseworker.erase.impl.AudioEraseWorkerImpl;
import io.xj.eraseworker.erase.impl.ChainEraseWorkerImpl;

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
