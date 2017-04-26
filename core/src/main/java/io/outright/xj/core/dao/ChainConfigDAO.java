// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.chain_config.ChainConfigWrapper;
import io.outright.xj.core.model.link.LinkWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface ChainConfigDAO {

  /**
   * Create a new Chain config
   *
   * @param data for the new Chain config.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, ChainConfigWrapper data) throws Exception;

  /**
   * Fetch one ChainConfig if accessible
   *
   * @param id of ChainConfig
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many ChainConfig for one Chain by id, if accessible
   *
   * @param chainId to fetch chainConfigs for.
   * @return JSONArray of chainConfigs.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception;

  /**
   * Update a specified ChainConfig
   *
   * @param id of specific ChainConfig to update.
   * @param data   for the updated ChainConfig.
   */
  void update(AccessControl access, ULong id, ChainConfigWrapper data) throws Exception;

  /**
   * Delete a specified ChainConfig
   * @param id of specific ChainConfig to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
