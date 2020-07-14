// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.dao;

import io.xj.lib.entity.Entity;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;

import java.util.Collection;
import java.util.UUID;

/**
 Nexus base DAO interface
 <p>
 [#171553408] XJ Mk3 Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface DAO<N extends Entity> {

  /**
   Create a new Record

   @param access control
   @param entity for the new Record
   @return newly readMany record
   @throws DAOFatalException on failure
   */
  N create(HubClientAccess access, N entity) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException;

  /**
   Delete a specified Entity@param access control

   @param access control
   @param id     of specific Entity to delete.
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  void destroy(HubClientAccess access, UUID id) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException;

  /**
   Fetch many records for many parents by id, if accessible

   @param access    control
   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws DAOFatalException     on failure
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<N> readMany(HubClientAccess access, Collection<UUID> parentIds) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException;

  /**
   Fetch one record  if accessible

   @param access control
   @param id     of record to fetch
   @return retrieved record
   @throws DAOPrivilegeException if access is prohibited
   */
  N readOne(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Update a specified Entity

   @param access control
   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  void update(HubClientAccess access, UUID id, N entity) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException;

  /**
   New instance of the primary expected Entity class for a given DAO

   @return new instance of entity
   */
  N newInstance();
}
