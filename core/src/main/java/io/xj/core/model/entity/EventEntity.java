//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

import io.xj.core.exception.CoreException;

public interface EventEntity extends SubEntity {

  /**
   Validate presence of required properties of any EventEntity

   @param event to validate
   @throws CoreException if invalid
   */
  static void validate(EventEntity event) throws CoreException {
    if (null == event.getDuration())
      throw new CoreException("Duration is required.");

    if (null == event.getNote() || event.getNote().isEmpty())
      throw new CoreException("Note is required.");

    if (null == event.getPosition())
      throw new CoreException("Position is required.");

    if (null == event.getVelocity())
      throw new CoreException("Velocity is required.");
  }

  /**
   get Duration

   @return Duration
   */
  Double getDuration();

  /**
   get Note

   @return Note
   */
  String getNote();

  /**
   get Position

   @return Position
   */
  Double getPosition();

  /**
   get Velocity

   @return Velocity
   */
  Double getVelocity();

  /**
   set Duration of EventEntity

   @param duration to set
   @return this EventEntity (for chaining methods)
   */
  EventEntity setDuration(Double duration);

  /**
   set Note of EventEntity

   @param note to set
   @return this EventEntity (for chaining methods)
   */
  EventEntity setNote(String note);

  /**
   set Position of EventEntity

   @param position to set
   @return this EventEntity (for chaining methods)
   */
  EventEntity setPosition(Double position);

  /**
   set Velocity of EventEntity

   @param velocity to set
   @return this EventEntity (for chaining methods)
   */
  EventEntity setVelocity(Double velocity);

}
