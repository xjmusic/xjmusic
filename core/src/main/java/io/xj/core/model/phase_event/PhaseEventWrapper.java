// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PhaseEventWrapper extends EntityWrapper {

  // PhaseEvent
  private PhaseEvent phaseEvent;

  public PhaseEvent getPhaseEvent() {
    return phaseEvent;
  }

  public PhaseEventWrapper setPhaseEvent(PhaseEvent phaseEvent) {
    this.phaseEvent = phaseEvent;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public PhaseEvent validate() throws BusinessException {
    if (this.phaseEvent == null) {
      throw new BusinessException("phaseEvent is required.");
    }
    this.phaseEvent.validate();
    return this.phaseEvent;
  }

}
