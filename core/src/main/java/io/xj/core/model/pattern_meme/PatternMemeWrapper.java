// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

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
  @Override
  public PatternMeme validate() throws BusinessException {
    if (this.patternMeme == null) {
      throw new BusinessException("patternMeme is required.");
    }
    this.patternMeme.validate();
    return this.patternMeme;
  }

}
