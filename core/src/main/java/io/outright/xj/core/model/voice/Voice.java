// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.voice;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.CSV.CSV;
import io.outright.xj.core.util.Purify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jooq.Field;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.Tables.VOICE;

public class Voice extends Entity {
  public final static String PERCUSSIVE = "percussive";
  public final static String HARMONIC = "harmonic";
  public final static String MELODIC = "melodic";
  public final static String VOCAL = "vocal";

  public final static List<String> allTypes = ImmutableList.of(
    PERCUSSIVE,
    HARMONIC,
    MELODIC,
    VOCAL
  );

  /**
   * Phase
   */
  private ULong phaseId;

  public ULong getPhaseId() {
    return phaseId;
  }

  public Voice setPhaseId(BigInteger phaseId) {
    this.phaseId = ULong.valueOf(phaseId);
    return this;
  }

  /**
   * Type
   */
  private String type;

  public String getType() {
    return type;
  }

  public Voice setType(String type) {
    this.type = Purify.LowerSlug(type);
    return this;
  }

  /**
   * Description
   */
  private String description;

  public String getDescription() {
    return description;
  }

  public Voice setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  @Override
  public void validate() throws BusinessException {
    if (this.phaseId == null) {
      throw new BusinessException("Phase ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!allTypes.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(allTypes) +").");
    }
    if (this.description == null || this.description.length() == 0) {
      throw new BusinessException("Description is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(VOICE.PHASE_ID, phaseId)
      .put(VOICE.TYPE, type)
      .put(VOICE.DESCRIPTION, description)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "phaseId:" + this.phaseId +
      ", type:" + this.type +
      ", description:" + this.description +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "voice";
  public static final String KEY_MANY = "voices";

}
