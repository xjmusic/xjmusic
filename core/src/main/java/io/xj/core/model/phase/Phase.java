// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase;

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
public class Phase extends Entity {
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "phase";
  public static final String KEY_MANY = "phases";
  /**
   Name
   */
  private String name;
  /**
   Pattern
   */
  private BigInteger patternId;
  /**
   Key
   */
  private String key;
  /**
   Total
   */
  private Integer total;
  /**
   Offset
   */
  private BigInteger offset;
  /**
   Density
   */
  private Double density;
  /**
   Tempo
   */
  private Double tempo;

  public String getName() {
    return name;
  }

  public Phase setName(String name) {
    this.name = name;
    return this;
  }

  public BigInteger getPatternId() {
    return patternId;
  }

  public Phase setPatternId(BigInteger patternId) {
    this.patternId = patternId;
    return this;
  }

  public String getKey() {
    return key;
  }

  public Phase setKey(String key) {
    this.key = key;
    return this;
  }

  public Integer getTotal() {
    return total;
  }

  public Phase setTotal(Integer total) {
    this.total = total;
    return this;
  }

  public BigInteger getOffset() {
    return offset;
  }

  public Phase setOffset(BigInteger offset) {
    this.offset = offset;
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Phase setDensity(Double density) {
    this.density = density;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Phase setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (Objects.nonNull(name) && name.isEmpty()) {
      name = null;
    }
    if (null == patternId) {
      throw new BusinessException("Pattern ID is required.");
    }
    if (Objects.nonNull(key) && key.isEmpty()) {
      key = null;
    }
    if (null == offset) {
      throw new BusinessException("Offset is required.");
    }
    if (Objects.nonNull(density) && 0 == density) {
      density = null;
    }
    if (Objects.nonNull(tempo) && 0 == tempo) {
      tempo = null;
    }
  }

}
