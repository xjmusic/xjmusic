// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/CraftFactory.h"

using namespace XJ;

CraftFactory::CraftFactory() {
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

