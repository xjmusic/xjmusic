// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker.craft;

import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.worker.CraftChainGangOperation;
import io.xj.worker.craft.impl.VoiceCraftImpl;
import io.xj.worker.craft.impl.FoundationCraftImpl;
import io.xj.worker.craft.impl.StructureCraftImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CraftworkerModule extends AbstractModule {
  protected void configure() {
    bindWorker();
    installCraftFactory();
  }

  private void bindWorker() {
    bind(ChainGangOperation.class).to(CraftChainGangOperation.class);
  }

  private void installCraftFactory() {
    install(new FactoryModuleBuilder()
      .implement(FoundationCraft.class, FoundationCraftImpl.class)
      .implement(StructureCraft.class, StructureCraftImpl.class)
      .implement(VoiceCraft.class, VoiceCraftImpl.class)
      .build(CraftFactory.class));
  }


}
