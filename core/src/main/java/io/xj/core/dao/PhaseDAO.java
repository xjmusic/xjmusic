// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.phase.Phase;
import io.xj.core.tables.records.PhaseRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface PhaseDAO {

  /**
   Create a new Phase

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  PhaseRecord create(Access access, Phase entity) throws Exception;

  /**
   Fetch one Phase if accessible

   @param access control
   @param id     of phase
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PhaseRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch one Phase if accessible, by Idea id and phase #

   @return retrieved record
   @throws Exception on failure
    @param access          control
   @param ideaId          of idea in which to read phase
   @param ideaPhaseOffset of phase in idea
   */
  @Nullable
  PhaseRecord readOneForIdea(Access access, ULong ideaId, ULong ideaPhaseOffset) throws Exception;

  /**
   Fetch all accessible Phase for one Account by id

   @param access control
   @param ideaId to fetch phases for.
   @return JSONArray of phases.
   @throws Exception on failure
   */
  Result<PhaseRecord> readAll(Access access, ULong ideaId) throws Exception;

  /**
   Update a specified Phase if accessible

   @param access control
   @param id     of specific Phase to update.
   @param entity for the updated Phase.
   */
  void update(Access access, ULong id, Phase entity) throws Exception;

  /**
   Delete a specified Phase if accessible

   @param access control
   @param id     of specific phase to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
