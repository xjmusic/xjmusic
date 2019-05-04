// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternState;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PatternDAO extends DAO<Pattern> {

  /**
   Clone a Pattern into a new Pattern

   @param access  control
   @param cloneId of pattern to clone
   @param entity  for the new Pattern
   @return newly readMany record
   */
  Pattern clone(Access access, BigInteger cloneId, Pattern entity) throws CoreException;

  /**
   Fetch all Pattern accessible, by Sequence id and offset #

   @param access             control
   @param sequenceId          of sequence in which to read pattern
   @param sequencePatternOffset of pattern in sequence
   @return retrieved record
   @throws CoreException on failure
   */
  @Nullable
  Collection<Pattern> readAllAtSequenceOffset(Access access, BigInteger sequenceId, BigInteger sequencePatternOffset) throws CoreException;

  /**
   Fetch all Pattern in a certain state
   [INTERNAL USE ONLY]

   @param access control
   @param state  to get patterns in
   @return Result of pattern records.
   @throws CoreException on failure
   */
  Collection<Pattern> readAllInState(Access access, PatternState state) throws CoreException;

  /**
   Erase a specified Pattern if accessible
   [#153976888] PatternErase job erase a Pattern in the background, in order to keep the UI functioning at a reasonable speed.

   @param access control
   @param id     of specific pattern to erase.
   */
  void erase(Access access, BigInteger id) throws CoreException;

}
