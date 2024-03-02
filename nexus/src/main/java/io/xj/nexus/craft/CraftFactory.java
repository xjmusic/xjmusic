// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft;


import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.background.BackgroundCraft;
import io.xj.nexus.craft.beat.BeatCraft;
import io.xj.nexus.craft.detail.DetailCraft;
import io.xj.nexus.craft.macro_main.MacroMainCraft;
import io.xj.nexus.craft.transition.TransitionCraft;
import io.xj.nexus.fabricator.Fabricator;
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
   @throws NexusException on failure
   */
  MacroMainCraft macroMain(
      Fabricator fabricator,
      @Nullable Program overrideMacroProgram,
      @Nullable Collection<String> overrideMemes
  ) throws NexusException;

  /**
   Create Beat Craft instance for a particular segment

   @param fabricator of craft
   @return BeatCraft
   @throws NexusException on failure
   */
  BeatCraft beat(
      Fabricator fabricator
  ) throws NexusException;

  /**
   Create Detail Craft instance for a particular segment

   @param fabricator of craft
   @return DetailCraft
   @throws NexusException on failure
   */
  DetailCraft detail(
      Fabricator fabricator
  ) throws NexusException;

  /**
   Create Background Craft instance for a particular segment

   @param fabricator of craft
   @return BackgroundCraft
   @throws NexusException on failure
   */
  BackgroundCraft background(
    Fabricator fabricator
  ) throws NexusException;

  /**
   Create Transition Craft instance for a particular segment

   @param fabricator of craft
   @return TransitionCraft
   @throws NexusException on failure
   */
  TransitionCraft transition(
      Fabricator fabricator
  ) throws NexusException;
}
