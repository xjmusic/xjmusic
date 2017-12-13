// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;

import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.util.Collection;

public interface PatternDAO {

  /**
   (ADMIN ONLY)
   Create a new Account User

   @return newly readMany record
    @param access control
   @param entity for the new Account User.
   */
  Pattern create(Access access, Pattern entity) throws Exception;

  /**
   Fetch one pattern if accessible

   @return retrieved record
   @throws Exception on failure
    @param access control
   @param id     of pattern
   */
  @Nullable
  Pattern readOne(Access access, ULong id) throws Exception;

  /**
   Read a given type of pattern for a given link

   @return macro-type pattern; null if none found
    @param access control
   @param linkId to read pattern for
   @param patternType type of pattern to read
   */
  @Nullable
  Pattern readOneRecordTypeInLink(Access access, ULong linkId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @return JSONArray of patterns.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch patterns for.
   @param patternType  to fetch
   */
  Collection<Pattern> readAllBoundToChain(Access access, ULong chainId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @return JSONArray of patterns.
   @throws Exception on failure
    @param access    control
   @param chainId to fetch patterns for.
   @param patternType  to fetch
   */
  Collection<Pattern> readAllBoundToChainLibrary(Access access, ULong chainId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @return JSONArray of patterns.
   @throws Exception on failure
    @param access    control
   @param accountId to fetch patterns for.
   */
  Collection<Pattern> readAllInAccount(Access access, ULong accountId) throws Exception;

  /**
   Fetch many pattern for one Library by id, if accessible

   @return JSONArray of patterns.
   @throws Exception on failure
    @param access    control
   @param libraryId to fetch patterns for.
   */
  Collection<Pattern> readAllInLibrary(Access access, ULong libraryId) throws Exception;

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
