// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.idea_meme.IdeaMemeWrapper;

import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface IdeaMemeDAO {

  /**
   * Create a new Idea Meme
   *
   * @param access control
   * @param data   for the new Idea Meme.
   * @return newly created record as JSON
   */
  JSONObject create(AccessControl access, IdeaMemeWrapper data) throws Exception;

  /**
   * Fetch one IdeaMeme if accessible
   *
   * @param access control
   * @param id     of IdeaMeme
   * @return retrieved record as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong id) throws Exception;

  /**
   * Fetch many IdeaMeme for one Idea by id, if accessible
   *
   * @param access control
   * @param ideaId to fetch ideaMemes for.
   * @return JSONArray of ideaMemes.
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong ideaId) throws Exception;

  /**
   * Delete a specified IdeaMeme
   *
   * @param access control
   * @param id     of specific IdeaMeme to delete.
   */
  void delete(AccessControl access, ULong id) throws Exception;
}
