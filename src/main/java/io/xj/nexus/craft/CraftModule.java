// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.nexus.craft.detail.DetailCraft;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.craft.macro_main.MacroMainCraft;
import io.xj.nexus.craft.macro_main.MacroMainCraftImpl;
import io.xj.nexus.craft.rhythm.RhythmCraft;
import io.xj.nexus.craft.rhythm.RhythmCraftImpl;

public class CraftModule extends AbstractModule {

  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(MacroMainCraft.class, MacroMainCraftImpl.class)
      .implement(RhythmCraft.class, RhythmCraftImpl.class)
      .implement(DetailCraft.class, DetailCraftImpl.class)
      .build(CraftFactory.class));
  }
}
