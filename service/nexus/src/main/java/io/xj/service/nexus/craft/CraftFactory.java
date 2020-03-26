// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.HubException;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.craft.harmonic.HarmonicDetailCraft;
import io.xj.service.nexus.craft.macro.MacroMainCraft;
import io.xj.service.nexus.craft.rhythm.RhythmCraft;

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
   @throws HubException on failure
   */
  MacroMainCraft macroMain(
    @Assisted("basis") Fabricator fabricator
  ) throws HubException;

  /**
   Create Rhythm Craft instance for a particular segment

   @param fabricator of craft
   @return RhythmCraft
   @throws HubException on failure
   */
  RhythmCraft rhythm(
    @Assisted("basis") Fabricator fabricator
  ) throws HubException;

  /**
   Create Detail Craft instance for a particular segment

   @param fabricator of craft
   @return HarmonicDetailCraft
   @throws HubException on failure
   */
  HarmonicDetailCraft harmonicDetail(
    @Assisted("basis") Fabricator fabricator
  ) throws HubException;

}
