// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.model.link.Link;

import com.google.inject.assistedinject.Assisted;

/**
 [#138] Macro-Choice for Initial Link of a Chain
 */
public interface CraftFactory {

  /**
   Create a Macro & Main idea Craft for a particular link

   @param link Link to be worked on
   @return ChainWorkMaster
   @throws ConfigException on failure
   */
  MacroCraft createMacroCraft(
    @Assisted("link") Link link
  ) throws ConfigException;

}
