// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PatternChordWrapper extends EntityWrapper {

  // PatternChord
  private PatternChord patternChord;

  public PatternChord getPatternChord() {
    return patternChord;
  }

  public PatternChordWrapper setPatternChord(PatternChord patternChord) {
    this.patternChord = patternChord;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public PatternChord validate() throws BusinessException {
    if (this.patternChord == null) {
      throw new BusinessException("patternChord is required.");
    }
    this.patternChord.validate();
    return this.patternChord;
  }

}
