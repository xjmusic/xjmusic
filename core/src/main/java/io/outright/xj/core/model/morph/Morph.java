// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.morph;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.MORPH;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Morph extends Entity {


  /**
   For use in maps.
   */
  public static final String KEY_ONE = "morph";
  public static final String KEY_MANY = "morphs";
  /**
   Arrangement
   */
  private ULong arrangementId;
  /**
   Position
   */
  private Double position;
  /**
   Note
   */
  private String note;
  /**
   Duration
   */
  private Double duration;

  public ULong getArrangementId() {
    return arrangementId;
  }

  public Morph setArrangementId(BigInteger arrangementId) {
    this.arrangementId = ULong.valueOf(arrangementId);
    return this;
  }

  public Double getPosition() {
    return position;
  }

  public Morph setPosition(Double position) {
    this.position = position;
    return this;
  }

  public String getNote() {
    return note;
  }

  public Morph setNote(String note) {
    this.note = Text.Note(note);
    return this;
  }

  public Double getDuration() {
    return duration;
  }

  public Morph setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

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

  @Override
  public Morph setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(MORPH.ID);
    arrangementId = record.get(MORPH.ARRANGEMENT_ID);
    position = record.get(MORPH.POSITION);
    note = record.get(MORPH.NOTE);
    duration = record.get(MORPH.DURATION);
    createdAt = record.get(MORPH.CREATED_AT);
    updatedAt = record.get(MORPH.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(MORPH.ARRANGEMENT_ID, arrangementId);
    fieldValues.put(MORPH.POSITION, position);
    fieldValues.put(MORPH.NOTE, note);
    fieldValues.put(MORPH.DURATION, duration);
    return fieldValues;
  }

}
