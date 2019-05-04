// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft;

import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.craft.macro.MacroMainCraft;
import io.xj.craft.harmonic.HarmonicDetailCraft;
import io.xj.craft.rhythm.RhythmCraft;

import com.google.inject.assistedinject.Assisted;

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
   @throws CoreException on failure
   */
  MacroMainCraft macroMain(
    @Assisted("basis") Fabricator fabricator
  ) throws CoreException;

  /**
   Create Rhythm Craft instance for a particular segment

   @param fabricator of craft
   @return RhythmCraft
   @throws CoreException on failure
   */
  RhythmCraft rhythm(
    @Assisted("basis") Fabricator fabricator
  ) throws CoreException;

  /**
   Create Detail Craft instance for a particular segment

   @param fabricator of craft
   @return HarmonicDetailCraft
   @throws CoreException on failure
   */
  HarmonicDetailCraft harmonicDetail(
    @Assisted("basis") Fabricator fabricator
  ) throws CoreException;

}
