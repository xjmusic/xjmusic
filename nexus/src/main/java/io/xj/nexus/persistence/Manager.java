// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import java.util.Collection;
import java.util.UUID;

/**
 Nexus base Manager interface
 <p>
 https://www.pivotaltracker.com/story/show/171553408 XJ Lab Distributed Architecture
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface Manager<E> {

  /**
   Create a new Record

   @param entity for the new Record
   @return newly readMany record
   @throws ManagerFatalException on failure
   */
  E create(E entity) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;

  /**
   Delete a specified Entity@param access control

   @param id of specific Entity to delete.
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  void destroy(UUID id) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException;

  /**
   Fetch many records for many parents by id, if accessible

   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws ManagerFatalException     on failure
   @throws ManagerPrivilegeException if access is prohibited
   */
  Collection<E> readMany(Collection<UUID> parentIds) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException;

  /**
   Fetch one record  if accessible

   @param id of record to fetch
   @return retrieved record
   @throws ManagerPrivilegeException if access is prohibited
   */
  E readOne(UUID id) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Update a specified Entity

   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  E update(UUID id, E entity) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;
}
