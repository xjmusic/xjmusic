//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence_pattern.SequencePattern;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface SequencePatternDAO extends DAO<SequencePattern> {
  /**
   * Fetch all Sequence-Pattern accessible, by Sequence id and offset #
   *
   * @param access                control
   * @param sequenceId            of sequence in which to read pattern
   * @param sequencePatternOffset of pattern in sequence
   * @return retrieved record
   * @throws CoreException on failure
   */
  @Nullable
  Collection<SequencePattern> readAllAtSequenceOffset(Access access, BigInteger sequenceId, BigInteger sequencePatternOffset) throws CoreException;


}
