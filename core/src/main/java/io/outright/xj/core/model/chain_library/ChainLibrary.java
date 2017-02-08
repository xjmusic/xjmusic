// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_library;

import io.outright.xj.core.app.exception.BusinessException;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN_LIBRARY;
import static io.outright.xj.core.Tables.IDEA;

public class ChainLibrary {

  // Chain ID
  private BigInteger chainId;
  public ULong getChainId() {
    return ULong.valueOf(chainId);
  }
  public ChainLibrary setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  // Library ID
  private BigInteger libraryId;
  public ULong getLibraryId() {
    return ULong.valueOf(libraryId);
  }
  public ChainLibrary setLibraryId(BigInteger libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.libraryId == null) {
      throw new BusinessException("Library ID is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(CHAIN_LIBRARY.CHAIN_ID, chainId)
      .put(CHAIN_LIBRARY.LIBRARY_ID, libraryId)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "libraryId:" + this.libraryId +
      "chainId:" + this.chainId +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "chainLibrary";
  public static final String KEY_MANY = "chainLibraries";


}
