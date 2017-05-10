// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_library;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainLibraryWrapper extends EntityWrapper {

  // Chain
  private ChainLibrary chainLibrary;

  public ChainLibrary getChainLibrary() {
    return chainLibrary;
  }

  public ChainLibraryWrapper setChainLibrary(ChainLibrary chainLibrary) {
    this.chainLibrary = chainLibrary;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public ChainLibrary validate() throws BusinessException {
    if (this.chainLibrary == null) {
      throw new BusinessException("Chain Library is required.");
    }
    this.chainLibrary.validate();
    return this.chainLibrary;
  }

}
