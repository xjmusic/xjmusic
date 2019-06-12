// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Event;
import io.xj.core.model.instrument.impl.InstrumentSubEntity;
import io.xj.core.util.Text;
import io.xj.core.util.Value;

import java.util.UUID;

/**
 + [#166708597] Audio model handles all of its own entities
 */
public class AudioEvent extends InstrumentSubEntity implements Event {
  private UUID audioId;
  private Double duration;
  private String inflection;
  private String note;
  private Double position;
  private Double velocity;

  /**
   get AudioId

   @return AudioId
   */
  public UUID getAudioId() {
    return audioId;
  }

  @Override
  public Double getDuration() {
    return duration;
  }

  @Override
  public String getInflection() {
    return inflection;
  }

  @Override
  public String getNote() {
    return note;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("duration")
      .add("inflection")
      .add("note")
      .add("position")
      .add("velocity")
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
  public Double getVelocity() {
    return velocity;
  }

  /**
   set Audio

   @param audio to set
   @return this AudioEvent (for chaining methods)
   */
  public AudioEvent setAudio(Audio audio) {
    return setAudioId(audio.getId());
  }

  /**
   set AudioId

   @param audioId to set
   @return this AudioEvent (for chaining methods)
   */
  public AudioEvent setAudioId(UUID audioId) {
    this.audioId = audioId;
    return this;
  }

  @Override
  public AudioEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public AudioEvent setInflection(String inflection) {
    this.inflection = Text.toUpperSlug(inflection);
    return this;
  }

  @Override
  public AudioEvent setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  @Override
  public AudioEvent setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }


  @Override
  public AudioEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  @Override
  public AudioEvent validate() throws CoreException {
    Event.validate(this);
    require(audioId, "Audio ID");
    return this;
  }
}
