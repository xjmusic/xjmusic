// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft;

import com.google.inject.assistedinject.Assisted;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.background.BackgroundCraft;
import io.xj.nexus.craft.detail.DetailCraft;
import io.xj.nexus.craft.hook.HookCraft;
import io.xj.nexus.craft.macro_main.MacroMainCraft;
import io.xj.nexus.craft.perc_loop.PercLoopCraft;
import io.xj.nexus.craft.beat.BeatCraft;
import io.xj.nexus.craft.transition.TransitionCraft;
import io.xj.nexus.fabricator.Fabricator;

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
   Create Background Craft instance for a particular segment

   @param fabricator of craft
   @return BackgroundCraft
   @throws NexusException on failure
   */
  BackgroundCraft background(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

  /**
   Create Beat Craft instance for a particular segment

   @param fabricator of craft
   @return BeatCraft
   @throws NexusException on failure
   */
  BeatCraft beat(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

  /**
   Create Detail Craft instance for a particular segment

   @param fabricator of craft
   @return DetailCraft
   @throws NexusException on failure
   */
  DetailCraft detail(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

  /**
   Create Hook Craft instance for a particular segment

   @param fabricator of craft
   @return HookCraft
   @throws NexusException on failure
   */
  HookCraft hook(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

  /**
   Create Foundation Craft instance for a particular segment
   [#138] Foundation craft for Segment of a Chain

   @param fabricator of craft
   @return MacroMainCraft
   @throws NexusException on failure
   */
  MacroMainCraft macroMain(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

  /**
   Create Percussion Loop Craft instance for a particular segment

   @param fabricator of craft
   @return PercLoopCraft
   @throws NexusException on failure
   */
  PercLoopCraft percLoop(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

  /**
   Create Transition Craft instance for a particular segment

   @param fabricator of craft
   @return TransitionCraft
   @throws NexusException on failure
   */
  TransitionCraft transition(
    @Assisted("basis") Fabricator fabricator
  ) throws NexusException;

}
