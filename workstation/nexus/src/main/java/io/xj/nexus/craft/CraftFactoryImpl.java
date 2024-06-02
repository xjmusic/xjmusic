// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.craft;

import io.xj.hub.pojos.Program;
import io.xj.nexus.FabricationException;
import io.xj.nexus.craft.background.BackgroundCraft;
import io.xj.nexus.craft.background.BackgroundCraftImpl;
import io.xj.nexus.craft.beat.BeatCraft;
import io.xj.nexus.craft.beat.BeatCraftImpl;
import io.xj.nexus.craft.detail.DetailCraft;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.craft.macro_main.MacroMainCraft;
import io.xj.nexus.craft.macro_main.MacroMainCraftImpl;
import io.xj.nexus.craft.transition.TransitionCraft;
import io.xj.nexus.craft.transition.TransitionCraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import jakarta.annotation.Nullable;

import java.util.Collection;

public class CraftFactoryImpl implements CraftFactory {

  public CraftFactoryImpl() {
  }

  @Override
  public MacroMainCraft macroMain(Fabricator fabricator, @Nullable Program overrideMacroProgram, @Nullable Collection<String> overrideMemes) {
    return new MacroMainCraftImpl(fabricator, overrideMacroProgram, overrideMemes);
  }

  @Override
  public BeatCraft beat(Fabricator fabricator) throws FabricationException {
    return new BeatCraftImpl(fabricator);
  }

  @Override
  public DetailCraft detail(Fabricator fabricator) throws FabricationException {
    return new DetailCraftImpl(fabricator);
  }

  @Override
  public TransitionCraft transition(Fabricator fabricator) throws FabricationException {
    return new TransitionCraftImpl(fabricator);
  }

  @Override
  public BackgroundCraft background(Fabricator fabricator) throws FabricationException {
    return new BackgroundCraftImpl(fabricator);
  }
}
