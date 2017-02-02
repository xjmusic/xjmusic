// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.phase_meme.PhaseMemeWrapper;

import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface PhaseMemeDAO {

  /**
   * Create a new Phase Meme
   *
   * @param access control
   * @param data   for the new Phase Meme.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, PhaseMemeWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one PhaseMeme if accessible
   *
   * @param access control
   * @param id     of PhaseMeme
   * @return retrieved record as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException;

  /**
   * Fetch many PhaseMeme for one Phase by id, if accessible
   *
   * @param access control
   * @param phaseId to fetch phaseMemes for.
   * @return JSONArray of phaseMemes.
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAllAble(AccessControl access, ULong phaseId) throws DatabaseException;

  /**
   * Delete a specified PhaseMeme
   *
   * @param access control
   * @param id     of specific PhaseMeme to delete.
   */
  void delete(AccessControl access, ULong id) throws DatabaseException, ConfigException, BusinessException;
}
