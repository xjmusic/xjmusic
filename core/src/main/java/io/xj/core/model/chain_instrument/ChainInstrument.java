// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_instrument;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainInstrument extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainInstrument";
  public static final String KEY_MANY = "chainInstruments";
  // Chain ID
  private BigInteger chainId;
  // Instrument ID
  private BigInteger instrumentId;

  public BigInteger getChainId() {
    return chainId;
  }

  public ChainInstrument setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  public BigInteger getInstrumentId() {
    return instrumentId;
  }

  public ChainInstrument setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
  }

}
