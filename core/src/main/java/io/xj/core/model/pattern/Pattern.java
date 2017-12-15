// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

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
public class Pattern extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "pattern";
  public static final String KEY_MANY = "patterns";

  private String name;
  private String _type; // to hold value before validation
  private PatternType type;
  private BigInteger libraryId;
  private BigInteger userId;
  private String key;
  private Double density;
  private Double tempo;

  public String getName() {
    return name;
  }

  public Pattern setName(String value) {
    name = value;
    return this;
  }

  public PatternType getType() {
    return type;
  }

  public Pattern setType(String value) {
    _type = value;
    return this;
  }

  public void setTypeEnum(PatternType type) {
    this.type = type;
  }

  public BigInteger getLibraryId() {
    return libraryId;
  }

  public Pattern setLibraryId(BigInteger value) {
    libraryId = value;
    return this;
  }

  public BigInteger getUserId() {
    return userId;
  }

  public Pattern setUserId(BigInteger value) {
    userId = value;
    return this;
  }

  public String getKey() {
    return key;
  }

  public Pattern setKey(String value) {
    key = value;
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Pattern setDensity(Double value) {
    density = value;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Pattern setTempo(Double value) {
    tempo = value;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = PatternType.validate(_type);

    if (Objects.isNull(name) || name.isEmpty()) {
      throw new BusinessException("Name is required.");
    }
    if (Objects.isNull(libraryId)) {
      throw new BusinessException("Library ID is required.");
    }
    if (Objects.isNull(userId)) {
      throw new BusinessException("User ID is required.");
    }
    if (Objects.isNull(type)) {
      throw new BusinessException("Type is required.");
    }
    if (Objects.isNull(key) || key.isEmpty()) {
      throw new BusinessException("Key is required.");
    }
    if (Objects.isNull(density)) {
      throw new BusinessException("Density is required.");
    }
    if (Objects.isNull(tempo)) {
      throw new BusinessException("Tempo is required.");
    }
  }
}
