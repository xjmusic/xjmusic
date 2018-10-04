//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_binding;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

import java.math.BigInteger;

/**
 [#160980748] Developer wants all chain binding models to extend `ChainBinding` with common properties and methods pertaining to Chain membership.
 <p>
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainBinding extends Entity {

  public static final String KEY_ONE = "chainBinding";
  public static final String KEY_MANY = "chainBindings";
  private BigInteger chainId;

  public BigInteger getChainId() {
    return chainId;
  }

  public ChainBinding setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return chainId;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == chainId)
      throw new BusinessException("Chain ID is required.");
  }
}
