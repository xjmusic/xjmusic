// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.chain.ChainWrapper;
import io.outright.xj.core.tables.records.ChainRecord;

import org.jooq.Result;
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
   * Read IDs of all Chains that are in fabricating-state at a given instant
   *
   * @param access     control
   * @param atOrBefore time to check for chains in fabricating-state
   * @return array of chains as JSON
   * @throws Exception on failure
   */
  @Nullable
  Result<ChainRecord> readAllRecordsInStateFabricating(AccessControl access, Timestamp atOrBefore) throws Exception;

  /**
   * Update a specified Chain
   *
   * @param id   of specific Chain to update.
   * @param data for the updated Chain.
   */
  void update(AccessControl access, ULong id, ChainWrapper data) throws Exception;

  /**
   * Update the state of a specified Chain
   *
   * @param id    of specific Chain to update.
   * @param state for the updated Chain.
   */
  void updateState(AccessControl access, ULong id, String state) throws Exception;

  /**
   * [INTERNAL USE ONLY]
   * Build a JSON Object template for the next link in this Chain,
   * or set the Chain state to COMPLETE.
   *  @param linkBeginBefore         ahead to create Link before end of previous Link  @return array of chain Ids
   * @param chainStopCompleteBefore behind to consider a chain complete
   */
  JSONObject
  buildNextLinkOrComplete(AccessControl access, ChainRecord chain, Timestamp linkBeginBefore, Timestamp chainStopCompleteBefore) throws Exception;

  /**
   * Delete a specified Chain
   *
   * @param chainId of specific Chain to delete.
   */
  void delete(AccessControl access, ULong chainId) throws Exception;

}
