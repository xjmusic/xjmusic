// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_pattern.ChainPattern;
import io.xj.core.tables.records.ChainPatternRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface ChainPatternDAO {

  /**
   Create a new Chain Pattern

   @param entity for the new Chain Pattern.
   @return newly readMany record
   */
  ChainPatternRecord create(Access access, ChainPattern entity) throws Exception;

  /**
   Fetch one ChainPattern if accessible

   @param id of ChainPattern
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainPatternRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many ChainPattern for one Chain by id, if accessible

   @param chainId to fetch chainPatterns for.
   @return JSONArray of chainPatterns.
   @throws Exception on failure
   */
  Result<ChainPatternRecord> readAll(Access access, ULong chainId) throws Exception;

  /**
   Delete a specified ChainPattern

   @param id of specific ChainPattern to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
