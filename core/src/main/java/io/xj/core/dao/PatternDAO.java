// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.tables.records.PatternRecord;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface PatternDAO {

  /**
   (ADMIN ONLY)
   Create a new Account User

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  PatternRecord create(Access access, Pattern entity) throws Exception;

  /**
   Fetch one pattern if accessible

   @param access control
   @param id     of pattern
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PatternRecord readOne(Access access, ULong id) throws Exception;

  /**
   Read a given type of pattern for a given link

   @return macro-type pattern; null if none found
    @param access control
   @param linkId to read pattern for
   @param patternType type of pattern to read
   */
  @Nullable
  PatternRecord readOneRecordTypeInLink(Access access, ULong linkId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @return JSONArray of patterns.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch patterns for.
   @param patternType  to fetch
   */
  Result<? extends Record> readAllBoundToChain(Access access, ULong chainId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @return JSONArray of patterns.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch patterns for.
   @param patternType  to fetch
   */
  Result<? extends Record> readAllBoundToChainLibrary(Access access, ULong chainId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @param access    control
   @param accountId to fetch patterns for.
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Result<PatternRecord> readAllInAccount(Access access, ULong accountId) throws Exception;

  /**
   Fetch many pattern for one Library by id, if accessible

   @param access    control
   @param libraryId to fetch patterns for.
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Result<PatternRecord> readAllInLibrary(Access access, ULong libraryId) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Pattern

   @param access control
   @param patternId of specific Pattern to update.
   @param entity for the updated Pattern.
   */
  void update(Access access, ULong patternId, Pattern entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified pattern

   @param access control
   @param id     of specific pattern to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
