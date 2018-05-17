// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.util.Text;

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
  public static final String KEY_ONE = "pattern";
  public static final String KEY_MANY = "patterns";

  private String name;
  private String _type; // to hold value before validation
  private PatternType type;
  private BigInteger sequenceId;
  private String key;
  private Integer total;
  private BigInteger offset;
  private Double density;
  private Double tempo;
  private PatternState state;
  private String _stateString; // pending validation, copied to `state` field


  public Pattern() {}

  public Pattern(int id) {
    this.id = BigInteger.valueOf((long) id);
  }

  public Pattern(BigInteger id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Pattern setName(String name) {
    this.name = name;
    return this;
  }

  public PatternState getState() {
    return state;
  }

  /**
   This sets the state String, however the value will remain null
   until validate() is called and the value is cast to enum

   @param stateString pending validation
   */
  public Pattern setState(String stateString) {
    _stateString = Text.toAlphabetical(stateString);
    return this;
  }

  public Pattern setStateEnum(PatternState state) {
    this.state = state;
    return this;
  }

  public PatternType getType() {
    return type;
  }

  public Pattern setType(String value) {
    _type = value;
    return this;
  }

  public Pattern setTypeEnum(PatternType type) {
    this.type = type;
    return this;
  }

  public BigInteger getSequenceId() {
    return sequenceId;
  }

  public Pattern setSequenceId(BigInteger sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  public String getKey() {
    return key;
  }

  public Pattern setKey(String key) {
    this.key = key;
    return this;
  }

  public Integer getTotal() {
    return total;
  }

  public Pattern setTotal(Integer total) {
    this.total = total;
    return this;
  }

  public BigInteger getOffset() {
    return offset;
  }

  public Pattern setOffset(BigInteger offset) {
    this.offset = offset;
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Pattern setDensity(Double density) {
    this.density = density;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Pattern setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return sequenceId;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    if (Objects.isNull(type))
      type = PatternType.validate(_type);

    // throws its own BusinessException on failure
    if (Objects.isNull(state))
      state = PatternState.validate(_stateString);

    if (Objects.nonNull(name) && name.isEmpty()) {
      name = null;
    }
    if (null == sequenceId) {
      throw new BusinessException("Sequence ID is required.");
    }
    if (Objects.nonNull(key) && key.isEmpty()) {
      key = null;
    }
    if (null == offset) {
      throw new BusinessException("Offset is required.");
    }
    if (Objects.nonNull(density) && (double) 0 == density) {
      density = null;
    }
    if (Objects.nonNull(tempo) && (double) 0 == tempo) {
      tempo = null;
    }
  }

  @Override
  public String toString() {
    return (Objects.nonNull(name) ? name + " " : "") + "(" + "@" + offset + ")";
  }

}
