// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity.common;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

public abstract class EventEntity {
  private Double duration;
  private String note;
  private Double position;
  private Double velocity;

  /**
   Validate presence of required properties of any EventEntity

   @param event to validate
   @throws ValueException if invalid
   */
  public static void validate(Object event) throws ValueException {
    try {
      Value.require(Entities.get(event, "duration"), "Duration");
      Value.require(Entities.get(event, "note"), "Note");
      Value.require(Entities.get(event, "position"), "Position");
      Value.require(Entities.get(event, "velocity"), "Velocity");
    } catch (EntityException e) {
      throw new ValueException(e);
    }
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
    this.position = position;
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
