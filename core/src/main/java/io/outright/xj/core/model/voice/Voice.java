// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.voice;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.VOICE;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Voice extends Entity {
  public final static String PERCUSSIVE = "percussive";
  public final static String HARMONIC = "harmonic";
  public final static String MELODIC = "melodic";
  public final static String VOCAL = "vocal";

  /**
   It is implied that voice types must equal instrument types
   */
  public final static List<String> TYPES = Instrument.TYPES;
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "voice";
  public static final String KEY_MANY = "voices";
  /**
   Phase
   */
  private ULong phaseId;
  /**
   Type
   */
  private String type;
  /**
   Description
   */
  private String description;

  public ULong getPhaseId() {
    return phaseId;
  }

  public Voice setPhaseId(BigInteger phaseId) {
    this.phaseId = ULong.valueOf(phaseId);
    return this;
  }

  public String getType() {
    return type;
  }

  public Voice setType(String type) {
    this.type = Text.LowerSlug(type);
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
    if (this.phaseId == null) {
      throw new BusinessException("Phase ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (this.description == null || this.description.length() == 0) {
      throw new BusinessException("Description is required.");
    }
  }

  @Override
  public Voice setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(VOICE.ID);
    phaseId = record.get(VOICE.PHASE_ID);
    type = record.get(VOICE.TYPE);
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
