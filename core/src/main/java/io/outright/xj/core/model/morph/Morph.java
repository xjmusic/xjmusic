// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.morph;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.MORPH;

public class Morph extends Entity {


  /**
   Arrangement
   */
  private ULong arrangementId;

  public ULong getArrangementId() {
    return arrangementId;
  }

  public Morph setArrangementId(BigInteger arrangementId) {
    this.arrangementId = ULong.valueOf(arrangementId);
    return this;
  }

  /**
   Position
   */
  private Double position;

  public Double getPosition() {
    return position;
  }

  public Morph setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   Note
   */
  private String note;

  public String getNote() {
    return note;
  }

  public Morph setNote(String note) {
    this.note = Purify.Note(note);
    return this;
  }

  /**
   Duration
   */
  private Double duration;

  public Double getDuration() {
    return duration;
  }

  public Morph setDuration(Double duration) {
    this.duration = duration;
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
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
    if (this.duration == null) {
      throw new BusinessException("Duration is required.");
    }
    if (this.note == null || this.note.length() == 0) {
      throw new BusinessException("Note is required.");
    }
  }

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(MORPH.ARRANGEMENT_ID, arrangementId);
    fieldValues.put(MORPH.POSITION, position);
    fieldValues.put(MORPH.NOTE, note);
    fieldValues.put(MORPH.DURATION, duration);
    return fieldValues;
  }

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "morph";
  public static final String KEY_MANY = "morphs";

}
