//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.chain.impl;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.entity.impl.SubEntityImpl;

import java.math.BigInteger;

/**
 [#166743281] Chain handles all of its own binding + config entities
 */
public abstract class ChainSubEntity extends SubEntityImpl {
  private BigInteger chainId;

  /**
   Get id of Chain to which this entity belongs

   @return chain id
   */
  public BigInteger getChainId() {
    return chainId;
  }

  @Override
  public BigInteger getParentId() {
    return chainId;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Chain.class)
      .build();
  }

  /**
   Set id of Chain to which this entity belongs

   @param chainId to which this entity belongs
   @return this Chain Entity (for chaining setters)
   */
  public ChainSubEntity setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  @Override
  public ChainSubEntity validate() throws CoreException {
    require(chainId, "Chain ID");

    return this;
  }
}
