// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase_event;

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
public class PhaseEvent extends Event {
  public static final String KEY_ONE = "phaseEvent";
  public static final String KEY_MANY = "phaseEvents";
  private BigInteger voiceId;
  private BigInteger phaseId;

  @Override
  public PhaseEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public PhaseEvent setInflection(String inflection) {
    this.inflection = Text.toUpperSlug(inflection);
    return this;
  }

  @Override
  public PhaseEvent setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  @Override
  public PhaseEvent setPosition(Double position) {
    this.position = roundPosition(position);
    return this;
  }

  @Override
  public PhaseEvent setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  @Override
  public PhaseEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  public BigInteger getVoiceId() {
    return voiceId;
  }

  public PhaseEvent setVoiceId(BigInteger voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return phaseId;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == voiceId) {
      throw new BusinessException("Voice ID is required.");
    }
    if (null == phaseId) {
      throw new BusinessException("Phase ID is required.");
    }
    super.validate();
  }

  public PhaseEvent setPhaseId(BigInteger phaseId) {
    this.phaseId = phaseId;
    return this;
  }

  public BigInteger getPhaseId() {
    return phaseId;
  }
}
