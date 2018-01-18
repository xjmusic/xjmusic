// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
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
   Validate data.

   @throws BusinessException if invalid.
   */
  public AudioEvent validate() throws BusinessException {
    if (this.audioEvent == null) {
      throw new BusinessException("audioEvent is required.");
    }
    this.audioEvent.validate();
    return this.audioEvent;
  }

}
