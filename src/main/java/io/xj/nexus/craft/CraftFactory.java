// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft;

import com.google.inject.assistedinject.Assisted;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraft;
import io.xj.nexus.craft.macro_main.MacroMainCraft;
import io.xj.nexus.craft.rhythm.RhythmCraft;
import io.xj.nexus.fabricator.Fabricator;

/**
 Craft is performed in order:
 1. High
 2. Mid
 3. Low
 <p>
 Fabricator basis = basisFactory.fabricate(segment);
 craftFactory.macroMain(basis).craft();
 craftFactory.rhythm(basis).craft();
 craftFactory.voice(basis).craft();
 basis.putReport();
 */
public interface CraftFactory {

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
   Create Rhythm Craft instance for a particular segment

   @param fabricator of craft
   @return RhythmCraft
   @throws NexusException on failure
   */
  RhythmCraft rhythm(
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

}
