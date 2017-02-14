// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.model.pick.PickWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface PickDAO {
  /**
   * Create a new Pick
   * @param data for the new Pick.
   * @return newly created Pick record.
   */
  JSONObject create(AccessControl access, PickWrapper data) throws Exception;

  /**
   * Fetch one Pick by id, if accessible
   *
   * @param access control
   * @param id to fetch
   * @return Pick if found
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Read all Picks that are accessible
   *
   * @param access control
   * @return array of picks as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong arrangementId) throws Exception;

  /**
   * Update a specified Pick
   * @param pickId of specific Pick to update.
   * @param data for the updated Pick.
   */
  void update(AccessControl access, ULong pickId, PickWrapper data) throws Exception;

  /**
   * Delete a specified Pick
   * @param pickId of specific Pick to delete.
   */
  void delete(AccessControl access, ULong pickId) throws Exception;
}
