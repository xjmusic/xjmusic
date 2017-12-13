// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHAIN_PATTERN;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainPattern extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainPattern";
  public static final String KEY_MANY = "chainPatterns";
  // Chain ID
  private ULong chainId;
  // Pattern ID
  private ULong patternId;

  public ULong getChainId() {
    return chainId;
  }

  public ChainPattern setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  public ULong getPatternId() {
    return patternId;
  }

  public ChainPattern setPatternId(BigInteger patternId) {
    this.patternId = ULong.valueOf(patternId);
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

  @Override
  public ChainPattern setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHAIN_PATTERN.ID);
    chainId = record.get(CHAIN_PATTERN.CHAIN_ID);
    patternId = record.get(CHAIN_PATTERN.PATTERN_ID);
    createdAt = record.get(CHAIN_PATTERN.CREATED_AT);
    updatedAt = record.get(CHAIN_PATTERN.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_PATTERN.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_PATTERN.PATTERN_ID, patternId);
    return fieldValues;
  }


}
