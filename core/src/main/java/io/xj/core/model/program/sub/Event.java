// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.EventEntity;
import io.xj.core.model.program.impl.ProgramSubEntity;
import io.xj.core.util.Text;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166690830] Pattern model handles all of its own entities
 */
public class Event extends ProgramSubEntity implements EventEntity {
  protected Double duration;
  protected String note;
  protected Double position;
  protected Double velocity;
  private UUID patternId;
  private UUID trackId;

  @Override
  public Double getDuration() {
    return duration;
  }
  
  @Override
  public String getNote() {
    return note;
  }

  /**
   Get Pattern UUID

   @return Pattern UUID
   */
  public UUID getPatternId() {
    return patternId;
  }

  /**
   Get Track UUID

   @return Track UUID
   */
  public UUID getTrackId() {
    return trackId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("duration")
      .add("note")
      .add("position")
      .add("velocity")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Pattern.class)
      .add(Track.class)
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

  @Override
  public Event setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public Event setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  /**
   Set Pattern UUID by providing the parent Pattern

   @param pattern to set UUID of
   @return this Event (for chaining methods)
   */
  public Event setPattern(Pattern pattern) {
    setPatternId(pattern.getId());
    return this;
  }

  /**
   Set Pattern UUID

   @param patternId to set
   @return this Event (for chaining methods)
   */
  public Event setPatternId(UUID patternId) {
    this.patternId = patternId;
    return this;
  }

  /**
   Set Track UUID by providing the parent Track

   @param track to set UUID of
   @return this Event (for chaining methods)
   */
  public Event setTrack(Track track) {
    setTrackId(track.getId());
    return this;
  }

  /**
   Set Track UUID

   @param trackId to set
   @return this Event (for chaining methods)
   */
  public Event setTrackId(UUID trackId) {
    this.trackId = trackId;
    return this;
  }

  @Override
  public Event setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override
  public Event setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public Event setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  @Override
  public Event setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public Event validate() throws CoreException {
    super.validate();
    EventEntity.validate(this);
    require(patternId, "Pattern ID");
    require(trackId, "Track ID");

    return this;
  }
}
