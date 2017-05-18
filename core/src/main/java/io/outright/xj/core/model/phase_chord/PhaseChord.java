// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.chord.Chord;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.PHASE_CHORD;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
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
  private ULong phaseId;

  public PhaseChord setName(String name) {
    this.name = name;
    return this;
  }

  public ULong getPhaseId() {
    return phaseId;
  }

  public PhaseChord setPhaseId(BigInteger phaseId) {
    this.phaseId = ULong.valueOf(phaseId);
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

  @Override
  public PhaseChord setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(PHASE_CHORD.ID);
    name = record.get(PHASE_CHORD.NAME);
    phaseId = record.get(PHASE_CHORD.PHASE_ID);
    position = record.get(PHASE_CHORD.POSITION);
    createdAt = record.get(PHASE_CHORD.CREATED_AT);
    updatedAt = record.get(PHASE_CHORD.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE_CHORD.NAME, name);
    fieldValues.put(PHASE_CHORD.PHASE_ID, phaseId);
    fieldValues.put(PHASE_CHORD.POSITION, position);
    return fieldValues;
  }

}
