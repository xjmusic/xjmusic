// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_pattern.ChainPattern;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface ChainPatternDAO {

  /**
   Create a new Chain Pattern

   @param entity for the new Chain Pattern.
   @return newly readMany record
   */
  ChainPattern create(Access access, ChainPattern entity) throws Exception;

  /**
   Fetch one ChainPattern if accessible

   @param id of ChainPattern
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainPattern readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many ChainPattern for one Chain by id, if accessible

   @param chainId to fetch chainPatterns for.
   @return JSONArray of chainPatterns.
   @throws Exception on failure
   */
  Collection<ChainPattern> readAll(Access access, BigInteger chainId) throws Exception;

  /**
   Delete a specified ChainPattern

   @param id of specific ChainPattern to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
