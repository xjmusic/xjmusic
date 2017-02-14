// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.point;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.POINT;

public class Point extends Entity {

  /**
   * Morph
   */
  private ULong morphId;

  public ULong getMorphId() {
    return morphId;
  }

  public Point setMorphId(BigInteger morphId) {
    this.morphId = ULong.valueOf(morphId);
    return this;
  }

  /**
   * VoiceEvent
   */
  private ULong voiceEventId;

  public ULong getVoiceEventId() {
    return voiceEventId;
  }

  public Point setVoiceEventId(BigInteger voiceEventId) {
    this.voiceEventId = ULong.valueOf(voiceEventId);
    return this;
  }

  /**
   * Position (beats)
   */
  private Double position;

  public Double getPosition() {
    return position;
  }

  public Point setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   * Duration (beats)
   */
  private Double duration;

  public Double getDuration() {
    return duration;
  }

  public Point setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  /**
   * Note
   */
  private String note;

  public String getNote() {
    return note;
  }

  public Point setNote(String note) {
    this.note = Purify.Note(note);
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  @Override
  public void validate() throws BusinessException {
    if (this.morphId == null) {
      throw new BusinessException("Morph ID is required.");
    }
    if (this.voiceEventId == null) {
      throw new BusinessException("VoiceEvent ID is required.");
    }
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
    if (this.duration == null || this.duration == 0) {
      throw new BusinessException("Duration is required.");
    }
    if (this.note == null || this.note.length() == 0) {
      throw new BusinessException("Note is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();

    fieldValues.put(POINT.MORPH_ID, morphId);
    fieldValues.put(POINT.VOICE_EVENT_ID, voiceEventId);
    fieldValues.put(POINT.POSITION, position);
    fieldValues.put(POINT.DURATION, duration);
    fieldValues.put(POINT.NOTE, note);
    return fieldValues;
  }

  @Override
  public String toString() {
    return "{" +
      "morphId:" + this.morphId +
      ", voiceEventId:" + this.voiceEventId +
      ", position:" + this.position +
      ", duration:" + this.duration +
      ", note:" + this.note +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "point";
  public static final String KEY_MANY = "points";

}
