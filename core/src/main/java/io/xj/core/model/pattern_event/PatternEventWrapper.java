// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PatternEventWrapper extends EntityWrapper {

  // PatternEvent
  private PatternEvent patternEvent;

  public PatternEvent getPatternEvent() {
    return patternEvent;
  }

  public PatternEventWrapper setPatternEvent(PatternEvent patternEvent) {
    this.patternEvent = patternEvent;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public PatternEvent validate() throws BusinessException {
    if (this.patternEvent == null) {
      throw new BusinessException("patternEvent is required.");
    }
    this.patternEvent.validate();
    return this.patternEvent;
  }

}
