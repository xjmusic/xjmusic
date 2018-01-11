// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.arrangement;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Arrangement extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "arrangement";
  public static final String KEY_MANY = "arrangements";
  /**
   Choice
   */
  private BigInteger choiceId;
  /**
   Voice
   */
  private BigInteger voiceId;
  /**
   Instrument
   */
  private BigInteger instrumentId;

  public BigInteger getChoiceId() {
    return choiceId;
  }

  public Arrangement setChoiceId(BigInteger choiceId) {
    this.choiceId = choiceId;
    return this;
  }

  public BigInteger getVoiceId() {
    return voiceId;
  }

  public Arrangement setVoiceId(BigInteger voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  public BigInteger getInstrumentId() {
    return instrumentId;
  }

  public Arrangement setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.choiceId == null) {
      throw new BusinessException("Choice ID is required.");
    }
    if (this.voiceId == null) {
      throw new BusinessException("Voice ID is required.");
    }
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
  }

}
