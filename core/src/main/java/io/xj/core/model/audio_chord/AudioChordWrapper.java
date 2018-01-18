// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AudioChordWrapper extends EntityWrapper {

  // AudioChord
  private AudioChord audioChord;

  public AudioChord getAudioChord() {
    return audioChord;
  }

  public AudioChordWrapper setAudioChord(AudioChord audioChord) {
    this.audioChord = audioChord;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public AudioChord validate() throws BusinessException {
    if (this.audioChord == null) {
      throw new BusinessException("audioChord is required.");
    }
    this.audioChord.validate();
    return this.audioChord;
  }

}
