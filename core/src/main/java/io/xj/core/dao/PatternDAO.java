// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PatternDAO {

  /**
   (ADMIN ONLY)
   Create a new Account User

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  Pattern create(Access access, Pattern entity) throws Exception;

  /**
   Fetch one pattern if accessible

   @param access control
   @param id     of pattern
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  Pattern readOne(Access access, BigInteger id) throws Exception;

  /**
   Read a given type of pattern for a given link

   @param access      control
   @param linkId      to read pattern for
   @param patternType type of pattern to read
   @return macro-type pattern; null if none found
   */
  @Nullable
  Pattern readOneTypeInLink(Access access, BigInteger linkId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @param access      control
   @param chainId     to fetch patterns for.
   @param patternType to fetch
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Collection<Pattern> readAllBoundToChain(Access access, BigInteger chainId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @param access      control
   @param chainId     to fetch patterns for.
   @param patternType to fetch
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Collection<Pattern> readAllBoundToChainLibrary(Access access, BigInteger chainId, PatternType patternType) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @param access    control
   @param accountId to fetch patterns for.
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Collection<Pattern> readAllInAccount(Access access, BigInteger accountId) throws Exception;

  /**
   Fetch many pattern for one Library by id, if accessible

   @param access    control
   @param libraryId to fetch patterns for.
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Collection<Pattern> readAllInLibrary(Access access, BigInteger libraryId) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Pattern

   @param access    control
   @param patternId of specific Pattern to update.
   @param entity    for the updated Pattern.
   */
  void update(Access access, BigInteger patternId, Pattern entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified pattern

   @param access control
   @param id     of specific pattern to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;

}
