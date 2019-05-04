// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;

import java.math.BigInteger;
import java.util.Collection;

public interface DAO<E extends Entity> {

  /**
   Create a new Record

   @param access control
   @param entity for the new Record
   @return newly readMany record
   */
  E create(Access access, E entity) throws CoreException;

  /**
   Fetch one record  if accessible

   @param access control
   @param id     of record to fetch
   @return retrieved record
   @throws CoreException on failure
   */
  E readOne(Access access, BigInteger id) throws CoreException;

  /**
   Fetch many records for many parents by id, if accessible

   @param access    control
   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws CoreException on failure
   */
  Collection<E> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException;

  /**
   Update a specified Entity

   @param access control
   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   */
  void update(Access access, BigInteger id, E entity) throws CoreException;

  /**
   Delete a specified Entity

   @param access control
   @param id     of specific Entity to delete.
   */
  void destroy(Access access, BigInteger id) throws CoreException;
}
