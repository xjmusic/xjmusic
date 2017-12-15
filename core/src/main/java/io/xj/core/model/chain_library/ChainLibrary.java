// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_library;

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
public class ChainLibrary extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainLibrary";
  public static final String KEY_MANY = "chainLibraries";
  // Chain ID
  private BigInteger chainId;
  // Library ID
  private BigInteger libraryId;

  public BigInteger getChainId() {
    return chainId;
  }

  public ChainLibrary setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  public BigInteger getLibraryId() {
    return libraryId;
  }

  public ChainLibrary setLibraryId(BigInteger libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.libraryId == null) {
      throw new BusinessException("Library ID is required.");
    }
  }

}
