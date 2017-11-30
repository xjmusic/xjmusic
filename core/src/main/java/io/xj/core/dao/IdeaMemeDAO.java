// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.idea_meme.IdeaMeme;
import io.xj.core.tables.records.IdeaMemeRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface IdeaMemeDAO {

  /**
   Create a new Idea Meme

   @param access control
   @param entity for the new Idea Meme.
   @return newly readMany record
   */
  IdeaMemeRecord create(Access access, IdeaMeme entity) throws Exception;

  /**
   Fetch one IdeaMeme if accessible

   @param access control
   @param id     of IdeaMeme
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  IdeaMemeRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many IdeaMeme for one Idea by id, if accessible

   @param access control
   @param ideaId to fetch ideaMemes for.
   @return JSONArray of ideaMemes.
   @throws Exception on failure
   */
  Result<IdeaMemeRecord> readAll(Access access, ULong ideaId) throws Exception;

  /**
   Delete a specified IdeaMeme

   @param access control
   @param id     of specific IdeaMeme to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
