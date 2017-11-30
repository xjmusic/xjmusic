// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.tables.records.PhaseMemeRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface PhaseMemeDAO {

  /**
   Create a new Phase Meme

   @param access control
   @param entity for the new Phase Meme.
   @return newly readMany record
   */
  PhaseMemeRecord create(Access access, PhaseMeme entity) throws Exception;

  /**
   Fetch one PhaseMeme if accessible

   @param access control
   @param id     of PhaseMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PhaseMemeRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many PhaseMeme for one Phase by id, if accessible

   @param access  control
   @param phaseId to fetch phaseMemes for.
   @return JSONArray of phaseMemes.
   @throws Exception on failure
   */
  Result<PhaseMemeRecord> readAll(Access access, ULong phaseId) throws Exception;

  /**
   Delete a specified PhaseMeme

   @param access control
   @param id     of specific PhaseMeme to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
