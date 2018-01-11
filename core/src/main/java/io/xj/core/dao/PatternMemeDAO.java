// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.pattern_meme.PatternMeme;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface PatternMemeDAO {

  /**
   Create a new Pattern Meme

   @param access control
   @param entity for the new Pattern Meme.
   @return newly readMany record
   */
  PatternMeme create(Access access, PatternMeme entity) throws Exception;

  /**
   Fetch one PatternMeme if accessible

   @param access control
   @param id     of PatternMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PatternMeme readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many PatternMeme for one Pattern by id, if accessible

   @param access    control
   @param patternId to fetch patternMemes for.
   @return JSONArray of patternMemes.
   @throws Exception on failure
   */
  Collection<PatternMeme> readAll(Access access, BigInteger patternId) throws Exception;

  /**
   Delete a specified PatternMeme

   @param access control
   @param id     of specific PatternMeme to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
