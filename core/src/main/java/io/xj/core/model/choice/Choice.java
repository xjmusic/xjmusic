// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.transport.CSV;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Choice extends Entity {
  public static final String KEY_ONE = "choice";
  public static final String KEY_MANY = "choices";
  private List<BigInteger> availablePatternOffsets = Lists.newArrayList();

  private BigInteger segmentId;
  private BigInteger sequenceId;
  private String _type; // to hold value before validation
  private SequenceType type;
  private BigInteger sequencePatternOffset;
  private Integer transpose;

  public BigInteger getSegmentId() {
    return segmentId;
  }

  public Choice setSegmentId(BigInteger value) {
    segmentId = value;
    return this;
  }

  public BigInteger getSequenceId() {
    return sequenceId;
  }

  public Choice setSequenceId(BigInteger value) {
    sequenceId = value;
    return this;
  }

  public SequenceType getType() {
    return type;
  }

  public Choice setType(String value) {
    _type = value;
    return this;
  }

  public void setTypeEnum(SequenceType type) {
    this.type = type;
  }

  public BigInteger getSequencePatternOffset() {
    return sequencePatternOffset;
  }

  public Choice setSequencePatternOffset(BigInteger value) {
    sequencePatternOffset = value;
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
   Whether the current Segment Choice has one or more patterns
   with a higher pattern offset than the current one

   @return true if it has one more pattern
   */
  public boolean hasOneMorePattern() {
    return availablePatternOffsets.stream().anyMatch(availableOffset -> 0 < availableOffset.compareTo(sequencePatternOffset));
  }

  /**
   Whether the current Segment Choice has two or more patterns
   with a higher pattern offset than the current two

   @return true if it has two more pattern
   */
  public boolean hasTwoMorePatterns() {
    int num = 0;
    for (BigInteger availableOffset : availablePatternOffsets)
      if (0 < availableOffset.compareTo(sequencePatternOffset)) {
        num++;
        if (2 <= num)
          return true;
      }
    return false;
  }

  /**
   Returns the pattern offset immediately after the current one,
   or loop back to zero is past the end of the available patterns

   @return next pattern offset
   */
  @Nullable
  public BigInteger nextPatternOffset() {
    BigInteger offset = null;
    for (BigInteger availableOffset : availablePatternOffsets)
      if (0 < availableOffset.compareTo(sequencePatternOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;
    return Objects.nonNull(offset) ? offset : BigInteger.valueOf(0L);
  }

  /**
   Get eitherOr pattern offsets for the chosen sequence

   @return eitherOr pattern offsets
   */
  public List<BigInteger> getAvailablePatternOffsets() {
    return Collections.unmodifiableList(availablePatternOffsets);
  }

  /**
   set available pattern offsets from CSV

   @param patternOffsets to set from
   */
  public Choice setAvailablePatternOffsets(String patternOffsets) {
    availablePatternOffsets = Lists.newArrayList();
    CSV.split(patternOffsets)
      .forEach(patternOffsetToSet ->
        availablePatternOffsets.add(new BigInteger(patternOffsetToSet)));
    Collections.sort(availablePatternOffsets);

    return this;
  }

  @Override
  public BigInteger getParentId() {
    return segmentId;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = SequenceType.validate(_type);

    if (Objects.isNull(segmentId))
      throw new BusinessException("Segment ID is required.");

    if (Objects.isNull(sequenceId))
      throw new BusinessException("Sequence ID is required.");

    if (Objects.isNull(type))
      throw new BusinessException("Type is required.");

    if (Objects.isNull(sequencePatternOffset))
      throw new BusinessException("Sequence Pattern Offset is required.");

    if (Objects.isNull(transpose))
      transpose = 0;
  }
}
