// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.chord.Chord;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class PhaseChord extends Chord {
  public static final String KEY_ONE = "phaseChord";
  public static final String KEY_MANY = "phaseChords";

  private BigInteger phaseId;

  /**
   Empty constructor
   */
  public PhaseChord() {
  }

  /**
   Constructor

   @param phaseId  phase to which chord belongs
   @param position of chord in phase, starting at 0
   @param name     of chord
   */
  public PhaseChord(BigInteger phaseId, Integer position, String name) {
    this.phaseId = phaseId;
    this.position = position;
    this.name = name;
  }

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

  public BigInteger getParentId() {
    return phaseId;
  }

  public PhaseChord setPosition(Integer position) {
    this.position = position;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (Objects.isNull(phaseId))
      throw new BusinessException("Phase ID is required.");

    super.validate();
  }

  @Override
  public PhaseChord of(String name) {
    return new PhaseChord().setName(name);
  }

}
