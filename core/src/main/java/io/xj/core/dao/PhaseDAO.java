// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.phase.Phase;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PhaseDAO {

  /**
   Create a new Phase

   @param access control
   @param entity for the new Phase
   @return newly readMany record
   */
  Phase create(Access access, Phase entity) throws Exception;

  /**
   Clone a Phase into a new Phase

   @param access  control
   @param cloneId of phase to clone
   @param entity  for the new Phase
   @return newly readMany record
   */
  Phase clone(Access access, BigInteger cloneId, Phase entity) throws Exception;

  /**
   Fetch one Phase if accessible

   @param access control
   @param id     of phase
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  Phase readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch one Phase if accessible, by Pattern id and phase #

   @param access             control
   @param patternId          of pattern in which to read phase
   @param patternPhaseOffset of phase in pattern
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  Phase readOneForPattern(Access access, BigInteger patternId, BigInteger patternPhaseOffset) throws Exception;

  /**
   Fetch all accessible Phase for one Account by id

   @param access    control
   @param patternId to fetch phases for.
   @return JSONArray of phases.
   @throws Exception on failure
   */
  Collection<Phase> readAll(Access access, BigInteger patternId) throws Exception;

  /**
   Update a specified Phase if accessible

   @param access control
   @param id     of specific Phase to update.
   @param entity for the updated Phase.
   */
  void update(Access access, BigInteger id, Phase entity) throws Exception;

  /**
   Delete a specified Phase if accessible

   @param access control
   @param id     of specific phase to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;

}
