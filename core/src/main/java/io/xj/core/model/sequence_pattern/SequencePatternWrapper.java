// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequencePatternWrapper extends EntityWrapper {

  /**
   Sequence pattern
   */
  private SequencePattern sequencePattern;

  /**
   Get sequence pattern

   @return sequence pattern
   */
  public SequencePattern getSequencePattern() {
    return sequencePattern;
  }

  /**
   Set sequence pattern

   @param sequencePattern to set
   @return this sequence pattern wrapper
   */
  public SequencePatternWrapper setSequencePattern(SequencePattern sequencePattern) {
    this.sequencePattern = sequencePattern;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public SequencePattern validate() throws BusinessException {
    if (null == sequencePattern) {
      throw new BusinessException("Sequence is required.");
    }
    sequencePattern.validate();
    return sequencePattern;
  }

}
