// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;

import java.math.BigInteger;

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
public class SequencePattern extends EntityImpl {
  public static final String KEY_ONE = "sequencePattern";
  public static final String KEY_MANY = "sequencePatterns";
  private BigInteger sequenceId;
  private BigInteger patternId;
  private BigInteger offset;

  /**
   Get Sequence id

   @return sequence id
   */
  public BigInteger getSequenceId() {
    return sequenceId;
  }

  /**
   Set sequence id

   @param sequenceId to set
   @return sequence pattern
   */
  public SequencePattern setSequenceId(BigInteger sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  /**
   Get Pattern id

   @return pattern id
   */
  public BigInteger getPatternId() {
    return patternId;
  }

  /**
   Set pattern id

   @param patternId to set
   @return pattern pattern
   */
  public SequencePattern setPatternId(BigInteger patternId) {
    this.patternId = patternId;
    return this;
  }

  /**
   Get offset

   @return offset
   */
  public BigInteger getOffset() {
    return offset;
  }

  /**
   Set offset

   @param offset to set
   @return this sequence pattern
   */
  public SequencePattern setOffset(BigInteger offset) {
    this.offset = offset;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return sequenceId;
  }

  @Override
  public void validate() throws CoreException {
    if (null == sequenceId) {
      throw new CoreException("Sequence ID is required.");
    }
    if (null == patternId) {
      throw new CoreException("Pattern ID is required.");
    }
    if (null == offset) {
      throw new CoreException("Offset is required.");
    }
  }

  @Override
  public String toString() {
    return String.format("Sequence%d-Pattern%d@%d", sequenceId, patternId, offset);
  }


}
