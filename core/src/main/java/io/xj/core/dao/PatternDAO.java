// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PatternDAO extends DAO<Pattern> {

  /**
   Clone a Pattern into a new Pattern

   @param access  control
   @param cloneId of pattern to clone
   @param entity  for the new Pattern
   @return newly readMany record
   */
  Pattern clone(Access access, BigInteger cloneId, Pattern entity) throws Exception;

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
   Fetch many pattern bound to a particular chain

   @param access  control
   @param chainId to fetch patterns for.
   @return collection of patterns.
   @throws Exception on failure
   */
  Collection<Pattern> readAllBoundToChain(Access access, BigInteger chainId) throws Exception;

  /**
   Fetch many pattern for one Account by id, if accessible

   @param access    control
   @param accountId to fetch patterns for.
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Collection<Pattern> readAllInAccount(Access access, BigInteger accountId) throws Exception;

  /**
   Fetch all pattern visible to given access

   @param access control
   @return JSONArray of patterns.
   @throws Exception on failure
   */
  Collection<Pattern> readAll(Access access) throws Exception;

  /**
   Fetch all Pattern in a certain state
   [INTERNAL USE ONLY]

   @param access control
   @param state  to get patterns in
   @return Result of pattern records.
   @throws Exception on failure
   */
  Collection<Pattern> readAllInState(Access access, PatternState state) throws Exception;

  /**
   Erase a specified Pattern if accessible.
   [#154887174] PatternErase job erase a Pattern and all its Phases in the background, in order to keep the UI functioning at a reasonable speed.

   @param access control
   @param id     of specific pattern to erase.
   */
  void erase(Access access, BigInteger id) throws Exception;
}
