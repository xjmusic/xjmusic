// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

import java.util.Objects;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AudioWrapper extends EntityWrapper {
  private Audio audio;

  /**
   Get Audio
   @return audio
   */
  public Audio getAudio() {
    return audio;
  }

  /**
   Set audio
   @param audio to set
   @return audio wrapper
   */
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
    if (Objects.isNull(audio)) {
      throw new BusinessException("Audio is required.");
    }
    audio.validate();
    return audio;
  }

}
