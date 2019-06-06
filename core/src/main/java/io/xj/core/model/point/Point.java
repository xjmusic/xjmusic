// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.point;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.util.Text;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 Point entities are used only in-memory; Point entities are not persisted to database.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Point extends EntityImpl {
  public static final String KEY_ONE = "point";
  public static final String KEY_MANY = "points";
  private BigInteger morphId;
  private BigInteger patternEventId;
  private Double position;
  private Double duration;
  private String note;

  public BigInteger getMorphId() {
    return morphId;
  }

  public Point setMorphId(BigInteger morphId) {
    this.morphId = morphId;
    return this;
  }

  public BigInteger getPatternEventId() {
    return patternEventId;
  }

  public Point setPatternEventId(BigInteger patternEventId) {
    this.patternEventId = patternEventId;
    return this;
  }

  /**
   Position from start of morph, in beats

   @return # beats
   */
  public Double getPosition() {
    return position;
  }

  public Point setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   Duration from start of this point, in beats

   @return # beats
   */
  public Double getDuration() {
    return duration;
  }

  public Point setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  public String getNote() {
    return note;
  }

  public Point setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return morphId;
  }

  @Override
  public void validate() throws CoreException {
    if (this.morphId == null) {
      throw new CoreException("Morph ID is required.");
    }
    if (this.patternEventId == null) {
      throw new CoreException("PatternEvent ID is required.");
    }
    if (this.position == null) {
      throw new CoreException("Position is required.");
    }
    if (this.duration == null || this.duration == (double) 0) {
      throw new CoreException("Duration is required.");
    }
    if (this.note == null || this.note.length() == 0) {
      throw new CoreException("Note is required.");
    }
  }

}
