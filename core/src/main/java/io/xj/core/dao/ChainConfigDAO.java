// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_config.ChainConfig;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface ChainConfigDAO {

  /**
   Create a new Chain config

   @param entity for the new Chain config.
   @return newly readMany record
   */
  ChainConfig create(Access access, ChainConfig entity) throws Exception;

  /**
   Fetch one ChainConfig if accessible

   @param id of ChainConfig
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainConfig readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many ChainConfig for one Chain by id, if accessible

   @param chainId to fetch chainConfigs for.
   @return JSONArray of chainConfigs.
   @throws Exception on failure
   */
  Collection<ChainConfig> readAll(Access access, BigInteger chainId) throws Exception;

  /**
   Update a specified ChainConfig

   @param id     of specific ChainConfig to update.
   @param entity for the updated ChainConfig.
   */
  void update(Access access, BigInteger id, ChainConfig entity) throws Exception;

  /**
   Delete a specified ChainConfig

   @param id of specific ChainConfig to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
