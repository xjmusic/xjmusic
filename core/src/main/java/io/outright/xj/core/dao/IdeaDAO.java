// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.model.idea.IdeaWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface IdeaDAO {

  /**
   * (ADMIN ONLY)
   * Create a new Account User
   *
   * @param access control
   * @param data   for the new Account User.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, IdeaWrapper data) throws Exception;

  /**
   * Fetch one idea if accessible
   *
   * @param access control
   * @param id     of idea
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many idea for one Account by id, if accessible
   *
   * @param access    control
   * @param libraryId to fetch ideas for.
   * @return JSONArray of ideas.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong libraryId) throws Exception;

  /**
   * (ADMIN ONLY)
   * Update a specified Idea
   *
   * @param access control
   * @param ideaId of specific Idea to update.
   * @param data   for the updated Idea.
   */
  void update(AccessControl access, ULong ideaId, IdeaWrapper data) throws Exception;

  /**
   * (ADMIN ONLY)
   * Delete a specified idea
   *
   * @param access control
   * @param id of specific idea to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
