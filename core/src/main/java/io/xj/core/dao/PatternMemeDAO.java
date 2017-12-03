// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.tables.records.PatternMemeRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface PatternMemeDAO {

  /**
   Create a new Pattern Meme

   @param access control
   @param entity for the new Pattern Meme.
   @return newly readMany record
   */
  PatternMemeRecord create(Access access, PatternMeme entity) throws Exception;

  /**
   Fetch one PatternMeme if accessible

   @param access control
   @param id     of PatternMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  PatternMemeRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many PatternMeme for one Pattern by id, if accessible

   @param access control
   @param patternId to fetch patternMemes for.
   @return JSONArray of patternMemes.
   @throws Exception on failure
   */
  Result<PatternMemeRecord> readAll(Access access, ULong patternId) throws Exception;

  /**
   Delete a specified PatternMeme

   @param access control
   @param id     of specific PatternMeme to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
