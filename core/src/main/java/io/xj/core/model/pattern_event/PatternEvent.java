// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.event.Event;
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
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class PatternEvent extends Event {
  public static final String KEY_ONE = "patternEvent";
  public static final String KEY_MANY = "patternEvents";
  private BigInteger voiceId;
  private BigInteger patternId;

  @Override
  public PatternEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public PatternEvent setInflection(String inflection) {
    this.inflection = Text.toUpperSlug(inflection);
    return this;
  }

  @Override
  public PatternEvent setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  @Override
  public PatternEvent setPosition(Double position) {
    this.position = roundPosition(position);
    return this;
  }

  @Override
  public PatternEvent setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  @Override
  public PatternEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  public BigInteger getVoiceId() {
    return voiceId;
  }

  public PatternEvent setVoiceId(BigInteger voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return patternId;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == voiceId) {
      throw new BusinessException("Voice ID is required.");
    }
    if (null == patternId) {
      throw new BusinessException("Pattern ID is required.");
    }
    super.validate();
  }

  public PatternEvent setPatternId(BigInteger patternId) {
    this.patternId = patternId;
    return this;
  }

  public BigInteger getPatternId() {
    return patternId;
  }
}
