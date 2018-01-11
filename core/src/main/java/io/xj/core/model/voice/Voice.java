// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.voice;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.model.instrument.InstrumentType;

import java.math.BigInteger;
import java.util.Objects;

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
public class Voice extends Entity {

  public static final String KEY_ONE = "voice";
  public static final String KEY_MANY = "voices";

  private String _type; // to hold value before validation
  private InstrumentType type;
  private String description;
  private BigInteger patternId;

  public Voice() {}

  public Voice(BigInteger id) {
    this.id = id;
  }

  public BigInteger getPatternId() {
    return patternId;
  }

  public Voice setPatternId(BigInteger patternId) {
    this.patternId = patternId;
    return this;
  }

  public InstrumentType getType() {
    return type;
  }

  public Voice setType(String type) {
    _type = type;
    return this;
  }

  public void setTypeEnum(InstrumentType type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public Voice setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = InstrumentType.validate(_type);

    if (Objects.isNull(patternId)) {
      throw new BusinessException("Pattern ID is required.");
    }
    if (Objects.isNull(type)) {
      throw new BusinessException("Type is required.");
    }

    if (Objects.isNull(description) || description.isEmpty()) {
      throw new BusinessException("Description is required.");
    }
  }
}
