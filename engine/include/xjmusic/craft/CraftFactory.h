// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CRAFT_FACTORY_H
#define XJMUSIC_CRAFT_FACTORY_H

#include <optional>

#include "xjmusic/content/Program.h"
#include "xjmusic/fabricator/Fabricator.h"

#include "BeatCraft.h"
#include "DetailCraft.h"
#include "MacroMainCraft.h"
#include "BackgroundCraft.h"
#include "TransitionCraft.h"

namespace XJ {

/**
 Craft is performed in order:
 1. High
 2. Mid
 3. Low
 <p>
 Fabricator basis = basisFactory.fabricate(segment);
 craftFactory.macroMain(basis).craft();
 craftFactory.beat(basis).craft();
 craftFactory.voice(basis).craft();
 basis.putReport();
 */
class CraftFactory {
public:

  /**
   Create Foundation Craft instance for a particular segment
   [#138] Foundation craft for Segment of a Chain

   @param fabricator           of craft
   @param overrideMacroProgram already selected to use for craft
   @param overrideMemes        already selected to use for craft
   @return MacroMainCraft
   @ on failure
   */
  static MacroMainCraft macroMain(
      Fabricator *fabricator,
      const std::optional<const Program *> &overrideMacroProgram,
      const std::set<std::string> &overrideMemes);

  /**
   Create Beat Craft instance for a particular segment

   @param fabricator of craft
   @return BeatCraft
   @ on failure
   */
  static BeatCraft beat(
      Fabricator *fabricator
  );

  /**
   Create Detail Craft instance for a particular segment

   @param fabricator of craft
   @return DetailCraft
   @ on failure
   */
  static DetailCraft detail(
      Fabricator *fabricator
  );

  /**
   Create Background Craft instance for a particular segment

   @param fabricator of craft
   @return BackgroundCraft
   @ on failure
   */
  static BackgroundCraft background(
    Fabricator *fabricator
  );

  /**
   Create Transition Craft instance for a particular segment

   @param fabricator of craft
   @return TransitionCraft
   @ on failure
   */
  static TransitionCraft transition(
      Fabricator *fabricator
  );
};

}// namespace XJ

#endif//XJMUSIC_CRAFT_FACTORY_H