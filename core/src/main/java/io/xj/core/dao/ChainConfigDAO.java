// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.tables.records.ChainConfigRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface ChainConfigDAO {

  /**
   Create a new Chain config

   @param entity for the new Chain config.
   @return newly readMany record
   */
  ChainConfigRecord create(Access access, ChainConfig entity) throws Exception;

  /**
   Fetch one ChainConfig if accessible

   @param id of ChainConfig
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainConfigRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many ChainConfig for one Chain by id, if accessible

   @param chainId to fetch chainConfigs for.
   @return JSONArray of chainConfigs.
   @throws Exception on failure
   */
  Result<ChainConfigRecord> readAll(Access access, ULong chainId) throws Exception;

  /**
   Update a specified ChainConfig

   @param id     of specific ChainConfig to update.
   @param entity for the updated ChainConfig.
   */
  void update(Access access, ULong id, ChainConfig entity) throws Exception;

  /**
   Delete a specified ChainConfig

   @param id of specific ChainConfig to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
