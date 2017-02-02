// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.model.library.LibraryWrapper;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;

public interface LibraryDAO {
  /**
   * (ADMIN ONLY)
   * Create a new Library
   * @param data for the new Library.
   * @return newly created Library record.
   */
  JSONObject create(AccessControl access, LibraryWrapper data) throws Exception;

  /**
   * Fetch one Library by id, if accessible
   *
   * @param access control
   * @param libraryId to fetch
   * @return Library if found
   * @throws Exception on failure
   */
  @Nullable
  JSONObject readOne(AccessControl access, ULong libraryId) throws Exception;

  /**
   * Read all Libraries that are accessible
   *
   * @param access control
   * @return array of libraries as JSON
   * @throws Exception on failure
   */
  @Nullable
  JSONArray readAllIn(AccessControl access, ULong accountId) throws Exception;

  /**
   * (ADMIN ONLY)
   * Update a specified Library
   * @param libraryId of specific Library to update.
   * @param data for the updated Library.
   */
  void update(AccessControl access, ULong libraryId, LibraryWrapper data) throws Exception;

  /**
   * (ADMIN ONLY)
   * Delete a specified Library
   * @param libraryId of specific Library to delete.
   */
  void delete(AccessControl access, ULong libraryId) throws Exception;
}
