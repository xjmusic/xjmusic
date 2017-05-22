// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.tables.records.IdeaRecord;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface IdeaDAO {

  /**
   (ADMIN ONLY)
   Create a new Account User

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  IdeaRecord create(Access access, Idea entity) throws Exception;

  /**
   Fetch one idea if accessible

   @param access control
   @param id     of idea
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  IdeaRecord readOne(Access access, ULong id) throws Exception;

  /**
   Read a given type of idea for a given link

   @param access control
   @param linkId to read idea for
   @param ideaType type of idea to read
   @return macro-type idea; null if none found
   */
  @Nullable
  IdeaRecord readOneRecordTypeInLink(Access access, ULong linkId, String ideaType) throws Exception;

  /**
   Fetch many idea for one Account by id, if accessible

   @param access    control
   @param chainId to fetch ideas for.
   @param ideaType  to fetch
   @return JSONArray of ideas.
   @throws Exception on failure
   */
  Result<? extends Record> readAllBoundToChain(Access access, ULong chainId, String ideaType) throws Exception;

  /**
   Fetch many idea for one Account by id, if accessible

   @param access    control
   @param chainId to fetch ideas for.
   @param ideaType  to fetch
   @return JSONArray of ideas.
   @throws Exception on failure
   */
  Result<? extends Record> readAllBoundToChainLibrary(Access access, ULong chainId, String ideaType) throws Exception;

  /**
   Fetch many idea for one Account by id, if accessible

   @param access    control
   @param accountId to fetch ideas for.
   @return JSONArray of ideas.
   @throws Exception on failure
   */
  Result<IdeaRecord> readAllInAccount(Access access, ULong accountId) throws Exception;

  /**
   Fetch many idea for one Library by id, if accessible

   @param access    control
   @param libraryId to fetch ideas for.
   @return JSONArray of ideas.
   @throws Exception on failure
   */
  Result<IdeaRecord> readAllInLibrary(Access access, ULong libraryId) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Idea

   @param access control
   @param ideaId of specific Idea to update.
   @param entity for the updated Idea.
   */
  void update(Access access, ULong ideaId, Idea entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified idea

   @param access control
   @param id     of specific idea to delete.
   */
  void delete(Access access, ULong id) throws Exception;

}
