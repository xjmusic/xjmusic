// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.nexus.craft.background.BackgroundCraft;
import io.xj.nexus.craft.background.BackgroundCraftImpl;
import io.xj.nexus.craft.detail.DetailCraft;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.craft.hook.HookCraft;
import io.xj.nexus.craft.hook.HookCraftImpl;
import io.xj.nexus.craft.macro_main.MacroMainCraft;
import io.xj.nexus.craft.macro_main.MacroMainCraftImpl;
import io.xj.nexus.craft.perc_loop.PercLoopCraft;
import io.xj.nexus.craft.perc_loop.PercLoopCraftImpl;
import io.xj.nexus.craft.beat.BeatCraft;
import io.xj.nexus.craft.beat.BeatCraftImpl;
import io.xj.nexus.craft.transition.TransitionCraft;
import io.xj.nexus.craft.transition.TransitionCraftImpl;

public class CraftModule extends AbstractModule {

  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(BackgroundCraft.class, BackgroundCraftImpl.class)
      .implement(BeatCraft.class, BeatCraftImpl.class)
      .implement(DetailCraft.class, DetailCraftImpl.class)
      .implement(HookCraft.class, HookCraftImpl.class)
      .implement(MacroMainCraft.class, MacroMainCraftImpl.class)
      .implement(PercLoopCraft.class, PercLoopCraftImpl.class)
      .implement(TransitionCraft.class, TransitionCraftImpl.class)
      .build(CraftFactory.class));
  }
}
