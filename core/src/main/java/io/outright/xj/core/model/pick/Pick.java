// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.pick;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.PICK;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Pick extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "pick";
  public static final String KEY_MANY = "picks";
  /**
   Arrangement
   */
  private ULong arrangementId;
  /**
   Morph
   */
  private ULong morphId;
  /**
   Audio
   */
  private ULong audioId;
  /**
   Start (seconds)
   */
  private Double start;
  /**
   Length (seconds)
   */
  private Double length;
  /**
   Amplitude
   */
  private Double amplitude;
  /**
   Pitch
   */
  private Double pitch;

  public ULong getArrangementId() {
    return arrangementId;
  }

  public Pick setArrangementId(BigInteger arrangementId) {
    this.arrangementId = ULong.valueOf(arrangementId);
    return this;
  }

  public ULong getMorphId() {
    return morphId;
  }

  public Pick setMorphId(BigInteger morphId) {
    this.morphId = ULong.valueOf(morphId);
    return this;
  }

  public ULong getAudioId() {
    return audioId;
  }

  public Pick setAudioId(BigInteger audioId) {
    this.audioId = ULong.valueOf(audioId);
    return this;
  }

  public Double getStart() {
    return start;
  }

  public Pick setStart(Double start) {
    this.start = start;
    return this;
  }

  public Double getLength() {
    return length;
  }

  public Pick setLength(Double length) {
    this.length = length;
    return this;
  }

  public Double getAmplitude() {
    return amplitude;
  }

  public Pick setAmplitude(Double amplitude) {
    this.amplitude = amplitude;
    return this;
  }

  public Double getPitch() {
    return pitch;
  }

  public Pick setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.arrangementId == null) {
      throw new BusinessException("Arrangement ID is required.");
    }
    if (this.morphId == null) {
      throw new BusinessException("Morph ID is required.");
    }
    if (this.audioId == null) {
      throw new BusinessException("Audio ID is required.");
    }
    if (this.start == null) {
      throw new BusinessException("Start is required.");
    }
    if (this.length == null || this.length == 0) {
      throw new BusinessException("Length is required.");
    }
    if (this.amplitude == null || this.amplitude == 0) {
      throw new BusinessException("Amplitude is required.");
    }
    if (this.pitch == null || this.pitch == 0) {
      throw new BusinessException("Pitch is required.");
    }
  }

  @Override
  public Pick setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(PICK.ID);
    arrangementId = record.get(PICK.ARRANGEMENT_ID);
    morphId = record.get(PICK.MORPH_ID);
    audioId = record.get(PICK.AUDIO_ID);
    start = record.get(PICK.START);
    length = record.get(PICK.LENGTH);
    amplitude = record.get(PICK.AMPLITUDE);
    pitch = record.get(PICK.PITCH);
    createdAt = record.get(PICK.CREATED_AT);
    updatedAt = record.get(PICK.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PICK.ARRANGEMENT_ID, arrangementId);
    fieldValues.put(PICK.MORPH_ID, morphId);
    fieldValues.put(PICK.AUDIO_ID, audioId);
    fieldValues.put(PICK.START, start);
    fieldValues.put(PICK.LENGTH, length);
    fieldValues.put(PICK.AMPLITUDE, amplitude);
    fieldValues.put(PICK.PITCH, pitch);
    return fieldValues;
  }

}
