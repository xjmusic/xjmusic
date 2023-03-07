// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Values;

public abstract class ManagerImpl<E> implements Manager<E> {
  protected final EntityFactory entityFactory;
  protected final NexusEntityStore store;

  public ManagerImpl(
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
   @throws ManagerExistenceException if not isNonNull
   */
  protected void requireExists(String name, E entity) throws ManagerExistenceException {
    if (!Values.isNonNull(entity)) throw new ManagerExistenceException(String.format("%s does not exist!", name));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws ManagerPrivilegeException if not true
   */
  protected void require(String name, Boolean mustBeTrue) throws ManagerPrivilegeException {
    if (!mustBeTrue) throw new ManagerPrivilegeException(name + " is required.");
  }

}
