// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Chord;
import io.xj.core.model.instrument.impl.InstrumentSubEntity;
import io.xj.core.util.Value;

import java.util.UUID;

/**
 + [#166708597] Audio model handles all of its own entities
 */
public class AudioChord extends InstrumentSubEntity implements Chord {
  private UUID audioId;
  private String name;
  private Double position;

  /**
   Get AudioChord of a specified name

   @param name to get AudioChord of
   @return AudioChord
   */
  public static AudioChord of(String name) {
    return new AudioChord().setName(name);
  }

  /**
   get Audio UUID

   @return Audio UUID
   */
  public UUID getAudioId() {
    return audioId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .add("position")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Audio.class)
      .build();
  }

  @Override
  public Double getPosition() {
    return position;
  }

  @Override
  public Boolean isChord() {
    return !isNoChord();
  }

  @Override
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  /**
   Set Audio UUID by providing the parent Audio

   @param audio from which to set audio id
   @return this AudioChord (for chaining methods)
   */
  public AudioChord setAudio(Audio audio) {
    return setAudioId(audio.getId());
  }

  /**
   Set Audio UUID

   @param audioId to set
   @return this AudioChord (for chaining methods)
   */
  public AudioChord setAudioId(UUID audioId) {
    this.audioId = audioId;
    return this;
  }

  @Override
  public AudioChord setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public AudioChord setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
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
  public AudioChord validate() throws CoreException {
    Chord.validate(this);

    require(audioId, "Audio ID");

    return this;
  }
}
