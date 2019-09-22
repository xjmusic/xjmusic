//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

import io.xj.core.exception.CoreException;

public interface Event extends SubEntity {

  /**
   Validate presence of required properties of any Event

   @param event to validate
   @throws CoreException if invalid
   */
  static void validate(Event event) throws CoreException {
    if (null == event.getDuration())
      throw new CoreException("Duration is required.");

    if (null == event.getName() || event.getName().isEmpty())
      throw new CoreException("Name is required.");

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
   get Name

   @return Name
   */
  String getName();

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
   set Duration of Event

   @param duration to set
   @return this Event (for chaining methods)
   */
  Event setDuration(Double duration);

  /**
   set Name of Event

   @param name to set
   @return this Event (for chaining methods)
   */
  Event setName(String name);

  /**
   set Note of Event

   @param note to set
   @return this Event (for chaining methods)
   */
  Event setNote(String note);

  /**
   set Position of Event

   @param position to set
   @return this Event (for chaining methods)
   */
  Event setPosition(Double position);

  /**
   set Velocity of Event

   @param velocity to set
   @return this Event (for chaining methods)
   */
  Event setVelocity(Double velocity);

}
