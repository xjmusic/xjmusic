// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chain_idea;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainIdeaWrapper extends EntityWrapper {

  // Chain
  private ChainIdea chainIdea;

  public ChainIdea getChainIdea() {
    return chainIdea;
  }

  public ChainIdeaWrapper setChainIdea(ChainIdea chainIdea) {
    this.chainIdea = chainIdea;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public ChainIdea validate() throws BusinessException {
    if (this.chainIdea == null) {
      throw new BusinessException("Chain Idea is required.");
    }
    this.chainIdea.validate();
    return this.chainIdea;
  }

}
