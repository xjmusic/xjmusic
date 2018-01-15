// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft;

import io.xj.core.exception.ConfigException;
import io.xj.core.work.basis.Basis;
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
 Basis basis = basisFactory.createBasis(link);
 craftFactory.macroMain(basis).craft();
 craftFactory.rhythm(basis).craft();
 craftFactory.voice(basis).craft();
 basis.sendReport();
 */
public interface CraftFactory {

  /**
   Create Foundation Craft instance for a particular link
   [#138] Foundation craft for Link of a Chain

   @param basis of craft
   @return MacroMainCraft
   @throws ConfigException on failure
   */
  MacroMainCraft macroMain(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create Rhythm Craft instance for a particular link

   @param basis of craft
   @return RhythmCraft
   @throws ConfigException on failure
   */
  RhythmCraft rhythm(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create Detail Craft instance for a particular link

   @param basis of craft
   @return HarmonicDetailCraft
   @throws ConfigException on failure
   */
  HarmonicDetailCraft harmonicDetail(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

}
