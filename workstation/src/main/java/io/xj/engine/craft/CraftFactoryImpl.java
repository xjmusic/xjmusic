// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.craft;

import io.xj.model.pojos.Program;
import io.xj.engine.FabricationException;
import io.xj.engine.craft.background.BackgroundCraft;
import io.xj.engine.craft.background.BackgroundCraftImpl;
import io.xj.engine.craft.beat.BeatCraft;
import io.xj.engine.craft.beat.BeatCraftImpl;
import io.xj.engine.craft.detail.DetailCraft;
import io.xj.engine.craft.detail.DetailCraftImpl;
import io.xj.engine.craft.macro_main.MacroMainCraft;
import io.xj.engine.craft.macro_main.MacroMainCraftImpl;
import io.xj.engine.craft.transition.TransitionCraft;
import io.xj.engine.craft.transition.TransitionCraftImpl;
import io.xj.engine.fabricator.Fabricator;
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
