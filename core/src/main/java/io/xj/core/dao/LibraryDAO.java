// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.library.Library;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface LibraryDAO {
  /**
   (ADMIN ONLY)
   Create a new Library

   @param entity for the new Library.
   @return newly readMany Library record.
   */
  Library create(Access access, Library entity) throws Exception;

  /**
   Fetch one Library by id, if accessible

   @return Library if found
   @throws Exception on failure
    @param access    control
   @param id to fetch
   */
  @Nullable
  Library readOne(Access access, BigInteger id) throws Exception;

  /**
   Read all Libraries that are accessible

   @param access    control
   @param accountId to get libraries of
   @return array of libraries as JSON
   @throws Exception on failure
   */
  Collection<Library> readAll(Access access, BigInteger accountId) throws Exception;

  /**
   (ADMIN ONLY)
   Update a specified Library
   * @param id of specific Library to update.
   @param entity    for the updated Library.

   */
  void update(Access access, BigInteger id, Library entity) throws Exception;

  /**
   (ADMIN ONLY)
   Delete a specified Library
   * @param id of specific Library to delete.

   */
  void delete(Access access, BigInteger id) throws Exception;
}
