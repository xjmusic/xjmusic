// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.service;

import io.xj.nexus.service.exception.ServiceExistenceException;
import io.xj.nexus.service.exception.ServiceFatalException;
import io.xj.nexus.service.exception.ServicePrivilegeException;
import io.xj.nexus.service.exception.ServiceValidationException;

import java.util.Collection;
import java.util.UUID;

/**
 Nexus base Service interface
 <p>
 [#171553408] XJ Lab Distributed Architecture
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface Service<E> {

  /**
   Create a new Record

   @return newly readMany record
   @throws ServiceFatalException on failure
   @param entity for the new Record
   */
  E create(E entity) throws ServiceFatalException, ServiceExistenceException, ServicePrivilegeException, ServiceValidationException;

  /**
   Delete a specified Entity@param access control

   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   @param id     of specific Entity to delete.
   */
  void destroy(UUID id) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException;

  /**
   Fetch many records for many parents by id, if accessible

   @return collection of retrieved records
   @throws ServiceFatalException     on failure
   @throws ServicePrivilegeException if access is prohibited
   @param parentIds to fetch records for.
   */
  Collection<E> readMany(Collection<UUID> parentIds) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException;

  /**
   Fetch one record  if accessible

   @return retrieved record
   @throws ServicePrivilegeException if access is prohibited
   @param id     of record to fetch
   */
  E readOne(UUID id) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException;

  /**
   Update a specified Entity

   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   */
  E update(UUID id, E entity) throws ServiceFatalException, ServiceExistenceException, ServicePrivilegeException, ServiceValidationException;

  /**
   New instance of the primary expected Entity class for a given Service

   @return new instance of entity
   */
  E newInstance();
}
