// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.pattern_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PatternMemeWrapper extends EntityWrapper {

  // Pattern
  private PatternMeme patternMeme;

  public PatternMeme getPatternMeme() {
    return patternMeme;
  }

  public PatternMemeWrapper setPatternMeme(PatternMeme patternMeme) {
    this.patternMeme = patternMeme;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public PatternMeme validate() throws BusinessException {
    if (this.patternMeme == null) {
      throw new BusinessException("Pattern is required.");
    }
    this.patternMeme.validate();
    return this.patternMeme;
  }

}
