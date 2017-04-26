// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_instrument;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class ChainInstrumentWrapper extends EntityWrapper {

  // Chain
  private ChainInstrument chainInstrument;
  public ChainInstrument getChainInstrument() {
    return chainInstrument;
  }
  public ChainInstrumentWrapper setChainInstrument(ChainInstrument chainInstrument) {
    this.chainInstrument = chainInstrument;
    return this;
  }

  /**
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public ChainInstrument validate() throws BusinessException{
    if (this.chainInstrument == null) {
      throw new BusinessException("Chain Instrument is required.");
    }
    this.chainInstrument.validate();
    return this.chainInstrument;
  }

}
