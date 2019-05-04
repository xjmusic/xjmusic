// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_config;

import io.xj.core.exception.CoreException;
import io.xj.core.model.chain_binding.ChainBinding;
import io.xj.core.util.Text;

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
public class ChainConfig extends ChainBinding {
  public static final String KEY_ONE = "chainConfig";
  public static final String KEY_MANY = "chainConfigs";
  private ChainConfigType type;
  private String _typeString; // pending validation, copied to `type` field
  private String value;

  /**
   Get type

   @return type
   */
  public ChainConfigType getType() {
    return type;
  }

  /**
   This sets the type String, however the value will remain null
   until validate() is called and the value is cast to enum

   @param typeString pending validation
   */
  public ChainConfig setType(String typeString) {
    _typeString = Text.toAlphabetical(typeString);
    return this;
  }

  /**
   Set Chain id

   @param chainId of chain
   @return self
   */
  public ChainConfig setChainId(BigInteger chainId) {
    super.setChainId(chainId);
    return this;
  }

  /**
   Set type enum

   @param type enum to set
   @return self
   */
  public ChainConfig setTypeEnum(ChainConfigType type) {
    this.type = type;
    return this;
  }

  /**
   Get value

   @return value
   */
  public String getValue() {
    return value;
  }

  /**
   Set value

   @param value to set
   @return self
   */
  public ChainConfig setValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();

    // throws its own CoreException on failure
    type = ChainConfigType.validate(_typeString);

    if (null == value || value.isEmpty())
      throw new CoreException("Value is required.");

  }
}
