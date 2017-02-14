// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public AudioChord validate() throws BusinessException {
    if (this.audioChord == null) {
      throw new BusinessException("audioChord is required.");
    }
    this.audioChord.validate();
    return this.audioChord;
  }

}
