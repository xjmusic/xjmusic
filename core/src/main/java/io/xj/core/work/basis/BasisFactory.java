// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work.basis;

import io.xj.core.exception.ConfigException;
import io.xj.core.model.link.Link;

import com.google.inject.assistedinject.Assisted;

/**
 <p>
 Basis basis = basisFactory.createBasis(link);
 ... do things with this basis, like craft or dub ...
 basis.sendReport();
 */
public interface BasisFactory {
  /**
   Create a basis for Macro & Main pattern craft (previous link and other cached resources)

   @param link Link to be worked on
   @return ChainWorkMaster
   @throws ConfigException on failure
   */
  Basis createBasis(
    @Assisted("link") Link link
  ) throws ConfigException;

}
