// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.arrangement;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.ARRANGEMENT;

public class Arrangement extends Entity {


  /**
   Choice
   */
  private ULong choiceId;

  public ULong getChoiceId() {
    return choiceId;
  }

  public Arrangement setChoiceId(BigInteger choiceId) {
    this.choiceId = ULong.valueOf(choiceId);
    return this;
  }

  /**
   Voice
   */
  private ULong voiceId;

  public ULong getVoiceId() {
    return voiceId;
  }

  public Arrangement setVoiceId(BigInteger voiceId) {
    this.voiceId = ULong.valueOf(voiceId);
    return this;
  }

  /**
   Instrument
   */
  private ULong instrumentId;

  public ULong getInstrumentId() {
    return instrumentId;
  }

  public Arrangement setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = ULong.valueOf(instrumentId);
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
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

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ARRANGEMENT.CHOICE_ID, choiceId);
    fieldValues.put(ARRANGEMENT.VOICE_ID, voiceId);
    fieldValues.put(ARRANGEMENT.INSTRUMENT_ID, instrumentId);
    return fieldValues;
  }

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "arrangement";
  public static final String KEY_MANY = "arrangements";

}
