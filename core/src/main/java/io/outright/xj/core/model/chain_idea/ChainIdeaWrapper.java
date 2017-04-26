// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_idea;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public ChainIdea validate() throws BusinessException{
    if (this.chainIdea == null) {
      throw new BusinessException("Chain Idea is required.");
    }
    this.chainIdea.validate();
    return this.chainIdea;
  }

}
