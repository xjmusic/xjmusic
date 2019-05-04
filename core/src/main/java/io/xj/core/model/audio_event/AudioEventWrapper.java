// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_event;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AudioEventWrapper {
  private AudioEvent audioEvent;

  public AudioEvent getAudioEvent() {
    return audioEvent;
  }

  public AudioEventWrapper setAudioEvent(AudioEvent audioEvent) {
    this.audioEvent = audioEvent;
    return this;
  }
}
