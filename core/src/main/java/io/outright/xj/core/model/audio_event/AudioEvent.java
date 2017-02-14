// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio_event;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.AUDIO_EVENT;

public class AudioEvent extends Entity {

  /**
   * Duration
   */
  private Double duration;

  public Double getDuration() {
    return duration;
  }

  public AudioEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  /**
   * Inflection
   */
  private String inflection;

  public String getInflection() {
    return inflection;
  }

  public AudioEvent setInflection(String inflection) {
    this.inflection = Purify.UpperSlug(inflection);
    return this;
  }

  /**
   * Note
   */
  private String note;

  public String getNote() {
    return note;
  }

  public AudioEvent setNote(String note) {
    this.note = Purify.Note(note);
    return this;
  }

  /**
   * Position
   */
  private Double position;

  public Double getPosition() {
    return position;
  }

  public AudioEvent setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   * Tonality
   */
  private Double tonality;

  public Double getTonality() {
    return tonality;
  }

  public AudioEvent setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  /**
   * Velocity
   */
  private Double velocity;

  public Double getVelocity() {
    return velocity;
  }

  public AudioEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  /**
   * Audio
   */
  private ULong audioId;

  public ULong getAudioId() {
    return audioId;
  }

  public AudioEvent setAudioId(BigInteger audioId) {
    this.audioId = ULong.valueOf(audioId);
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
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

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
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

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "audioEvent";
  public static final String KEY_MANY = "audioEvents";

}
