// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.craft;

import io.xj.nexus.NexusException;
import io.xj.nexus.craft.background.BackgroundCraft;
import io.xj.nexus.craft.background.BackgroundCraftImpl;
import io.xj.nexus.craft.beat.BeatCraft;
import io.xj.nexus.craft.beat.BeatCraftImpl;
import io.xj.nexus.craft.detail.DetailCraft;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.craft.hook.HookCraft;
import io.xj.nexus.craft.hook.HookCraftImpl;
import io.xj.nexus.craft.macro_main.MacroMainCraft;
import io.xj.nexus.craft.macro_main.MacroMainCraftImpl;
import io.xj.nexus.craft.perc_loop.PercLoopCraft;
import io.xj.nexus.craft.perc_loop.PercLoopCraftImpl;
import io.xj.nexus.craft.transition.TransitionCraft;
import io.xj.nexus.craft.transition.TransitionCraftImpl;
import io.xj.nexus.fabricator.Fabricator;

public class CraftFactoryImpl implements CraftFactory {

  public CraftFactoryImpl() {
  }

  @Override
  public BackgroundCraft background(Fabricator fabricator) throws NexusException {
    return new BackgroundCraftImpl(fabricator);
  }

  @Override
  public BeatCraft beat(Fabricator fabricator) throws NexusException {
    return new BeatCraftImpl(fabricator);
  }

  @Override
  public DetailCraft detail(Fabricator fabricator) throws NexusException {
    return new DetailCraftImpl(fabricator);
  }

  @Override
  public HookCraft hook(Fabricator fabricator) throws NexusException {
    return new HookCraftImpl(fabricator);
  }

  @Override
  public MacroMainCraft macroMain(Fabricator fabricator) {
    return new MacroMainCraftImpl(fabricator);
  }

  @Override
  public PercLoopCraft percLoop(Fabricator fabricator) throws NexusException {
    return new PercLoopCraftImpl(fabricator);
  }

  @Override
  public TransitionCraft transition(Fabricator fabricator) throws NexusException {
    return new TransitionCraftImpl(fabricator);
  }
}
