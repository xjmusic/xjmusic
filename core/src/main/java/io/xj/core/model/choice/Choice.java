// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.model.pattern.PatternType;
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
  private List<BigInteger> availablePhaseOffsets = Lists.newArrayList();

  private BigInteger linkId;
  private BigInteger patternId;
  private String _type; // to hold value before validation
  private PatternType type;
  private BigInteger phaseOffset;
  private Integer transpose;

  public BigInteger getLinkId() {
    return linkId;
  }

  public Choice setLinkId(BigInteger value) {
    linkId = value;
    return this;
  }

  public BigInteger getPatternId() {
    return patternId;
  }

  public Choice setPatternId(BigInteger value) {
    patternId = value;
    return this;
  }

  public PatternType getType() {
    return type;
  }

  public Choice setType(String value) {
    _type = value;
    return this;
  }

  public void setTypeEnum(PatternType type) {
    this.type = type;
  }

  public BigInteger getPhaseOffset() {
    return phaseOffset;
  }

  public Choice setPhaseOffset(BigInteger value) {
    phaseOffset = value;
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
    for (BigInteger availableOffset : availablePhaseOffsets)
      if (0 < availableOffset.compareTo(phaseOffset))
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
    for (BigInteger availableOffset : availablePhaseOffsets)
      if (0 < availableOffset.compareTo(phaseOffset)) {
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
  public BigInteger nextPhaseOffset() {
    BigInteger offset = null;
    for (BigInteger availableOffset : availablePhaseOffsets)
      if (0 < availableOffset.compareTo(phaseOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;
    return Objects.nonNull(offset) ? offset : BigInteger.valueOf(0);
  }

  /**
   Get eitherOr phase offsets for the chosen pattern

   @return eitherOr phase offsets
   */
  public List<BigInteger> getAvailablePhaseOffsets() {
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
        availablePhaseOffsets.add(new BigInteger(phaseOffsetToSet)));
    Collections.sort(availablePhaseOffsets);

    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = PatternType.validate(_type);

    if (Objects.isNull(linkId))
      throw new BusinessException("Link ID is required.");

    if (Objects.isNull(patternId))
      throw new BusinessException("Pattern ID is required.");

    if (Objects.isNull(type))
      throw new BusinessException("Type is required.");

    if (Objects.isNull(phaseOffset))
      throw new BusinessException("Phase Offset is required.");

    if (Objects.isNull(transpose))
      transpose = 0;
  }
}
