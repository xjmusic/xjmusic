// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainWrapper extends EntityWrapper {

  // Chain
  private Chain chain;

  public Chain getChain() {
    return chain;
  }

  public ChainWrapper setChain(Chain chain) {
    this.chain = chain;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public Chain validate() throws BusinessException {
    if (this.chain == null) {
      throw new BusinessException("Chain is required.");
    }
    this.chain.validate();
    return this.chain;
  }

}
