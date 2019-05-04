// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_instrument;

import io.xj.core.exception.CoreException;
import io.xj.core.model.chain_binding.ChainBinding;

import java.math.BigInteger;

/**
 [#160980748] Developer wants all chain binding models to extend `ChainBinding` with common properties and methods pertaining to Chain membership.

 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainInstrument extends ChainBinding {
  public static final String KEY_ONE = "chainInstrument";
  public static final String KEY_MANY = "chainInstruments";
  private BigInteger instrumentId;

  /**
   Set chain id

   @param chainId to set
   @return self
   */
  public ChainInstrument setChainId(BigInteger chainId) {
    super.setChainId(chainId);
    return this;
  }

  /**
   Get instrument id

   @return instrument id
   */
  public BigInteger getInstrumentId() {
    return instrumentId;
  }

  /**
   set instrument id

   @param instrumentId instrument id
   @return self
   */
  public ChainInstrument setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();
    if (null == instrumentId) {
      throw new CoreException("Instrument ID is required.");
    }
  }

}
