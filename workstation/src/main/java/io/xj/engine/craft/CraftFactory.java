// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft;


import io.xj.model.pojos.Program;
import io.xj.engine.FabricationException;
import io.xj.engine.craft.background.BackgroundCraft;
import io.xj.engine.craft.beat.BeatCraft;
import io.xj.engine.craft.detail.DetailCraft;
import io.xj.engine.craft.macro_main.MacroMainCraft;
import io.xj.engine.craft.transition.TransitionCraft;
import io.xj.engine.fabricator.Fabricator;
import jakarta.annotation.Nullable;

import java.util.Collection;

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
public interface CraftFactory {

  /**
   Create Foundation Craft instance for a particular segment
   [#138] Foundation craft for Segment of a Chain

   @param fabricator           of craft
   @param overrideMacroProgram already selected to use for craft
   @param overrideMemes        already selected to use for craft
   @return MacroMainCraft
   @throws FabricationException on failure
   */
  MacroMainCraft macroMain(
      Fabricator fabricator,
      @Nullable Program overrideMacroProgram,
      @Nullable Collection<String> overrideMemes
  ) throws FabricationException;

  /**
   Create Beat Craft instance for a particular segment

   @param fabricator of craft
   @return BeatCraft
   @throws FabricationException on failure
   */
  BeatCraft beat(
      Fabricator fabricator
  ) throws FabricationException;

  /**
   Create Detail Craft instance for a particular segment

   @param fabricator of craft
   @return DetailCraft
   @throws FabricationException on failure
   */
  DetailCraft detail(
      Fabricator fabricator
  ) throws FabricationException;

  /**
   Create Background Craft instance for a particular segment

   @param fabricator of craft
   @return BackgroundCraft
   @throws FabricationException on failure
   */
  BackgroundCraft background(
    Fabricator fabricator
  ) throws FabricationException;

  /**
   Create Transition Craft instance for a particular segment

   @param fabricator of craft
   @return TransitionCraft
   @throws FabricationException on failure
   */
  TransitionCraft transition(
      Fabricator fabricator
  ) throws FabricationException;
}
