// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.app.access.impl.Access;
import io.xj.core.model.chain_idea.ChainIdea;
import io.xj.core.tables.records.ChainIdeaRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface ChainIdeaDAO {

  /**
   Create a new Chain Idea

   @param entity for the new Chain Idea.
   @return newly readMany record
   */
  ChainIdeaRecord create(Access access, ChainIdea entity) throws Exception;

  /**
   Fetch one ChainIdea if accessible

   @param id of ChainIdea
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  ChainIdeaRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch many ChainIdea for one Chain by id, if accessible

   @param chainId to fetch chainIdeas for.
   @return JSONArray of chainIdeas.
   @throws Exception on failure
   */
  Result<ChainIdeaRecord> readAll(Access access, ULong chainId) throws Exception;

  /**
   Delete a specified ChainIdea

   @param id of specific ChainIdea to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
