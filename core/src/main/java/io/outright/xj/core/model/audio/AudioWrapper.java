// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  @Override
  public Audio validate() throws BusinessException{
    if (this.audio == null) {
      throw new BusinessException("Audio is required.");
    }
    this.audio.validate();
    return this.audio;
  }

}
