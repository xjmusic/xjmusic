// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.voice;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class VoiceWrapper {
  private Voice voice;

  public Voice getVoice() {
    return voice;
  }

  public VoiceWrapper setVoice(Voice voice) {
    this.voice = voice;
    return this;
  }
}
