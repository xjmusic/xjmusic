// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_chord;

import io.xj.core.exception.CoreException;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class AudioChord extends EntityImpl implements Chord<AudioChord> {
  public static final String KEY_ONE = "audioChord";
  public static final String KEY_MANY = "audioChords";
  private BigInteger audioId;
  private String name;
  private Double position;

  public static AudioChord of(String name) {
    return new AudioChord().setName(name);
  }

  public BigInteger getAudioId() {
    return audioId;
  }

  public AudioChord setAudioId(BigInteger audioId) {
    this.audioId = audioId;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return audioId;
  }

  @Override
  public AudioChord setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public AudioChord setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Double getPosition() {
    return position;
  }

  @Override
  public void validate() throws CoreException {
    Chord.validate(this);
    if (Objects.isNull(audioId)) {
      throw new CoreException("Audio ID is required.");
    }
  }

  @Override
  public io.xj.music.Chord toMusical() {
    return new io.xj.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

  @Override
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  @Override
  public Boolean isChord() {
    return !isNoChord();
  }
}
