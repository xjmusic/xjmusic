// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chain_library;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHAIN_LIBRARY;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainLibrary extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainLibrary";
  public static final String KEY_MANY = "chainLibraries";
  // Chain ID
  private ULong chainId;
  // Library ID
  private ULong libraryId;

  public ULong getChainId() {
    return chainId;
  }

  public ChainLibrary setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  public ULong getLibraryId() {
    return libraryId;
  }

  public ChainLibrary setLibraryId(BigInteger libraryId) {
    this.libraryId = ULong.valueOf(libraryId);
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

  @Override
  public ChainLibrary setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHAIN_LIBRARY.ID);
    chainId = record.get(CHAIN_LIBRARY.CHAIN_ID);
    libraryId = record.get(CHAIN_LIBRARY.LIBRARY_ID);
    createdAt = record.get(CHAIN_LIBRARY.CREATED_AT);
    updatedAt = record.get(CHAIN_LIBRARY.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_LIBRARY.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_LIBRARY.LIBRARY_ID, libraryId);
    return fieldValues;
  }


}
