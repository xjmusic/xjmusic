// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern;

import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.util.Text;

import javax.annotation.Nullable;
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
public class Pattern extends EntityImpl {
  public static final String KEY_ONE = "pattern";
  public static final String KEY_MANY = "patterns";

  @Nullable
  private String name;
  private String _type; // to hold value before validation
  private PatternType type;
  private BigInteger sequenceId;
  @Nullable
  private String key;
  private Integer total;
  private Integer meterSuper;
  private Integer meterSub;
  private Integer meterSwing;
  @Nullable
  private Double density;
  @Nullable
  private Double tempo;
  private PatternState state;
  private String _stateString; // pending validation, copied to `state` field


  public Pattern() {
  }

  public Pattern(int id) {
    this.id = BigInteger.valueOf(id);
  }

  public Pattern(BigInteger id) {
    this.id = id;
  }

  @Nullable
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

/*
  public Pattern setStateEnum(PatternState state) {
    this.state = state;
    return this;
  }
*/

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

  @Nullable
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

  public Integer getMeterSuper() {
    return meterSuper;
  }

  public Pattern setMeterSuper(Integer meterSuper) {
    this.meterSuper = meterSuper;
    return this;
  }

  public Integer getMeterSub() {
    return meterSub;
  }

  public Pattern setMeterSub(Integer meterSub) {
    this.meterSub = meterSub;
    return this;
  }

  public Integer getMeterSwing() {
    return meterSwing;
  }

  public Pattern setMeterSwing(Integer meterSwing) {
    this.meterSwing = meterSwing;
    return this;
  }

  @Nullable
  public Double getDensity() {
    return density;
  }

  public Pattern setDensity(Double density) {
    this.density = density;
    return this;
  }

  @Nullable
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
  public void validate() throws CoreException {
    // throws its own CoreException on failure
    if (Objects.isNull(type))
      type = PatternType.validate(_type);

    // throws its own CoreException on failure
    if (Objects.isNull(state))
      state = PatternState.validate(_stateString);

    if (Objects.nonNull(name) && name.isEmpty()) {
      name = null;
    }
    if (null == sequenceId) {
      throw new CoreException("Sequence ID is required.");
    }
    if (Objects.nonNull(key) && key.isEmpty()) {
      key = null;
    }
    if (Objects.nonNull(density) && (double) 0 == density) {
      density = null;
    }
    if (Objects.nonNull(tempo) && (double) 0 == tempo) {
      tempo = null;
    }
    switch (type) {
      case Macro:
        break;
      case Main:
        break;
      case Intro:
      case Loop:
      case Outro:
        if (Objects.isNull(meterSuper)) {
          meterSuper = Config.patternDefaultMeterSuper();
        }
        if (Objects.isNull(meterSub)) {
          meterSub = Config.patternDefaultMeterSub();
        }
        if (Objects.isNull(meterSwing)) {
          meterSwing = Config.patternDefaultMeterSwing();
        }
        break;
    }
  }

  @Override
  public String toString() {
    return (Objects.nonNull(name) ? name + " " : "");
  }

}
