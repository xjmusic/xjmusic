// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.CHOICE;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Choice extends Entity {
  public static final String MACRO = Idea.MACRO;
  public static final String MAIN = Idea.MAIN;
  public static final String RHYTHM = Idea.RHYTHM;
  public static final String SUPPORT = Idea.SUPPORT;

  /**
   It is implied that choice types must equal idea types
   */
  public final static List<String> TYPES = Idea.TYPES;
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "choice";
  public static final String KEY_MANY = "choices";
  /**
   Link
   */
  private ULong linkId;
  /**
   Idea
   */
  private ULong ideaId;
  /**
   Type
   */
  private String type;
  /**
   Phase Offset
   */
  private ULong phaseOffset;
  /**
   Transpose +/-
   */
  private Integer transpose;

  public ULong getLinkId() {
    return linkId;
  }

  public Choice setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  public ULong getIdeaId() {
    return ideaId;
  }

  public Choice setIdeaId(BigInteger ideaId) {
    this.ideaId = ULong.valueOf(ideaId);
    return this;
  }

  public String getType() {
    return type;
  }

  public Choice setType(String type) {
    this.type = Text.LowerSlug(type);
    return this;
  }

  public ULong getPhaseOffset() {
    return phaseOffset;
  }

  public Choice setPhaseOffset(BigInteger phaseOffset) {
    this.phaseOffset = ULong.valueOf(phaseOffset);
    return this;
  }

  public Integer getTranspose() {
    return transpose;
  }

  public Choice setTranspose(Integer transpose) {
    this.transpose = transpose;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.linkId == null) {
      throw new BusinessException("Link ID is required.");
    }
    if (this.ideaId == null) {
      throw new BusinessException("Idea ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (this.phaseOffset == null || this.phaseOffset.equals(UInteger.valueOf(0))) {
      throw new BusinessException("Phase Offset is required.");
    }
    if (this.transpose == null) {
      this.transpose = 0;
    }
  }

  @Override
  public Choice setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHOICE.ID);
    linkId = record.get(CHOICE.LINK_ID);
    ideaId = record.get(CHOICE.IDEA_ID);
    type = record.get(CHOICE.TYPE);
    transpose = record.get(CHOICE.TRANSPOSE);
    phaseOffset = record.get(CHOICE.PHASE_OFFSET);
    createdAt = record.get(CHOICE.CREATED_AT);
    updatedAt = record.get(CHOICE.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHOICE.LINK_ID, linkId);
    fieldValues.put(CHOICE.IDEA_ID, ideaId);
    fieldValues.put(CHOICE.TYPE, type);
    fieldValues.put(CHOICE.TRANSPOSE, transpose);
    fieldValues.put(CHOICE.PHASE_OFFSET, phaseOffset);
    return fieldValues;
  }

}
