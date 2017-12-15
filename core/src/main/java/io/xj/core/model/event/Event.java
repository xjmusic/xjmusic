// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

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
public class Event extends Entity {
  public static final String KEY_ONE = "event";
  public static final String KEY_MANY = "events";
  protected Double duration;
  protected String inflection;
  protected String note;
  protected Double position;
  protected Double tonality;
  protected Double velocity;

  public Double getDuration() {
    return duration;
  }

  public Event setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  public String getInflection() {
    return inflection;
  }

  public Event setInflection(String inflection) {
    this.inflection = inflection;
    return this;
  }

  public String getNote() {
    return note;
  }

  public Event setNote(String note) {
    this.note = note;
    return this;
  }

  public Double getPosition() {
    return position;
  }

  public Event setPosition(Double position) {
    this.position = position;
    return this;
  }

  public Double getTonality() {
    return tonality;
  }

  public Event setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  public Double getVelocity() {
    return velocity;
  }

  public Event setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == duration) throw new BusinessException("Duration is required.");
    if (null == inflection || inflection.isEmpty())
      throw new BusinessException("Inflection is required.");
    if (null == note || note.isEmpty()) throw new BusinessException("Note is required.");
    if (null == position) throw new BusinessException("Position is required.");
    if (null == tonality) throw new BusinessException("Tonality is required.");
    if (null == velocity) throw new BusinessException("Velocity is required.");
  }

}
