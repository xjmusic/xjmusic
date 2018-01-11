// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PatternWrapper extends EntityWrapper {

  // Pattern
  private Pattern pattern;

  public Pattern getPattern() {
    return pattern;
  }

  public PatternWrapper setPattern(Pattern pattern) {
    this.pattern = pattern;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public Pattern validate() throws BusinessException {
    if (this.pattern == null) {
      throw new BusinessException("Pattern is required.");
    }
    this.pattern.validate();
    return this.pattern;
  }

}
