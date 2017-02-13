// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.voice;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  @Override
  public Voice validate() throws BusinessException{
    if (this.voice == null) {
      throw new BusinessException("Voice is required.");
    }
    this.voice.validate();
    return this.voice;
  }

  @Override
  public String toString() {
    return "{" +
      Voice.KEY_ONE + ":" + this.voice +
      "}";
  }
}
