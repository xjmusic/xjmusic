// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker.craft;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.basis.Basis;

import com.google.inject.assistedinject.Assisted;

/**
 Craft is performed in order:
 1. High
 2. Mid
 3. Low
 <p>
 Basis basis = basisFactory.createBasis(link);
 craftFactory.foundation(basis).craft();
 craftFactory.structure(basis).craft();
 craftFactory.voice(basis).craft();
 basis.sendReport();
 */
public interface CraftFactory {

  /**
   Create Foundation Craft instance for a particular link
   [#138] Foundation craft for Link of a Chain

   @param basis of craft
   @return FoundationCraft
   @throws ConfigException on failure
   */
  FoundationCraft foundation(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create Structure Craft instance for a particular link

   @param basis of craft
   @return StructureCraft
   @throws ConfigException on failure
   */
  StructureCraft structure(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create Voice Craft instance for a particular link

   @param basis of craft
   @return VoiceCraft
   @throws ConfigException on failure
   */
  VoiceCraft voice(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

}
