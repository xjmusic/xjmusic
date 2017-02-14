// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.PHASE_CHORD;

public class PhaseChord extends Entity {

  /**
   * Name
   */
  private String name;

  public String getName() {
    return name;
  }

  public PhaseChord setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Phase
   */
  private ULong phaseId;

  public ULong getPhaseId() {
    return phaseId;
  }

  public PhaseChord setPhaseId(BigInteger phaseId) {
    this.phaseId = ULong.valueOf(phaseId);
    return this;
  }

  /**
   * Position
   */
  private Double position;

  public Double getPosition() {
    return position;
  }

  public PhaseChord setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.phaseId == null) {
      throw new BusinessException("Phase ID is required.");
    }
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE_CHORD.NAME, name);
    fieldValues.put(PHASE_CHORD.PHASE_ID, phaseId);
    fieldValues.put(PHASE_CHORD.POSITION, position);
    return fieldValues;
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "phaseChord";
  public static final String KEY_MANY = "phaseChords";

}
