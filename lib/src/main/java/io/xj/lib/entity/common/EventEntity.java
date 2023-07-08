// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity.common;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

public abstract class EventEntity {
  Double duration;
  String tones;
  Double position;
  Double velocity;

  /**
   Validate presence of required properties of any EventEntity

   @param event to validate
   @throws ValueException if invalid
   */
  public static void validate(Object event) throws ValueException {
    try {
      Values.require(Entities.get(event, "duration"), "Duration");
      Values.require(Entities.get(event, "tones"), "Tones");
      Values.require(Entities.get(event, "position"), "Position");
      Values.require(Entities.get(event, "velocity"), "Velocity");
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
   set Duration of EventEntity

   @param duration to set
   @return this EventEntity (for chaining methods)
   */
  public EventEntity setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  /**
   get Note

   @return Note
   */
  public String getTones() {
    return tones;
  }

  /**
   set Note of EventEntity

   @param tones to set
   @return this EventEntity (for chaining methods)
   */
  public EventEntity setTones(String tones) {
    this.tones = tones;
    return this;
  }

  /**
   get Position

   @return Position
   */
  public Double getPosition() {
    return position;
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
   get Velocity

   @return Velocity
   */
  public Double getVelocity() {
    return velocity;
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
