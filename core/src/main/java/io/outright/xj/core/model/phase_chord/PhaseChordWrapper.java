// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class PhaseChordWrapper extends EntityWrapper {

  // PhaseChord
  private PhaseChord phaseChord;
  public PhaseChord getPhaseChord() {
    return phaseChord;
  }
  public PhaseChordWrapper setPhaseChord(PhaseChord phaseChord) {
    this.phaseChord = phaseChord;
    return this;
  }

  /**
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public PhaseChord validate() throws BusinessException{
    if (this.phaseChord == null) {
      throw new BusinessException("phaseChord is required.");
    }
    this.phaseChord.validate();
    return this.phaseChord;
  }

  @Override
  public String toString() {
    return "{" +
      PhaseChord.KEY_ONE + ":" + this.phaseChord +
      "}";
  }
}
