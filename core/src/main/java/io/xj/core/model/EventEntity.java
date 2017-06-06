// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.app.exception.BusinessException;

import org.apache.commons.codec.language.DoubleMetaphone;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public abstract class EventEntity extends Entity {
  public static final String KEY_ONE = "event";
  public static final String KEY_MANY = "events";
  private static final double SIMILARITY_SCORE_MATCHING_INFLECTION = 1.5;
  private static final double SIMILARITY_SCORE_VELOCITY = 0.2;
  private static final double SIMILARITY_SCORE_DURATION = 0.3;
  private static final double SIMILARITY_SCORE_TONALITY = 0.6;
  protected Double duration;
  protected String inflection;
  protected String note;
  protected Double position;
  protected Double tonality;
  protected Double velocity;

  public Double getDuration() {
    return duration;
  }

  public abstract EventEntity setDuration(Double duration);

  public String getInflection() {
    return inflection;
  }

  public abstract EventEntity setInflection(String inflection);

  public String getNote() {
    return note;
  }

  public abstract EventEntity setNote(String note);

  public Double getPosition() {
    return position;
  }

  public abstract EventEntity setPosition(Double position);

  public Double getTonality() {
    return tonality;
  }

  public abstract EventEntity setTonality(Double tonality);

  public Double getVelocity() {
    return velocity;
  }

  public abstract EventEntity setVelocity(Double velocity);

  @Override
  public void validate() throws BusinessException {
    if (this.duration == null) {
      throw new BusinessException("Duration is required.");
    }
    if (this.inflection == null || this.inflection.length() == 0) {
      throw new BusinessException("Inflection is required.");
    }
    if (this.note == null || this.note.length() == 0) {
      throw new BusinessException("Note is required.");
    }
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
    if (this.tonality == null) {
      throw new BusinessException("Tonality is required.");
    }
    if (this.velocity == null) {
      throw new BusinessException("Velocity is required.");
    }
  }

  /**
   [#252] Similarity between two events implements Double Metaphone phonetic similarity algorithm

   @param event1 to compare
   @param event2 to compare
   @return score
   */
  public static double similarity(EventEntity event1, EventEntity event2) {
    double score = 0;
    DoubleMetaphone dm = new DoubleMetaphone();

    // score a phonetically matching inflection
    if (dm.isDoubleMetaphoneEqual(event1.getInflection(), event2.getInflection()))
      score += SIMILARITY_SCORE_MATCHING_INFLECTION;

    // score velocity similarity
    score += (1 - Math.abs(event2.getVelocity() - event1.getVelocity())) * SIMILARITY_SCORE_VELOCITY;

    // score duration similarity
    score += (1 - Math.abs(event2.getDuration() - event1.getDuration())) * SIMILARITY_SCORE_DURATION;

    // score tonality similarity
    score += (1 - Math.abs(event2.getTonality() - event1.getTonality())) * SIMILARITY_SCORE_TONALITY;

    // TODO: score matching adj symbol?

    return score;
  }
}
