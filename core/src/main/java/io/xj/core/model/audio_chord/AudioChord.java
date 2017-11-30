// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.ChordEntity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.AUDIO_CHORD;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AudioChord extends ChordEntity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "audioChord";
  public static final String KEY_MANY = "audioChords";
  /**
   Audio
   */
  private ULong audioId;

  public AudioChord setName(String name) {
    this.name = name;
    return this;
  }

  public ULong getAudioId() {
    return audioId;
  }

  public AudioChord setAudioId(BigInteger audioId) {
    this.audioId = ULong.valueOf(audioId);
    return this;
  }

  public AudioChord setPosition(Double position) {
    this.position = position;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.audioId == null) {
      throw new BusinessException("Audio ID is required.");
    }
    super.validate();
  }

  @Override
  public AudioChord setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(AUDIO_CHORD.ID);
    name = record.get(AUDIO_CHORD.NAME);
    audioId = record.get(AUDIO_CHORD.AUDIO_ID);
    position = record.get(AUDIO_CHORD.POSITION);
    createdAt = record.get(AUDIO_CHORD.CREATED_AT);
    updatedAt = record.get(AUDIO_CHORD.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(AUDIO_CHORD.NAME, name);
    fieldValues.put(AUDIO_CHORD.AUDIO_ID, audioId);
    fieldValues.put(AUDIO_CHORD.POSITION, position);
    return fieldValues;
  }

}
