// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_library;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

import org.jooq.Record;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.chainLibrary == null) {
      throw new BusinessException("Chain Library is required.");
    }
    this.chainLibrary.validate();
  }

  @Override
  public String toString() {
    return "{" +
      ChainLibrary.KEY_ONE + ":" + this.chainLibrary +
      "}";
  }

  public Record intoRecord() {
    return null;
  }
}
