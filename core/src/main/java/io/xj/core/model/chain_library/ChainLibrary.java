// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_library;

import io.xj.core.exception.BusinessException;
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
public class ChainLibrary extends ChainBinding {
  public static final String KEY_ONE = "chainLibrary";
  public static final String KEY_MANY = "chainLibraries";
  private BigInteger libraryId;

  /**
   Set chain id

   @param chainId to set
   @return self
   */
  public ChainLibrary setChainId(BigInteger chainId) {
    super.setChainId(chainId);
    return this;
  }

  /**
   Get library id

   @return library id
   */
  public BigInteger getLibraryId() {
    return libraryId;
  }

  /**
   Set library id

   @param libraryId to set
   @return self
   */
  public ChainLibrary setLibraryId(BigInteger libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    super.validate();
    if (null == libraryId) {
      throw new BusinessException("Library ID is required.");
    }
  }

}
