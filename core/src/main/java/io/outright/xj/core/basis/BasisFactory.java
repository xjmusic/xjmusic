// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.basis;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.model.link.Link;

import com.google.inject.assistedinject.Assisted;

/**
 <p>
 Basis basis = basisFactory.createBasis(link);
 ... do things with this basis, like craft or dub ...
 basis.sendReport();
 */
public interface BasisFactory {
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
