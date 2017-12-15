// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_config;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.util.Text;

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
public class ChainConfig extends Entity {

  public static final String KEY_ONE = "chainConfig";
  public static final String KEY_MANY = "chainConfigs";
  private BigInteger chainId;
  private ChainConfigType type;
  private String _typeString; // pending validation, copied to `type` field
  private String value;

  public BigInteger getChainId() {
    return chainId;
  }

  public ChainConfig setChainId(BigInteger chainId) {
    this.chainId = chainId;
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
}
