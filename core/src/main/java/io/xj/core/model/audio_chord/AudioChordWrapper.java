// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class AudioChordWrapper {
  private AudioChord audioChord;

  public AudioChord getAudioChord() {
    return audioChord;
  }

  public AudioChordWrapper setAudioChord(AudioChord audioChord) {
    this.audioChord = audioChord;
    return this;
  }
}
