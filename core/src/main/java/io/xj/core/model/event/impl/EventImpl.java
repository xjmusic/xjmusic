//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.event.impl;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.event.Event;
import io.xj.core.util.Value;

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
public abstract class EventImpl extends EntityImpl implements Event {
  protected Double duration;
  protected String inflection;
  protected String note;
  protected Double position;
  protected Double tonality;
  protected Double velocity;

  @Override public Double getDuration() {
    return duration;
  }

  @Override public Event setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override public String getInflection() {
    return inflection;
  }

  @Override public Event setInflection(String inflection) {
    this.inflection = inflection;
    return this;
  }

  @Override public String getNote() {
    return note;
  }

  @Override public Event setNote(String note) {
    this.note = note;
    return this;
  }

  @Override public Double getPosition() {
    return position;
  }

  @Override public Event setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override public Double getTonality() {
    return tonality;
  }

  @Override public Event setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  @Override public Double getVelocity() {
    return velocity;
  }

  @Override public Event setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    if (null == duration) throw new CoreException("Duration is required.");
    if (null == inflection || inflection.isEmpty())
      throw new CoreException("Inflection is required.");
    if (null == note || note.isEmpty()) throw new CoreException("Note is required.");
    if (null == position) throw new CoreException("Position is required.");
    if (null == tonality) throw new CoreException("Tonality is required.");
    if (null == velocity) throw new CoreException("Velocity is required.");
  }

}
