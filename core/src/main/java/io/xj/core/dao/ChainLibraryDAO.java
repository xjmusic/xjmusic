// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain_library.ChainLibrary;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface ChainLibraryDAO {

  /**
   Create a new Chain Library

   @param entity for the new Chain Library.
   @return newly readMany record
   */
  ChainLibrary create(Access access, ChainLibrary entity) throws Exception;

  /**
   Fetch one ChainLibrary if accessible

   @param id of ChainLibrary
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainLibrary readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many ChainLibrary for one Chain by id, if accessible

   @param chainId to fetch chainLibraries for.
   @return JSONArray of chainLibraries.
   @throws Exception on failure
   */
  Collection<ChainLibrary> readAll(Access access, BigInteger chainId) throws Exception;

  /**
   Delete a specified ChainLibrary

   @param id of specific ChainLibrary to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
