// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.phase_meme.PhaseMeme;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PhaseMemeDAO {

  /**
   Create a new Phase Meme

   @param access control
   @param entity for the new Phase Meme.
   @return newly readMany record
   */
  PhaseMeme create(Access access, PhaseMeme entity) throws Exception;

  /**
   Fetch one PhaseMeme if accessible

   @param access control
   @param id     of PhaseMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PhaseMeme readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many PhaseMeme for one Phase by id, if accessible

   @param access  control
   @param phaseId to fetch phaseMemes for.
   @return JSONArray of phaseMemes.
   @throws Exception on failure
   */
  Collection<PhaseMeme> readAll(Access access, BigInteger phaseId) throws Exception;

  /**
   Delete a specified PhaseMeme

   @param access control
   @param id     of specific PhaseMeme to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
