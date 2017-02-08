// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

import org.jooq.Record;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.chain == null) {
      throw new BusinessException("Chain is required.");
    }
    this.chain.validate();
  }

  @Override
  public String toString() {
    return "{" +
      Chain.KEY_ONE + ":" + this.chain +
      "}";
  }

  public Record intoRecord() {
    return null;
  }
}
