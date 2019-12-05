// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

import java.util.Collection;
import java.util.UUID;

public interface DAO<E extends Entity> {

  /**
   Create a new Record

   @param access control
   @param entity for the new Record
   @return newly readMany record
   */
  E create(Access access, E entity) throws CoreException;

  /**
   Create many new records.
   There's a default implementation of this, the slowest possible version, iterating over records and adding them.
   Override that for classes that clearly benefit of batch writing. (*ahem* segment entities)@param access

   @param entities to of many of
   */
  void createMany(Access access, Collection<E> entities) throws CoreException;

  /**
   Delete a specified Entity@param access control

   @param id of specific Entity to delete.
   */
  void destroy(Access access, UUID id) throws CoreException;

  /**
   Create a new instance of this type of Entity

   @return new entity instance
   */
  E newInstance();

  /**
   Fetch many records for many parents by id, if accessible

   @param access    control
   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws CoreException on failure
   */
  Collection<E> readMany(Access access, Collection<UUID> parentIds) throws CoreException;

  /**
   Fetch one record  if accessible

   @param access control
   @param id     of record to fetch
   @return retrieved record
   @throws CoreException on failure
   */
  E readOne(Access access, UUID id) throws CoreException;

  /**
   Update a specified Entity@param access control

   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   */
  void update(Access access, UUID id, E entity) throws CoreException;

}
