// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.voice_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
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
