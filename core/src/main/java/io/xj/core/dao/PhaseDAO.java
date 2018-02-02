// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseState;

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

  /**
   Fetch all Phase in a certain state
   [INTERNAL USE ONLY]

   @param access control
   @param state  to get phases in
   @return Result of phase records.
   @throws Exception on failure
   */
  Collection<Phase> readAllInState(Access access, PhaseState state) throws Exception;

  /**
   Erase a specified Phase if accessible
   [#153976888] PhaseErase job erase a Phase in the background, in order to keep the UI functioning at a reasonable speed.

   @param access control
   @param id     of specific phase to erase.
   */
  void erase(Access access, BigInteger id) throws Exception;

}
