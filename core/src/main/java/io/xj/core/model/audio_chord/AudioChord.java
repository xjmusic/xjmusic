// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.chord.Chord;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AudioChord extends Chord {
  public static final String KEY_ONE = "audioChord";
  public static final String KEY_MANY = "audioChords";

  private BigInteger audioId;

  public AudioChord setName(String name) {
    this.name = name;
    return this;
  }

  public BigInteger getAudioId() {
    return audioId;
  }

  public AudioChord setAudioId(BigInteger audioId) {
    this.audioId = audioId;
    return this;
  }

  public BigInteger getParentId() {
    return audioId;
  }

  public AudioChord setPosition(Integer position) {
    this.position = position;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (Objects.isNull(audioId)) {
      throw new BusinessException("Audio ID is required.");
    }
    super.validate();
  }

  @Override
  public AudioChord of(String name) {
    return new AudioChord().setName(name);
  }

}
