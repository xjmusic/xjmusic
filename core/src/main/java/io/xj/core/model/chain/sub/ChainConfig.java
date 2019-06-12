//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.chain.impl.ChainSubEntity;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.util.Objects;

/**
 [#160980748] Developer wants all chain binding models to extend `ChainBinding` with common properties and methods pertaining to Chain membership.
 <p>
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainConfig extends ChainSubEntity {
  private ChainConfigType type;
  private String value;
  private Exception typeException;

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("type")
      .add("value")
      .build();
  }

  /**
   Get type

   @return type
   */
  public ChainConfigType getType() {
    return type;
  }

  /**
   Get value

   @return value
   */
  public String getValue() {
    return value;
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
   @param typeString pending validation
   */
  public ChainConfig setType(String typeString) {
    try {
      type = ChainConfigType.validate(typeString);
    } catch (CoreException e) {
      typeException = e;
    }
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
   Set value

   @param value to set
   @return self
   */
  public ChainConfig setValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public ChainConfig validate() throws CoreException {
    super.validate();

    if (Objects.nonNull(typeException))
      throw new CoreException("Invalid type value.", typeException);

    require(type, "Type");

    if (Objects.isNull(value) || value.isEmpty())
      throw new CoreException("Value is required.");

    switch (type) {
      case OutputSampleBits:
      case OutputFrameRate:
      case OutputChannels:
      case OutputEncodingQuality:
        value = Text.toNumeric(value);
        requireNotEmpty(value, "numeric");
        break;

      case OutputEncoding:
      case OutputContainer:
        value = Text.toAlphaSlug(value).toUpperCase();
        requireNotEmpty(value, "text");
        break;
    }

    return this;
  }

  /**
   Throw exception is value is empty

   @param value     to ensure non-empty
   @param valueType for error message
   @throws CoreException if value is empty
   */
  private void requireNotEmpty(String value, String valueType) throws CoreException {
    if (value.isEmpty())
      throw new CoreException(String.format("Chain %s requires %s value!", type, valueType));
  }
}
