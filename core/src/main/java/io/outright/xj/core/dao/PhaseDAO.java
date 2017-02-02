// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.phase.PhaseWrapper;

import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface PhaseDAO {

  /**
   * Create a new Phase
   *
   * @param access control
   * @param data   for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, PhaseWrapper data) throws Exception;

  /**
   * Fetch one Phase if accessible
   *
   * @param access control
   * @param id     of phase
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch all accessible Phase for one Account by id
   *
   * @param access    control
   * @param ideaId to fetch phases for.
   * @return JSONArray of phases.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong ideaId) throws Exception;

  /**
   * Update a specified Phase if accessible
   *
   * @param access control
   * @param id of specific Phase to update.
   * @param data   for the updated Phase.
   */
  void update(AccessControl access, ULong id, PhaseWrapper data) throws Exception;

  /**
   * Delete a specified Phase if accessible
   *
   * @param access control
   * @param id of specific phase to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
