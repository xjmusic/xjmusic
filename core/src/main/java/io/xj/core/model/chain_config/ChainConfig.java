// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_config;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHAIN_CONFIG;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainConfig extends Entity {

  public static final String KEY_ONE = "chainConfig";
  public static final String KEY_MANY = "chainConfigs";
  private ULong chainId;
  private ChainConfigType type;
  private String _typeString; // pending validation, copied to `type` field
  private String value;

  public ULong getChainId() {
    return chainId;
  }

  public ChainConfig setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  public ChainConfigType getType() {
    return type;
  }

  /**
   This sets the type String, however the value will remain null
   until validate() is called and the value is cast to enum
   @param typeString pending validation
   */
  public ChainConfig setType(String typeString) {
    this._typeString = Text.toAlphabetical(typeString);
    return this;
  }

  public ChainConfig setTypeEnum(ChainConfigType type) {
    this.type = type;
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
    if (this.chainId == null)
      throw new BusinessException("Chain ID is required.");

    // throws its own BusinessException on failure
    this.type = ChainConfigType.validate(_typeString);

    if (this.value == null || this.value.length() == 0)
      throw new BusinessException("Value is required.");

  }

  @Override
  public ChainConfig setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHAIN_CONFIG.ID);
    chainId = record.get(CHAIN_CONFIG.CHAIN_ID);
    type = ChainConfigType.valueOf(record.get(CHAIN_CONFIG.TYPE));
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
