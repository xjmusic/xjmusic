// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
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
   Validate data.

   @throws BusinessException if invalid.
   */
  public PhaseChord validate() throws BusinessException {
    if (this.phaseChord == null) {
      throw new BusinessException("phaseChord is required.");
    }
    this.phaseChord.validate();
    return this.phaseChord;
  }

}
