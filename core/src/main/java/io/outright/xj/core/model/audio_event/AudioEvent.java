// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio_event;

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

import static io.outright.xj.core.Tables.AUDIO_EVENT;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AudioEvent extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "audioEvent";
  public static final String KEY_MANY = "audioEvents";
  /**
   Duration
   */
  private Double duration;
  /**
   Inflection
   */
  private String inflection;
  /**
   Note
   */
  private String note;
  /**
   Position
   */
  private Double position;
  /**
   Tonality
   */
  private Double tonality;
  /**
   Velocity
   */
  private Double velocity;
  /**
   Audio
   */
  private ULong audioId;

  public Double getDuration() {
    return duration;
  }

  public AudioEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  public String getInflection() {
    return inflection;
  }

  public AudioEvent setInflection(String inflection) {
    this.inflection = Text.UpperSlug(inflection);
    return this;
  }

  public String getNote() {
    return note;
  }

  public AudioEvent setNote(String note) {
    this.note = Text.Note(note);
    return this;
  }

  public Double getPosition() {
    return position;
  }

  public AudioEvent setPosition(Double position) {
    this.position = position;
    return this;
  }

  public Double getTonality() {
    return tonality;
  }

  public AudioEvent setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  public Double getVelocity() {
    return velocity;
  }

  public AudioEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  public ULong getAudioId() {
    return audioId;
  }

  public AudioEvent setAudioId(BigInteger audioId) {
    this.audioId = ULong.valueOf(audioId);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.duration == null) {
      throw new BusinessException("Duration is required.");
    }
    if (this.inflection == null || this.inflection.length() == 0) {
      throw new BusinessException("Inflection is required.");
    }
    if (this.note == null || this.note.length() == 0) {
      throw new BusinessException("Note is required.");
    }
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
    if (this.tonality == null) {
      throw new BusinessException("Tonality is required.");
    }
    if (this.velocity == null) {
      throw new BusinessException("Velocity is required.");
    }
    if (this.audioId == null) {
      throw new BusinessException("Audio ID is required.");
    }
  }

  @Override
  public AudioEvent setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(AUDIO_EVENT.ID);
    duration = record.get(AUDIO_EVENT.DURATION);
    inflection = record.get(AUDIO_EVENT.INFLECTION);
    note = record.get(AUDIO_EVENT.NOTE);
    position = record.get(AUDIO_EVENT.POSITION);
    tonality = record.get(AUDIO_EVENT.TONALITY);
    velocity = record.get(AUDIO_EVENT.VELOCITY);
    audioId = record.get(AUDIO_EVENT.AUDIO_ID);
    createdAt = record.get(AUDIO_EVENT.CREATED_AT);
    updatedAt = record.get(AUDIO_EVENT.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(AUDIO_EVENT.DURATION, duration);
    fieldValues.put(AUDIO_EVENT.INFLECTION, inflection);
    fieldValues.put(AUDIO_EVENT.NOTE, note);
    fieldValues.put(AUDIO_EVENT.POSITION, position);
    fieldValues.put(AUDIO_EVENT.TONALITY, tonality);
    fieldValues.put(AUDIO_EVENT.VELOCITY, velocity);
    fieldValues.put(AUDIO_EVENT.AUDIO_ID, audioId);
    return fieldValues;
  }

}
