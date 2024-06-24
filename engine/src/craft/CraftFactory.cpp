// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/CraftFactory.h"

using namespace XJ;

MacroMainCraft CraftFactory::macroMain(
    Fabricator *fabricator,
    const std::optional<Program *> &overrideMacroProgram,
    const std::set<std::string> &overrideMemes) {
  return MacroMainCraft(fabricator, overrideMacroProgram, overrideMemes);
}

BeatCraft CraftFactory::beat(Fabricator *fabricator) {
  return BeatCraft(fabricator);
}

DetailCraft CraftFactory::detail(Fabricator *fabricator) {
  return DetailCraft(fabricator);
}

TransitionCraft CraftFactory::transition(Fabricator *fabricator) {
  return TransitionCraft(fabricator);
}

BackgroundCraft CraftFactory::background(Fabricator *fabricator) {
  return BackgroundCraft(fabricator);
}
