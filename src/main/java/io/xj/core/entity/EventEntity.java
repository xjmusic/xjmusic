// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.entity;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.util.Value;

public abstract class EventEntity extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES =
    ImmutableList.<String>builder()
      .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
      .add("duration")
      .add("note")
      .add("position")
      .add("velocity")
      .build();
  private Double duration;
  private String note;
  private Double position;
  private Double velocity;

  /**
   Validate presence of required properties of any EventEntity

   @param event to validate
   @throws CoreException if invalid
   */
  public static void validate(EventEntity event) throws CoreException {
    require(event.getDuration(), "Duration");
    require(event.getNote(), "Note");
    require(event.getPosition(), "Position");
    require(event.getVelocity(), "Velocity");
  }

  /**
   get Duration

   @return Duration
   */
  public Double getDuration() {
    return duration;
  }

  /**
   get Note

   @return Note
   */
  public String getNote() {
    return note;
  }

  /**
   get Position

   @return Position
   */
  public Double getPosition() {
    return position;
  }

  /**
   get Velocity

   @return Velocity
   */
  public Double getVelocity() {
    return velocity;
  }

  /**
   set Duration of EventEntity

   @param duration to set
   @return this EventEntity (for chaining methods)
   */
  public EventEntity setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  /**
   set Note of EventEntity

   @param note to set
   @return this EventEntity (for chaining methods)
   */
  public EventEntity setNote(String note) {
    this.note = note;
    return this;
  }

  /**
   set Position of EventEntity

   @param position to set
   @return this EventEntity (for chaining methods)
   */
  public EventEntity setPosition(Double position) {
    this.position = limitDecimalPrecision(position);
    return this;
  }

  /**
   set Velocity of EventEntity

   @param velocity to set
   @return this EventEntity (for chaining methods)
   */
  public EventEntity setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

}
