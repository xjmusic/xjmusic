// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.entity.common;

import io.xj.hub.entity.EntityUtils;
import io.xj.hub.util.ValueException;
import io.xj.hub.util.ValueUtils;

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
      ValueUtils.require(EntityUtils.get(event, "duration"), "Duration");
      ValueUtils.require(EntityUtils.get(event, "tones"), "Tones");
      ValueUtils.require(EntityUtils.get(event, "position"), "Position");
      ValueUtils.require(EntityUtils.get(event, "velocity"), "Velocity");
    } catch (Exception e) {
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
