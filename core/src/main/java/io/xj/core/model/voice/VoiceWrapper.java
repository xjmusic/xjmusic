// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.voice;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class VoiceWrapper extends EntityWrapper {

  // Voice
  private Voice voice;

  public Voice getVoice() {
    return voice;
  }

  public VoiceWrapper setVoice(Voice voice) {
    this.voice = voice;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Voice validate() throws BusinessException {
    if (this.voice == null) {
      throw new BusinessException("Voice is required.");
    }
    this.voice.validate();
    return this.voice;
  }

}
