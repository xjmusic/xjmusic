// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequenceWrapper extends EntityWrapper {

  // Sequence
  private Sequence sequence;

  public Sequence getSequence() {
    return sequence;
  }

  public SequenceWrapper setSequence(Sequence sequence) {
    this.sequence = sequence;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Sequence validate() throws BusinessException {
    if (this.sequence == null) {
      throw new BusinessException("Sequence is required.");
    }
    this.sequence.validate();
    return this.sequence;
  }

}
