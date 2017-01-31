// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
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
  JSONObject create(AccessControl access, IdeaWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one idea if accessible
   *
   * @param access control
   * @param id     of idea
   * @return retrieved record as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneAble(AccessControl access, ULong id) throws DatabaseException;

  /**
   * Fetch many idea for one Account by id, if accessible
   *
   * @param access    control
   * @param libraryId to fetch ideas for.
   * @return JSONArray of ideas.
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAllAble(AccessControl access, ULong libraryId) throws DatabaseException;

  /**
   * (ADMIN ONLY)
   * Update a specified Idea
   *
   * @param access control
   * @param ideaId of specific Idea to update.
   * @param data   for the updated Idea.
   */
  void update(AccessControl access, ULong ideaId, IdeaWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * (ADMIN ONLY)
   * Delete a specified idea
   *
   * @param id of specific idea to delete.
   */
  void delete(ULong id) throws DatabaseException, ConfigException, BusinessException;
}
