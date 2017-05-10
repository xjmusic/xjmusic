// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
