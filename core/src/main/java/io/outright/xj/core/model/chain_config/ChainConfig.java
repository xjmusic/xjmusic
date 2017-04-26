// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_config;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN_CONFIG;

public class ChainConfig extends Entity {
  public final static String OUTPUT_SAMPLE_BITS = "OUTPUT_SAMPLE_BITS";
  public final static String OUTPUT_FRAME_RATE = "OUTPUT_FRAME_RATE";
  public final static String OUTPUT_CHANNELS = "OUTPUT_CHANNELS";

  public final static List<String> TYPES = ImmutableList.of(
    OUTPUT_SAMPLE_BITS,
    OUTPUT_FRAME_RATE,
    OUTPUT_CHANNELS
  );

  // Chain ID
  private BigInteger chainId;

  public ULong getChainId() {
    return ULong.valueOf(chainId);
  }

  public ChainConfig setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  // Type
  private String type;

  public String getType() {
    return type;
  }

  public ChainConfig setType(String type) {
    this.type = Purify.UpperScored(type);
    return this;
  }

  // Value
  private String value;

  public String getValue() {
    return value;
  }

  public ChainConfig setValue(String value) {
    this.value = value;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (this.value == null || this.value.length() == 0) {
      throw new BusinessException("Value is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_CONFIG.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_CONFIG.TYPE, type);
    fieldValues.put(CHAIN_CONFIG.VALUE, value);
    return fieldValues;
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "chainConfig";
  public static final String KEY_MANY = "chainConfigs";


}
