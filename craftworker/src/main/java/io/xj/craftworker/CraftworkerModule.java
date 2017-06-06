// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craftworker;

import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.craftworker.craft.CraftFactory;
import io.xj.craftworker.craft.FoundationCraft;
import io.xj.craftworker.craft.impl.VoiceCraftImpl;
import io.xj.craftworker.craft.StructureCraft;
import io.xj.craftworker.craft.VoiceCraft;
import io.xj.craftworker.craft.impl.FoundationCraftImpl;
import io.xj.craftworker.craft.impl.StructureCraftImpl;

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
