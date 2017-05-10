// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_config;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.CHAIN_CONFIG;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainConfig extends Entity {
  public final static String OUTPUT_SAMPLE_BITS = "OUTPUT_SAMPLE_BITS";
  public final static String OUTPUT_FRAME_RATE = "OUTPUT_FRAME_RATE";
  public final static String OUTPUT_CHANNELS = "OUTPUT_CHANNELS";

  public final static List<String> TYPES = ImmutableList.of(
    OUTPUT_SAMPLE_BITS,
    OUTPUT_FRAME_RATE,
    OUTPUT_CHANNELS
  );
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainConfig";
  public static final String KEY_MANY = "chainConfigs";
  // Chain ID
  private ULong chainId;
  // Type
  private String type;
  // Value
  private String value;

  public ULong getChainId() {
    return chainId;
  }

  public ChainConfig setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  public String getType() {
    return type;
  }

  public ChainConfig setType(String type) {
    this.type = Text.UpperScored(type);
    return this;
  }

  public String getValue() {
    return value;
  }

  public ChainConfig setValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (this.value == null || this.value.length() == 0) {
      throw new BusinessException("Value is required.");
    }
  }

  @Override
  public ChainConfig setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHAIN_CONFIG.ID);
    chainId = record.get(CHAIN_CONFIG.CHAIN_ID);
    type = record.get(CHAIN_CONFIG.TYPE);
    value = record.get(CHAIN_CONFIG.VALUE);
    createdAt = record.get(CHAIN_CONFIG.CREATED_AT);
    updatedAt = record.get(CHAIN_CONFIG.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_CONFIG.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_CONFIG.TYPE, type);
    fieldValues.put(CHAIN_CONFIG.VALUE, value);
    return fieldValues;
  }


}
