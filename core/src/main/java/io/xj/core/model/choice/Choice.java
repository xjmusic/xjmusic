// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.model.idea.Idea;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHOICE;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Choice extends Entity {
  public static final String MACRO = Idea.MACRO;
  public static final String MAIN = Idea.MAIN;
  public static final String RHYTHM = Idea.RHYTHM;
  public static final String SUPPORT = Idea.SUPPORT;
  public static final String KEY_AVAILABLE_PHASE_OFFSETS = "availablePhaseOffsets";
  public static final List<String> TYPES = Idea.TYPES;
  public static final String KEY_ONE = "choice";
  public static final String KEY_MANY = "choices";
  private static final String KEY_IDEA_ID = "ideaId";
  private static final String KEY_PHASE_OFFSET = "phaseOffset";
  private static final String KEY_TRANSPOSE = "transpose";
  private static final String KEY_TYPE = "type";
  private List<ULong> availablePhaseOffsets;
  private ULong linkId;
  private ULong ideaId;
  private String type;
  private ULong phaseOffset;
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

  /**
   Whether the current Link Choice has one or more phases
   with a higher phase offset than the current one

   @return true if it has one more phase
   */
  public boolean hasOneMorePhase() {
    for (ULong availableOffset : availablePhaseOffsets)
      if (availableOffset.compareTo(phaseOffset) == 1)
        return true;
    return false;
  }

  /**
   Whether the current Link Choice has two or more phases
   with a higher phase offset than the current two

   @return true if it has two more phase
   */
  public boolean hasTwoMorePhases() {
    int num = 0;
    for (ULong availableOffset : availablePhaseOffsets)
      if (availableOffset.compareTo(phaseOffset) == 1) {
        num++;
        if (num >= 2)
          return true;
      }
    return false;
  }

  /**
   Returns the phase offset immediately after the current one,
   or loop back to zero is past the end of the available phases

   @return next phase offset
   */
  @Nullable
  public ULong nextPhaseOffset() {
    ULong offset = null;
    for (ULong availableOffset : availablePhaseOffsets)
      if (availableOffset.compareTo(phaseOffset) == 1)
        if (Objects.isNull(offset) ||
          availableOffset.compareTo(offset) == -1)
          offset = availableOffset;
    return Objects.nonNull(offset) ? offset : ULong.valueOf(0);
  }

  /**
   Get eitherOr phase offsets for the chosen idea

   @return eitherOr phase offsets
   */
  public List<ULong> getAvailablePhaseOffsets() {
    return availablePhaseOffsets;
  }

  /**
   set available phase offsets from CSV

   @param phaseOffsets to set from
   */
  public Choice setAvailablePhaseOffsets(String phaseOffsets) {
    availablePhaseOffsets = Lists.newArrayList();
    CSV.split(phaseOffsets)
      .forEach(phaseOffset ->
        availablePhaseOffsets.add(ULong.valueOf(phaseOffset)));
    Collections.sort(availablePhaseOffsets);

    return this;
  }

  @Override
  public void validate() throws BusinessException {

    if (this.linkId == null)
      throw new BusinessException("Link ID is required.");

    if (this.ideaId == null)
      throw new BusinessException("Idea ID is required.");

    if (this.type == null || this.type.length() == 0)
      throw new BusinessException("Type is required.");

    if (!TYPES.contains(this.type))
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");

    if (this.phaseOffset == null)
      throw new BusinessException("Phase Offset is required.");

    if (this.transpose == null)
      this.transpose = 0;
  }

  /**
   Return the link choice as a map, for reporting

   @return report map
   */
  public Map<String, Object> asMap() {
    Map<String, Object> out = com.google.common.collect.Maps.newHashMap();
    out.put(KEY_IDEA_ID, ideaId);
    out.put(KEY_PHASE_OFFSET, phaseOffset);
    out.put(KEY_TRANSPOSE, transpose);
    out.put(KEY_TYPE, type);
    out.put(KEY_AVAILABLE_PHASE_OFFSETS, availablePhaseOffsets);
    return out;
  }

  @Override
  public Choice setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }

    if (Objects.nonNull(record.field(CHOICE.ID)))
      id = record.get(CHOICE.ID);

    if (Objects.nonNull(record.field(CHOICE.LINK_ID)))
      linkId = record.get(CHOICE.LINK_ID);

    if (Objects.nonNull(record.field(CHOICE.IDEA_ID)))
      ideaId = record.get(CHOICE.IDEA_ID);

    if (Objects.nonNull(record.field(CHOICE.TYPE)))
      type = record.get(CHOICE.TYPE);

    if (Objects.nonNull(record.field(CHOICE.TRANSPOSE)))
      transpose = record.get(CHOICE.TRANSPOSE);

    if (Objects.nonNull(record.field(CHOICE.PHASE_OFFSET)))
      phaseOffset = record.get(CHOICE.PHASE_OFFSET);

    if (Objects.nonNull(record.field(CHOICE.CREATED_AT)))
      createdAt = record.get(CHOICE.CREATED_AT);

    if (Objects.nonNull(record.field(CHOICE.UPDATED_AT)))
      updatedAt = record.get(CHOICE.UPDATED_AT);

    if (Objects.nonNull(record.field(KEY_AVAILABLE_PHASE_OFFSETS)))
      setAvailablePhaseOffsets((String) record.get(KEY_AVAILABLE_PHASE_OFFSETS));

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
