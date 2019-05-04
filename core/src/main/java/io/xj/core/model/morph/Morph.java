// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.morph;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE, THERE IS NO DAO! Morph entities are used only in-memory; Morph entities are not persisted to database.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Morph extends EntityImpl {

  public static final String KEY_ONE = "morph";
  public static final String KEY_MANY = "morphs";
  private BigInteger arrangementId;
  private Double position;
  private String note;
  private Double duration;

  public BigInteger getArrangementId() {
    return arrangementId;
  }

  public Morph setArrangementId(BigInteger arrangementId) {
    this.arrangementId = arrangementId;
    return this;
  }

  public Double getPosition() {
    return position;
  }

  /**
   position in beats from start of segment

   @param position in beats
   @return morph
   */
  public Morph setPosition(Double position) {
    this.position = position;
    return this;
  }

  public String getNote() {
    return note;
  }

  public Morph setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  public Double getDuration() {
    return duration;
  }

  /**
   duration in beats from start of segment

   @param duration in beats
   @return morph
   */
  public Morph setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return arrangementId;
  }

  @Override
  public void validate() throws CoreException {
    if (Objects.isNull(arrangementId)) {
      throw new CoreException("Arrangement ID is required.");
    }
    if (Objects.isNull(position)) {
      throw new CoreException("Position is required.");
    }
    if (Objects.isNull(duration)) {
      throw new CoreException("Duration is required.");
    }
    if (Objects.isNull(note) || note.isEmpty()) {
      throw new CoreException("Note is required.");
    }
  }

}
