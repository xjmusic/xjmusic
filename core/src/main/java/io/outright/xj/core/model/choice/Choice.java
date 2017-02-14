// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.util.CSV.CSV;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.Tables.CHOICE;

public class Choice extends Entity {
  public static final String MACRO = Idea.MACRO;
  public static final String MAIN = Idea.MAIN;
  public static final String RHYTHM = Idea.RHYTHM;
  public static final String SUPPORT = Idea.SUPPORT;

  private final static List<String> allTypes = ImmutableList.of(
    MACRO,
    MAIN,
    RHYTHM,
    SUPPORT
  );


  /**
   * Link
   */
  private ULong linkId;

  public ULong getLinkId() {
    return linkId;
  }

  public Choice setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  /**
   * Idea
   */
  private ULong ideaId;

  public ULong getIdeaId() {
    return ideaId;
  }

  public Choice setIdeaId(BigInteger ideaId) {
    this.ideaId = ULong.valueOf(ideaId);
    return this;
  }

  /**
   * Type
   */
  private String type;

  public String getType() {
    return type;
  }

  public Choice setType(String type) {
    this.type = Purify.LowerSlug(type);
    return this;
  }

  /**
   * Phase Offset
   */
  private ULong phaseOffset;

  public ULong getPhaseOffset() {
    return phaseOffset;
  }

  public Choice setPhaseOffset(BigInteger phaseOffset) {
    this.phaseOffset = ULong.valueOf(phaseOffset);
    return this;
  }

  /**
   * Transpose +/-
   */
  private Integer transpose;

  public Integer getTranspose() {
    return transpose;
  }

  public Choice setTranspose(Integer transpose) {
    this.transpose = transpose;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  @Override
  public void validate() throws BusinessException {
    if (this.linkId == null) {
      throw new BusinessException("Link ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!allTypes.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(allTypes) + ").");
    }
    if (this.phaseOffset == null || this.phaseOffset.equals(ULong.valueOf(0))) {
      throw new BusinessException("Phase Offset is required.");
    }
    if (this.transpose == null) {
      this.transpose = 0;
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHOICE.LINK_ID, linkId);
    fieldValues.put(CHOICE.IDEA_ID, ideaId);
    fieldValues.put(CHOICE.TYPE, type);
    fieldValues.put(CHOICE.TRANSPOSE, transpose);
    fieldValues.put(CHOICE.PHASE_OFFSET, phaseOffset);
    return fieldValues;
  }

  @Override
  public String toString() {
    return "{" +
      "linkId:" + this.linkId +
      ", ideaId:" + this.ideaId +
      ", type:" + this.type +
      ", transpose:" + this.transpose +
      ", phaseOffset:" + this.phaseOffset +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "choice";
  public static final String KEY_MANY = "choices";

}
