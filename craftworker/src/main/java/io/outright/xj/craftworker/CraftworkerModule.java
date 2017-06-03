// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker;

import io.outright.xj.core.chain_gang.ChainGangOperation;
import io.outright.xj.craftworker.craft.CraftFactory;
import io.outright.xj.craftworker.craft.FoundationCraft;
import io.outright.xj.craftworker.craft.StructureCraft;
import io.outright.xj.craftworker.craft.VoiceCraft;
import io.outright.xj.craftworker.craft.impl.FoundationCraftImpl;
import io.outright.xj.craftworker.craft.impl.StructureCraftImpl;
import io.outright.xj.craftworker.craft.impl.VoiceCraftImpl;

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
