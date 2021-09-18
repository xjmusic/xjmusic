// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.service;

import com.google.inject.Inject;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Value;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.service.exception.ServiceExistenceException;
import io.xj.nexus.service.exception.ServicePrivilegeException;

public abstract class ServiceImpl<E> implements Service<E> {
  protected final EntityFactory entityFactory;
  protected final NexusEntityStore store;

  @Inject
  public ServiceImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore
  ) {
    this.entityFactory = entityFactory;
    this.store = nexusEntityStore;
  }

  /**
   Require that an entity is non-null

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws ServiceExistenceException if not isNonNull
   */
  protected void requireExists(String name, E entity) throws ServiceExistenceException {
    if (!Value.isNonNull(entity)) throw new ServiceExistenceException(String.format("%s does not exist!", name));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws ServicePrivilegeException if not true
   */
  protected void require(String name, Boolean mustBeTrue) throws ServicePrivilegeException {
    if (!mustBeTrue) throw new ServicePrivilegeException(name + " is required.");
  }

}
