// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

import java.util.Objects;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequencePatternMemeWrapper extends EntityWrapper {

  // Pattern
  private SequencePatternMeme sequencePatternMeme;

  public SequencePatternMeme getSequencePatternMeme() {
    return sequencePatternMeme;
  }

  public SequencePatternMemeWrapper setSequencePatternMeme(SequencePatternMeme sequencePatternMeme) {
    this.sequencePatternMeme = sequencePatternMeme;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public SequencePatternMeme validate() throws BusinessException {
    if (Objects.isNull(sequencePatternMeme)) {
      throw new BusinessException("sequencePatternMeme is required.");
    }
    sequencePatternMeme.validate();
    return sequencePatternMeme;
  }

}
