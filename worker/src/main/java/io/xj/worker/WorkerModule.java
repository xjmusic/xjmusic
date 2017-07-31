// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker;

import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.worker.craft.CraftFactory;
import io.xj.worker.craft.FoundationCraft;
import io.xj.worker.craft.StructureCraft;
import io.xj.worker.craft.VoiceCraft;
import io.xj.worker.craft.impl.FoundationCraftImpl;
import io.xj.worker.craft.impl.StructureCraftImpl;
import io.xj.worker.craft.impl.VoiceCraftImpl;
import io.xj.worker.impl.JobFactoryImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import net.greghaines.jesque.worker.JobFactory;

public class WorkerModule extends AbstractModule {
  protected void configure() {
    bindWorker();
  }

  private void bindWorker() {
    bind(JobFactory.class).to(JobFactoryImpl.class);
  }

}
