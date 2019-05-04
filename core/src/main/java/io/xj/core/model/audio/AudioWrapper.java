// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AudioWrapper {
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

}
