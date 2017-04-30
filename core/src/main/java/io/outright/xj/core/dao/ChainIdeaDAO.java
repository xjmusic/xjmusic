// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.chain_idea.ChainIdeaWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface ChainIdeaDAO {

  /**
   Create a new Chain Idea

   @param data for the new Chain Idea.
   @return newly created record as JSON
   */
  JSONObject create(AccessControl access, ChainIdeaWrapper data) throws Exception;

  /**
   Fetch one ChainIdea if accessible

   @param id of ChainIdea
   @return retrieved record as JSON
   @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   Fetch many ChainIdea for one Chain by id, if accessible

   @param chainId to fetch chainIdeas for.
   @return JSONArray of chainIdeas.
   @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong chainId) throws Exception;

  /**
   Delete a specified ChainIdea

   @param id of specific ChainIdea to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
