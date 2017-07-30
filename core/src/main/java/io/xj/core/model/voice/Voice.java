// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.voice;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.model.instrument.InstrumentType;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.VOICE;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Voice extends Entity {

  public static final String KEY_ONE = "voice";
  public static final String KEY_MANY = "voices";

  private ULong phaseId;
  private String _type; // to hold value before validation
  private InstrumentType type;
  private String description;

  public ULong getPhaseId() {
    return phaseId;
  }

  public Voice setPhaseId(BigInteger phaseId) {
    this.phaseId = ULong.valueOf(phaseId);
    return this;
  }

  public InstrumentType getType() {
    return type;
  }

  public Voice setType(String type) {
    _type = type;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Voice setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = InstrumentType.validate(_type);

    if (Objects.isNull(phaseId)) {
      throw new BusinessException("Phase ID is required.");
    }
    if (Objects.isNull(type)) {
      throw new BusinessException("Type is required.");
    }

    if (Objects.isNull(description )|| description.isEmpty()) {
      throw new BusinessException("Description is required.");
    }
  }

  @Override
  public Voice setFromRecord(Record record) throws BusinessException {
    if (Objects.isNull(record)) {
      return null; // intentional return-null to "pass through" a null input value
    }
    id = record.get(VOICE.ID);
    phaseId = record.get(VOICE.PHASE_ID);
    type = InstrumentType.validate(record.get(VOICE.TYPE));
    description = record.get(VOICE.DESCRIPTION);
    createdAt = record.get(VOICE.CREATED_AT);
    updatedAt = record.get(VOICE.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(VOICE.PHASE_ID, phaseId);
    fieldValues.put(VOICE.TYPE, type);
    fieldValues.put(VOICE.DESCRIPTION, description);
    return fieldValues;
  }

}
