// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.chord.Chord;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AudioChord extends Chord {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "audioChord";
  public static final String KEY_MANY = "audioChords";
  /**
   Audio
   */
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

}
