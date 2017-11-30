// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.audio_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EventEntity;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.AUDIO_EVENT;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AudioEvent extends EventEntity {
  public static final String KEY_ONE = "audioEvent";
  public static final String KEY_MANY = "audioEvents";
  private ULong audioId;

  @Override
  public AudioEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public AudioEvent setInflection(String inflection) {
    this.inflection = Text.toUpperSlug(inflection);
    return this;
  }

  @Override
  public AudioEvent setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  @Override
  public AudioEvent setPosition(Double position) {
    this.position = position;
    return this;
  }

  @Override
  public AudioEvent setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  @Override
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
    super.validate();
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
