// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.pick;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.PICK;

public class Pick extends Entity {

  /**
   Arrangement
   */
  private ULong arrangementId;

  public ULong getArrangementId() {
    return arrangementId;
  }

  public Pick setArrangementId(BigInteger arrangementId) {
    this.arrangementId = ULong.valueOf(arrangementId);
    return this;
  }

  /**
   Morph
   */
  private ULong morphId;

  public ULong getMorphId() {
    return morphId;
  }

  public Pick setMorphId(BigInteger morphId) {
    this.morphId = ULong.valueOf(morphId);
    return this;
  }

  /**
   Audio
   */
  private ULong audioId;

  public ULong getAudioId() {
    return audioId;
  }

  public Pick setAudioId(BigInteger audioId) {
    this.audioId = ULong.valueOf(audioId);
    return this;
  }

  /**
   Start (seconds)
   */
  private Double start;

  public Double getStart() {
    return start;
  }

  public Pick setStart(Double start) {
    this.start = start;
    return this;
  }

  /**
   Length (seconds)
   */
  private Double length;

  public Double getLength() {
    return length;
  }

  public Pick setLength(Double length) {
    this.length = length;
    return this;
  }

  /**
   Amplitude
   */
  private Double amplitude;

  public Double getAmplitude() {
    return amplitude;
  }

  public Pick setAmplitude(Double amplitude) {
    this.amplitude = amplitude;
    return this;
  }

  /**
   Pitch
   */
  private Double pitch;

  public Double getPitch() {
    return pitch;
  }

  public Pick setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
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

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
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

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "pick";
  public static final String KEY_MANY = "picks";

}
