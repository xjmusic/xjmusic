// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import java.util.Collection;
import java.util.UUID;

/**
 Nexus base Manager interface
 <p>
 [#171553408] XJ Lab Distributed Architecture
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface Manager<E> {

  /**
   Create a new Record

   @return newly readMany record
   @throws ManagerFatalException on failure
   @param entity for the new Record
   */
  E create(E entity) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;

  /**
   Delete a specified Entity@param access control

   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   @param id     of specific Entity to delete.
   */
  void destroy(UUID id) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException;

  /**
   Fetch many records for many parents by id, if accessible

   @return collection of retrieved records
   @throws ManagerFatalException     on failure
   @throws ManagerPrivilegeException if access is prohibited
   @param parentIds to fetch records for.
   */
  Collection<E> readMany(Collection<UUID> parentIds) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException;

  /**
   Fetch one record  if accessible

   @return retrieved record
   @throws ManagerPrivilegeException if access is prohibited
   @param id     of record to fetch
   */
  E readOne(UUID id) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Update a specified Entity

   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   */
  E update(UUID id, E entity) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;
}
