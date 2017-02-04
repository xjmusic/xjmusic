// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.voice_event;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.VoiceEventRecord;
import io.outright.xj.core.util.Purify;

import com.google.common.collect.ImmutableMap;

import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.VOICE_EVENT;

public class VoiceEvent {

  /**
   * Duration
   */
  private Double duration;

  public Double getDuration() {
    return duration;
  }

  public VoiceEvent setDuration(Double duration) {
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

  public VoiceEvent setInflection(String inflection) {
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

  public VoiceEvent setNote(String note) {
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

  public VoiceEvent setPosition(Double position) {
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

  public VoiceEvent setTonality(Double tonality) {
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

  public VoiceEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  /**
   * Voice
   */
  private ULong voiceId;

  public ULong getVoiceId() {
    return voiceId;
  }

  public VoiceEvent setVoiceId(BigInteger voiceId) {
    this.voiceId = ULong.valueOf(voiceId);
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
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
    if (this.voiceId == null) {
      throw new BusinessException("Voice ID is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(VOICE_EVENT.DURATION, duration)
      .put(VOICE_EVENT.INFLECTION, inflection)
      .put(VOICE_EVENT.NOTE, note)
      .put(VOICE_EVENT.POSITION, position)
      .put(VOICE_EVENT.TONALITY, tonality)
      .put(VOICE_EVENT.VELOCITY, velocity)
      .put(VOICE_EVENT.VOICE_ID, voiceId)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "duration:" + duration +
      ", inflection:" + inflection +
      ", note:" + note +
      ", position:" + position +
      ", tonality:" + tonality +
      ", velocity:" + velocity +
      ", voiceId:" + voiceId +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "voiceEvent";
  public static final String KEY_MANY = "voiceEvents";

}
