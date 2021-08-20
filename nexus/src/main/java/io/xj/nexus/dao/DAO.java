// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.dao;

import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;

import java.util.Collection;
import java.util.UUID;

/**
 Nexus base DAO interface
 <p>
 [#171553408] XJ Lab Distributed Architecture
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface DAO<E> {

  /**
   Create a new Record

   @param access control
   @param entity for the new Record
   @return newly readMany record
   @throws DAOFatalException on failure
   */
  E create(HubClientAccess access, E entity) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException;

  /**
   Delete a specified Entity@param access control

   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   @param access control
   @param id     of specific Entity to delete.
   */
  void destroy(HubClientAccess access, UUID id) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException;

  /**
   Fetch many records for many parents by id, if accessible

   @return collection of retrieved records
   @throws DAOFatalException     on failure
   @throws DAOPrivilegeException if access is prohibited
   @param access    control
   @param parentIds to fetch records for.
   */
  Collection<E> readMany(HubClientAccess access, Collection<UUID> parentIds) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException;

  /**
   Fetch one record  if accessible

   @return retrieved record
   @throws DAOPrivilegeException if access is prohibited
   @param access control
   @param id     of record to fetch
   */
  E readOne(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Update a specified Entity

   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   @param access control
   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   */
  E update(HubClientAccess access, UUID id, E entity) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException;

  /**
   New instance of the primary expected Entity class for a given DAO

   @return new instance of entity
   */
  E newInstance();
}
