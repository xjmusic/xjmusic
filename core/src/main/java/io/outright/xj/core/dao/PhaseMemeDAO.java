// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
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
  JSONObject create(AccessControl access, PhaseMemeWrapper data) throws Exception;

  /**
   * Fetch one PhaseMeme if accessible
   *
   * @param access control
   * @param id     of PhaseMeme
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many PhaseMeme for one Phase by id, if accessible
   *
   * @param access control
   * @param phaseId to fetch phaseMemes for.
   * @return JSONArray of phaseMemes.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong phaseId) throws Exception;

  /**
   * Delete a specified PhaseMeme
   *
   * @param access control
   * @param id     of specific PhaseMeme to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
