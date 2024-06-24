// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CRAFT_MACRO_MAIN_CRAFT_H
#define XJMUSIC_CRAFT_MACRO_MAIN_CRAFT_H

#include "xjmusic/fabricator/FabricationWrapper.h"
#include "xjmusic/fabricator/Fabricator.h"

namespace XJ {

  /**
   [#138] Foundation craft for Initial Segment of a Chain
   [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
   */
  class MacroMainCraft : FabricationWrapper {
    std::optional<XJ::Program *> overrideMacroProgram;
    std::set<std::string> overrideMemes;

  public:
    MacroMainCraft(
        Fabricator *fabricator,
        const std::optional<Program *> &overrideMacroProgram,
        const std::set<std::string> &overrideMemes);

    /**
     perform macro craft for the current segment
     */
    void doWork();
  };

}// namespace XJ

#endif//XJMUSIC_CRAFT_MACRO_MAIN_CRAFT_H