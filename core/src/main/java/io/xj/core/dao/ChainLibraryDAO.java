// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.tables.records.ChainLibraryRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface ChainLibraryDAO {

  /**
   Create a new Chain Library

   @param entity for the new Chain Library.
   @return newly readMany record
   */
  ChainLibraryRecord create(Access access, ChainLibrary entity) throws Exception;

  /**
   Fetch one ChainLibrary if accessible

   @param id of ChainLibrary
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainLibraryRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many ChainLibrary for one Chain by id, if accessible

   @param chainId to fetch chainLibraries for.
   @return JSONArray of chainLibraries.
   @throws Exception on failure
   */
  Result<ChainLibraryRecord> readAll(Access access, ULong chainId) throws Exception;

  /**
   Delete a specified ChainLibrary

   @param id of specific ChainLibrary to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
