// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft;

import io.xj.craft.impl.FoundationCraftImpl;
import io.xj.craft.impl.StructureCraftImpl;
import io.xj.craft.impl.VoiceCraftImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CraftModule extends AbstractModule {

  protected void configure() {
    installCraftFactory();
  }

  private void installCraftFactory() {
    install(new FactoryModuleBuilder()
      .implement(FoundationCraft.class, FoundationCraftImpl.class)
      .implement(StructureCraft.class, StructureCraftImpl.class)
      .implement(VoiceCraft.class, VoiceCraftImpl.class)
      .build(CraftFactory.class));
  }

}
