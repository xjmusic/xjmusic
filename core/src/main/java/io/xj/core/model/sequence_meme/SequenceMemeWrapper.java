// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequenceMemeWrapper extends EntityWrapper {

  // Sequence
  private SequenceMeme sequenceMeme;

  public SequenceMeme getSequenceMeme() {
    return sequenceMeme;
  }

  public SequenceMemeWrapper setSequenceMeme(SequenceMeme sequenceMeme) {
    this.sequenceMeme = sequenceMeme;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public SequenceMeme validate() throws BusinessException {
    if (this.sequenceMeme == null) {
      throw new BusinessException("Sequence is required.");
    }
    this.sequenceMeme.validate();
    return this.sequenceMeme;
  }

}
