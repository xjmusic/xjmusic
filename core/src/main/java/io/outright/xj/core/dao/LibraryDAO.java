// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.model.library.Library;
import io.outright.xj.core.tables.records.LibraryRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;

public interface LibraryDAO {
  /**
   (ADMIN ONLY)
   Create a new Library

   @param entity for the new Library.
   @return newly readMany Library record.
   */
  LibraryRecord create(Access access, Library entity) throws Exception;

  /**
   Fetch one Library by id, if accessible

   @param access    control
   @param libraryId to fetch
   @return Library if found
   @throws Exception on failure
   */
  @Nullable
  LibraryRecord readOne(Access access, ULong libraryId) throws Exception;

  /**
   Read all Libraries that are accessible

   @param access control
   @return array of libraries as JSON
   @throws Exception on failure
   */
  Result<LibraryRecord> readAll(Access access, ULong accountId) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Library

   @param libraryId of specific Library to update.
   @param entity    for the updated Library.
   */
  void update(Access access, ULong libraryId, Library entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified Library

   @param libraryId of specific Library to delete.
   */
  void delete(Access access, ULong libraryId) throws Exception;
}
