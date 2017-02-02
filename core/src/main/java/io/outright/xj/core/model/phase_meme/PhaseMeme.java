// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.util.Purify;

import com.google.common.collect.ImmutableMap;
import org.jooq.Field;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.PHASE_MEME;

public class PhaseMeme {

  // Phase ID
  private BigInteger phaseId;
  public BigInteger getPhaseId() {
    return phaseId;
  }
  public PhaseMeme setPhaseId(BigInteger phaseId) {
    this.phaseId = phaseId;
    return this;
  }

  // Name
  private String name;
  public String getName() {
    return name;
  }
  public PhaseMeme setName(String name) {
    this.name = Purify.ProperSlug(name);
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.phaseId == null) {
      throw new BusinessException("Phase ID is required.");
    }
    if (this.name == null) {
      throw new BusinessException("Name is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(PHASE_MEME.PHASE_ID, phaseId)
      .put(PHASE_MEME.NAME, name)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "phaseId:" + this.phaseId +
      "name:" + this.name +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "phaseMeme";
  public static final String KEY_MANY = "phaseMemes";


}
