// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PhaseWrapper extends EntityWrapper {

  // Phase
  private Phase phase;

  public Phase getPhase() {
    return phase;
  }

  public PhaseWrapper setPhase(Phase phase) {
    this.phase = phase;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Phase validate() throws BusinessException {
    if (this.phase == null) {
      throw new BusinessException("Phase is required.");
    }
    this.phase.validate();
    return this.phase;
  }

}
