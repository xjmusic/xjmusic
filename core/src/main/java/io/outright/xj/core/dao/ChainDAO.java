// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.chain.ChainWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.Timestamp;

public interface ChainDAO {
  /**
   * Create a new Chain
   *
   * @param data for the new Chain.
   * @return newly created Chain record.
   */
  JSONObject create(AccessControl access, ChainWrapper data) throws Exception;

  /**
   * Fetch one Chain by id, if accessible
   *
   * @param access  control
   * @param chainId to fetch
   * @return Chain if found
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong chainId) throws Exception;

  /**
   * Read all Chains that are accessible
   *
   * @param access control
   * @return array of chains as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong accountId) throws Exception;

  /**
   * [INTERNAL USE ONLY]
   * Read IDs of all Chains that are in production at a given instant
   *
   * @param access control
   * @param at time to check for chains in production
   * @param rangeSeconds plus or minus
   * @return array of chains as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIdStartAtInProduction(AccessControl access, Timestamp at, int rangeSeconds) throws Exception;

  /**
   * Update a specified Chain
   *
   * @param chainId of specific Chain to update.
   * @param data    for the updated Chain.
   */
  void update(AccessControl access, ULong chainId, ChainWrapper data) throws Exception;

  /**
   * Delete a specified Chain
   *
   * @param chainId of specific Chain to delete.
   */
  void delete(AccessControl access, ULong chainId) throws Exception;

}
