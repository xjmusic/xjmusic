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
  JSONObject create(LibraryWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * Fetch one Library by id, if accessible
   *
   * @param access control
   * @param libraryId to fetch
   * @return Library if found
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONObject readOneAble(AccessControl access, ULong libraryId) throws DatabaseException;

  /**
   * Read all Libraries that are accessible
   *
   * @param access control
   * @return array of libraries as JSON
   * @throws DatabaseException on failure
   */
  @Nullable
  JSONArray readAllAble(AccessControl access, ULong accountId) throws DatabaseException;

  /**
   * (ADMIN ONLY)
   * Update a specified Library
   * @param libraryId of specific Library to update.
   * @param data for the updated Library.
   */
  void update(ULong libraryId, LibraryWrapper data) throws DatabaseException, ConfigException, BusinessException;

  /**
   * (ADMIN ONLY)
   * Delete a specified Library
   * @param libraryId of specific Library to delete.
   */
  void delete(ULong libraryId) throws DatabaseException, ConfigException, BusinessException;
}
