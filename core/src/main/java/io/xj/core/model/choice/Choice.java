// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.transport.CSV;

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
  public static final String KEY_AVAILABLE_PHASE_OFFSETS = "availablePhaseOffsets";

  public static final String KEY_ONE = "choice";
  public static final String KEY_MANY = "choices";
  private static final String KEY_PATTERN_ID = "patternId";
  private static final String KEY_PHASE_OFFSET = "phaseOffset";
  private static final String KEY_TRANSPOSE = "transpose";
  private static final String KEY_TYPE = "type";
  private List<ULong> availablePhaseOffsets;

  private ULong linkId;
  private ULong patternId;
  private String _type; // to hold value before validation
  private PatternType type;
  private ULong phaseOffset;
  private Integer transpose;

  public ULong getLinkId() {
    return linkId;
  }

  public Choice setLinkId(BigInteger value) {
    linkId = ULong.valueOf(value);
    return this;
  }

  public ULong getPatternId() {
    return patternId;
  }

  public Choice setPatternId(BigInteger value) {
    patternId = ULong.valueOf(value);
    return this;
  }

  public PatternType getType() {
    return type;
  }

  public Choice setType(String value) {
    _type = value;
    return this;
  }

  public ULong getPhaseOffset() {
    return phaseOffset;
  }

  public Choice setPhaseOffset(BigInteger value) {
    phaseOffset = ULong.valueOf(value);
    return this;
  }

  public Integer getTranspose() {
    return transpose;
  }

  public Choice setTranspose(Integer value) {
    transpose = value;
    return this;
  }

  /**
   Whether the current Link Choice has one or more phases
   with a higher phase offset than the current one

   @return true if it has one more phase
   */
  public boolean hasOneMorePhase() {
    for (ULong availableOffset : availablePhaseOffsets)
      if (1 == availableOffset.compareTo(phaseOffset))
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
      if (1 == availableOffset.compareTo(phaseOffset)) {
        num++;
        if (2 <= num)
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
      if (1 == availableOffset.compareTo(phaseOffset))
        if (Objects.isNull(offset) ||
          -1 == availableOffset.compareTo(offset))
          offset = availableOffset;
    return Objects.nonNull(offset) ? offset : ULong.valueOf(0);
  }

  /**
   Get eitherOr phase offsets for the chosen pattern

   @return eitherOr phase offsets
   */
  public List<ULong> getAvailablePhaseOffsets() {
    return Collections.unmodifiableList(availablePhaseOffsets);
  }

  /**
   set available phase offsets from CSV

   @param phaseOffsets to set from
   */
  public Choice setAvailablePhaseOffsets(String phaseOffsets) {
    availablePhaseOffsets = Lists.newArrayList();
    CSV.split(phaseOffsets)
      .forEach(phaseOffsetToSet ->
        availablePhaseOffsets.add(ULong.valueOf(phaseOffsetToSet)));
    Collections.sort(availablePhaseOffsets);

    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = PatternType.validate(_type);

    if (Objects.isNull(linkId ))
      throw new BusinessException("Link ID is required.");

    if (Objects.isNull(patternId ))
      throw new BusinessException("Pattern ID is required.");

    if (Objects.isNull(type ))
      throw new BusinessException("Type is required.");

    if (Objects.isNull(phaseOffset ))
      throw new BusinessException("Phase Offset is required.");

    if (Objects.isNull(transpose ))
      transpose = 0;
  }

  /**
   Return the link choice as a map, for reporting

   @return report map
   */
  public Map<String, Object> asMap() {
    Map<String, Object> out = com.google.common.collect.Maps.newHashMap();
    out.put(KEY_PATTERN_ID, patternId);
    out.put(KEY_PHASE_OFFSET, phaseOffset);
    out.put(KEY_TRANSPOSE, transpose);
    out.put(KEY_TYPE, type);
    out.put(KEY_AVAILABLE_PHASE_OFFSETS, availablePhaseOffsets);
    return out;
  }

  @Override
  public Choice setFromRecord(Record record) throws BusinessException {
    if (Objects.isNull(record)) {
      return null;
    }

    if (Objects.nonNull(record.field(CHOICE.ID)))
      id = record.get(CHOICE.ID);

    if (Objects.nonNull(record.field(CHOICE.LINK_ID)))
      linkId = record.get(CHOICE.LINK_ID);

    if (Objects.nonNull(record.field(CHOICE.PATTERN_ID)))
      patternId = record.get(CHOICE.PATTERN_ID);

    if (Objects.nonNull(record.field(CHOICE.TYPE)))
      type = PatternType.validate(record.get(CHOICE.TYPE));

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
    fieldValues.put(CHOICE.PATTERN_ID, patternId);
    fieldValues.put(CHOICE.TYPE, type);
    fieldValues.put(CHOICE.TRANSPOSE, transpose);
    fieldValues.put(CHOICE.PHASE_OFFSET, phaseOffset);
    return fieldValues;
  }

}
