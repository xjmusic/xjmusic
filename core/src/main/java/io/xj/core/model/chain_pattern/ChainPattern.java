// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_pattern;

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
public class ChainPattern extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainPattern";
  public static final String KEY_MANY = "chainPatterns";
  // Chain ID
  private BigInteger chainId;
  // Pattern ID
  private BigInteger patternId;

  public BigInteger getChainId() {
    return chainId;
  }

  public ChainPattern setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  public BigInteger getPatternId() {
    return patternId;
  }

  public ChainPattern setPatternId(BigInteger patternId) {
    this.patternId = patternId;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.patternId == null) {
      throw new BusinessException("Pattern ID is required.");
    }
  }

}
