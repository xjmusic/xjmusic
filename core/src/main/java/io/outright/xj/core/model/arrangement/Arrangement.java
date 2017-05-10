// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.arrangement;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.ARRANGEMENT;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
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
  private ULong choiceId;
  /**
   Voice
   */
  private ULong voiceId;
  /**
   Instrument
   */
  private ULong instrumentId;

  public ULong getChoiceId() {
    return choiceId;
  }

  public Arrangement setChoiceId(BigInteger choiceId) {
    this.choiceId = ULong.valueOf(choiceId);
    return this;
  }

  public ULong getVoiceId() {
    return voiceId;
  }

  public Arrangement setVoiceId(BigInteger voiceId) {
    this.voiceId = ULong.valueOf(voiceId);
    return this;
  }

  public ULong getInstrumentId() {
    return instrumentId;
  }

  public Arrangement setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = ULong.valueOf(instrumentId);
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

  @Override
  public Arrangement setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(ARRANGEMENT.ID);
    choiceId = record.get(ARRANGEMENT.CHOICE_ID);
    voiceId = record.get(ARRANGEMENT.VOICE_ID);
    instrumentId = record.get(ARRANGEMENT.INSTRUMENT_ID);
    createdAt = record.get(ARRANGEMENT.CREATED_AT);
    updatedAt = record.get(ARRANGEMENT.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ARRANGEMENT.CHOICE_ID, choiceId);
    fieldValues.put(ARRANGEMENT.VOICE_ID, voiceId);
    fieldValues.put(ARRANGEMENT.INSTRUMENT_ID, instrumentId);
    return fieldValues;
  }

}
