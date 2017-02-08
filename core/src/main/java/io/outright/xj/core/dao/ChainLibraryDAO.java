// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.model.chain_library.ChainLibraryWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface ChainLibraryDAO {

  /**
   * Create a new Chain Library
   *
   * @param data for the new Chain Library.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, ChainLibraryWrapper data) throws Exception;

  /**
   * Fetch one ChainLibrary if accessible
   *
   * @param id of ChainLibrary
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many ChainLibrary for one Chain by id, if accessible
   *
   * @param chainId to fetch chainLibraries for.
   * @return JSONArray of chainLibraries.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception;

  /**
   * Delete a specified ChainLibrary
   * @param id of specific ChainLibrary to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
