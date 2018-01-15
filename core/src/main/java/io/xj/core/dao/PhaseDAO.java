// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.phase.Phase;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PhaseDAO extends DAO<Phase> {

  /**
   Clone a Phase into a new Phase

   @param access  control
   @param cloneId of phase to clone
   @param entity  for the new Phase
   @return newly readMany record
   */
  Phase clone(Access access, BigInteger cloneId, Phase entity) throws Exception;

  /**
   Fetch all Phase accessible, by Pattern id and offset #

   @param access             control
   @param patternId          of pattern in which to read phase
   @param patternPhaseOffset of phase in pattern
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  Collection<Phase> readAllAtPatternOffset(Access access, BigInteger patternId, BigInteger patternPhaseOffset) throws Exception;

}
