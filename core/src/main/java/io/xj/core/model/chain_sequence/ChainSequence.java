// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_sequence;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

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
public class ChainSequence extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainSequence";
  public static final String KEY_MANY = "chainSequences";
  // Chain ID
  private BigInteger chainId;
  // Sequence ID
  private BigInteger sequenceId;

  public BigInteger getChainId() {
    return chainId;
  }

  public ChainSequence setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  public BigInteger getSequenceId() {
    return sequenceId;
  }

  public ChainSequence setSequenceId(BigInteger sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return chainId;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.sequenceId == null) {
      throw new BusinessException("Sequence ID is required.");
    }
  }

}
