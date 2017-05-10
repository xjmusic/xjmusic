// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
