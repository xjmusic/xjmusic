// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.model.link.Link;

import com.google.inject.assistedinject.Assisted;

/**
 Craft is performed in order:
 1. High
 2. Mid
 3. Low
 <p>
 Basis basis = craftFactory.createBasis(link);
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
   @return ChainWorkMaster
   @throws ConfigException on failure
   */
  FoundationCraft foundation(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create Structure Craft instance for a particular link

   @param basis of craft
   @return ChainWorkMaster
   @throws ConfigException on failure
   */
  StructureCraft structure(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create Voice Craft instance for a particular link

   @param basis of craft
   @return ChainWorkMaster
   @throws ConfigException on failure
   */
  VoiceCraft voice(
    @Assisted("basis") Basis basis
  ) throws ConfigException;

  /**
   Create a basis for Macro & Main idea craft (previous link and other cached resources)

   @param link Link to be worked on
   @return ChainWorkMaster
   @throws ConfigException on failure
   */
  Basis createBasis(
    @Assisted("link") Link link
  ) throws ConfigException;

}
