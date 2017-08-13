// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.phase_meme;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.MemeEntity;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PHASE_MEME;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class PhaseMeme extends MemeEntity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "phaseMeme";
  public static final String KEY_MANY = "phaseMemes";
  // Phase ID
  private ULong phaseId;

  public ULong getPhaseId() {
    return phaseId;
  }

  public PhaseMeme setPhaseId(BigInteger phaseId) {
    this.phaseId = ULong.valueOf(phaseId);
    return this;
  }

  public PhaseMeme setName(String name) {
    this.name = Text.toProperSlug(name);
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
  public PhaseMeme setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(PHASE_MEME.ID);
    phaseId = record.get(PHASE_MEME.PHASE_ID);
    name = record.get(PHASE_MEME.NAME);
    createdAt = record.get(PHASE_MEME.CREATED_AT);
    updatedAt = record.get(PHASE_MEME.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE_MEME.PHASE_ID, phaseId);
    fieldValues.put(PHASE_MEME.NAME, name);
    return fieldValues;
  }


}
