// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio_chord;

import io.outright.xj.core.app.exception.BusinessException;

import com.google.common.collect.ImmutableMap;
import org.jooq.Field;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.AUDIO_CHORD;

public class AudioChord {

  /**
   * Name
   */
  private String name;

  public String getName() {
    return name;
  }

  public AudioChord setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Audio
   */
  private ULong audioId;

  public ULong getAudioId() {
    return audioId;
  }

  public AudioChord setAudioId(BigInteger audioId) {
    this.audioId = ULong.valueOf(audioId);
    return this;
  }

  /**
   * Position
   */
  private Double position;

  public Double getPosition() {
    return position;
  }

  public AudioChord setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.audioId == null) {
      throw new BusinessException("Audio ID is required.");
    }
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(AUDIO_CHORD.NAME, name)
      .put(AUDIO_CHORD.AUDIO_ID, audioId)
      .put(AUDIO_CHORD.POSITION, position)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "name:" + this.name +
      ", audioId:" + this.audioId +
      ", position:" + this.position +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "audioChord";
  public static final String KEY_MANY = "audioChords";

}
