// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AudioWrapper extends EntityWrapper {

  // Audio
  private Audio audio;

  public Audio getAudio() {
    return audio;
  }

  public AudioWrapper setAudio(Audio audio) {
    this.audio = audio;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Audio validate() throws BusinessException {
    if (this.audio == null) {
      throw new BusinessException("Audio is required.");
    }
    this.audio.validate();
    return this.audio;
  }

}
