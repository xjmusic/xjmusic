// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.voice_event;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class VoiceEventWrapper extends EntityWrapper {

  // VoiceEvent
  private VoiceEvent voiceEvent;

  public VoiceEvent getVoiceEvent() {
    return voiceEvent;
  }

  public VoiceEventWrapper setVoiceEvent(VoiceEvent voiceEvent) {
    this.voiceEvent = voiceEvent;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public VoiceEvent validate() throws BusinessException {
    if (this.voiceEvent == null) {
      throw new BusinessException("voiceEvent is required.");
    }
    this.voiceEvent.validate();
    return this.voiceEvent;
  }

}
