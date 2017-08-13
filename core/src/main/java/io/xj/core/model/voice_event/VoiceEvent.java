// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.voice_event;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.EventEntity;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.VOICE_EVENT;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class VoiceEvent extends EventEntity {
  public static final String KEY_ONE = "voiceEvent";
  public static final String KEY_MANY = "voiceEvents";
  private ULong voiceId;

  @Override
  public VoiceEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public VoiceEvent setInflection(String inflection) {
    this.inflection = Text.toUpperSlug(inflection);
    return this;
  }

  @Override
  public VoiceEvent setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  @Override
  public VoiceEvent setPosition(Double position) {
    this.position = position;
    return this;
  }

  @Override
  public VoiceEvent setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  @Override
  public VoiceEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  public ULong getVoiceId() {
    return voiceId;
  }

  public VoiceEvent setVoiceId(BigInteger voiceId) {
    this.voiceId = ULong.valueOf(voiceId);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    super.validate();
    if (this.voiceId == null) {
      throw new BusinessException("Voice ID is required.");
    }
  }

  @Override
  public VoiceEvent setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(VOICE_EVENT.ID);
    duration = record.get(VOICE_EVENT.DURATION);
    inflection = record.get(VOICE_EVENT.INFLECTION);
    note = record.get(VOICE_EVENT.NOTE);
    position = record.get(VOICE_EVENT.POSITION);
    tonality = record.get(VOICE_EVENT.TONALITY);
    velocity = record.get(VOICE_EVENT.VELOCITY);
    voiceId = record.get(VOICE_EVENT.VOICE_ID);
    createdAt = record.get(VOICE_EVENT.CREATED_AT);
    updatedAt = record.get(VOICE_EVENT.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(VOICE_EVENT.DURATION, duration);
    fieldValues.put(VOICE_EVENT.INFLECTION, inflection);
    fieldValues.put(VOICE_EVENT.NOTE, note);
    fieldValues.put(VOICE_EVENT.POSITION, position);
    fieldValues.put(VOICE_EVENT.TONALITY, tonality);
    fieldValues.put(VOICE_EVENT.VELOCITY, velocity);
    fieldValues.put(VOICE_EVENT.VOICE_ID, voiceId);
    return fieldValues;
  }

}
