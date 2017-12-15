// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.chord.Chord;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class PhaseChord extends Chord {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "phaseChord";
  public static final String KEY_MANY = "phaseChords";
  /**
   Phase
   */
  private BigInteger phaseId;

  public PhaseChord setName(String name) {
    this.name = name;
    return this;
  }

  public BigInteger getPhaseId() {
    return phaseId;
  }

  public PhaseChord setPhaseId(BigInteger phaseId) {
    this.phaseId = phaseId;
    return this;
  }

  public PhaseChord setPosition(Double position) {
    this.position = position;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.phaseId == null) {
      throw new BusinessException("Phase ID is required.");
    }
    super.validate();
  }

}
