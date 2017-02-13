// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio_event;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class AudioEventWrapper extends EntityWrapper {

  // AudioEvent
  private AudioEvent audioEvent;
  public AudioEvent getAudioEvent() {
    return audioEvent;
  }
  public AudioEventWrapper setAudioEvent(AudioEvent audioEvent) {
    this.audioEvent = audioEvent;
    return this;
  }

  /**
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public AudioEvent validate() throws BusinessException{
    if (this.audioEvent == null) {
      throw new BusinessException("audioEvent is required.");
    }
    this.audioEvent.validate();
    return this.audioEvent;
  }

  @Override
  public String toString() {
    return "{" +
      AudioEvent.KEY_ONE + ":" + this.audioEvent +
      "}";
  }
}
