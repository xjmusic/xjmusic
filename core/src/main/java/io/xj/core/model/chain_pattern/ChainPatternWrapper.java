// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainPatternWrapper extends EntityWrapper {

  // Chain
  private ChainPattern chainPattern;

  public ChainPattern getChainPattern() {
    return chainPattern;
  }

  public ChainPatternWrapper setChainPattern(ChainPattern chainPattern) {
    this.chainPattern = chainPattern;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public ChainPattern validate() throws BusinessException {
    if (this.chainPattern == null) {
      throw new BusinessException("Chain Pattern is required.");
    }
    this.chainPattern.validate();
    return this.chainPattern;
  }

}
