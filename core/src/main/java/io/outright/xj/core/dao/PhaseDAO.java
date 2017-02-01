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
   * (ADMIN ONLY)
   * Create a new Account User
   *
   * @param access control
   * @param data   for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, PhaseWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one phase if accessible
   *
   * @param access control
   * @param id     of phase
   * @return retrieved record as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException;

  /**
   * Fetch many phase for one Account by id, if accessible
   *
   * @param access    control
   * @param ideaId to fetch phases for.
   * @return JSONArray of phases.
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAllAble(AccessControl access, ULong ideaId) throws DatabaseException;

  /**
   * (ADMIN ONLY)
   * Update a specified Phase
   *
   * @param access control
   * @param id of specific Phase to update.
   * @param data   for the updated Phase.
   */
  void update(AccessControl access, ULong id, PhaseWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * (ADMIN ONLY)
   * Delete a specified phase
   *
   * @param access control
   * @param id of specific phase to delete.
   */
  void delete(AccessControl access, ULong id) throws DatabaseException, ConfigException, BusinessException;
}
