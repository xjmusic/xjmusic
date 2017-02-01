// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase;

import io.outright.xj.core.app.exception.BusinessException;

public class PhaseWrapper {

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.phase == null) {
      throw new BusinessException("Phase is required.");
    }
    this.phase.validate();
  }

  @Override
  public String toString() {
    return "{" +
      Phase.KEY_ONE + ":" + this.phase +
      "}";
  }
}
