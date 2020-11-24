// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.dao;

import com.google.inject.Inject;
import com.google.protobuf.MessageLite;
import io.xj.UserRole;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Value;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.persistence.NexusEntityStore;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public abstract class DAOImpl<E extends MessageLite> implements DAO<E> {
  protected final EntityFactory entityFactory;
  protected final NexusEntityStore store;

  @Inject
  public DAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore
  ) {
    this.entityFactory = entityFactory;
    this.store = nexusEntityStore;
  }

  /**
   Require empty Result

   @param name   to require
   @param result to check.
   @throws DAOExistenceException if result set is not empty.
   */
  protected void requireNotExists(String name, Collection<?> result) throws DAOExistenceException {
    if (Value.isNonNull(result) && !result.isEmpty()) throw new DAOExistenceException("Found" + " " + name);
  }

  /**
   Require empty count of a Result

   @param name  to require
   @param count to check.
   @throws DAOExistenceException if result set is not empty.
   */
  protected void requireNotExists(String name, int count) throws DAOExistenceException {
    if (0 < count) throw new DAOExistenceException("Found" + " " + name);
  }

  /**
   Require that a entity isNonNull

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws DAOExistenceException if not isNonNull
   */
  protected void requireExists(String name, E entity) throws DAOExistenceException {
    if (!Value.isNonNull(entity)) throw new DAOExistenceException(String.format("%s does not exist!", name));
  }

  /**
   Require that a count of a entity isNonNull

   @param name  name of entity (for error message)
   @param count to require existence of
   @throws DAOExistenceException if not isNonNull
   */
  protected void requireExists(String name, int count) throws DAOExistenceException {
    if (!(0 < count)) throw new DAOExistenceException(String.format("%s does not exist!", name));
  }

  /**
   Require that a entity isNonNull

   @param name     name of entity (for error message)
   @param entities to require existence of
   @throws DAOExistenceException if not isNonNull
   */
  protected void requireExists(String name, Collection<?> entities) throws DAOExistenceException {
    requireExists(name, entities.size());
  }

  /**
   Require user has admin access

   @param access to validate
   @throws DAOPrivilegeException if not admin
   */
  protected void requireTopLevel(HubClientAccess access) throws DAOPrivilegeException {
    require("top-level access", access.isTopLevel());
  }

  /**
   ASSUMED an entity.parentId() is a libraryId for this class of entity
   Require library-level access to an entity

   @param access to validate
   @throws DAOPrivilegeException if does not have access
   */
  protected void requireArtist(HubClientAccess access) throws DAOPrivilegeException {
    require(access, UserRole.Type.Artist);
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws DAOPrivilegeException if not true
   */
  protected void require(String name, Boolean mustBeTrue) throws DAOPrivilegeException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param message    condition (for error message)
   @param mustBeTrue to require true
   @param condition  to append
   @throws DAOPrivilegeException if not true
   */
  protected void require(String message, String condition, Boolean mustBeTrue) throws DAOPrivilegeException {
    if (!mustBeTrue) throw new DAOPrivilegeException(message + " " + condition);
  }

  /**
   Require user has access to account #

   @param accountId to check for access to
   @throws DAOPrivilegeException if not admin
   */
  protected void requireAccount(HubClientAccess access, String accountId) throws DAOPrivilegeException {
    if (access.isTopLevel()) return;
    require("Account access", access.hasAccount(accountId));
  }

  /**
   Require user has access to account # as well as a specific role

   @param access    to validate
   @param accountId to check for access to
   @throws DAOPrivilegeException if not admin
   */
  protected void requireAccount(HubClientAccess access, String accountId, UserRole.Type... allowedRoles) throws DAOPrivilegeException {
    requireAccount(access, accountId);
    require(access, allowedRoles);
  }

  /**
   Require access has one of the specified roles
   <p>
   Uses static formats to improve efficiency of method calls with less than 3 allowed roles

   @param access       to validate
   @param allowedRoles to require
   @throws DAOPrivilegeException if access does not have any one of the specified roles
   */
  protected void require(HubClientAccess access, UserRole.Type... allowedRoles) throws DAOPrivilegeException {
    if (access.isTopLevel()) return;
    if (3 < allowedRoles.length)
      require(
        String.format("%s role", Arrays.stream(allowedRoles).map(Enum::toString).collect(Collectors.joining("/"))),
        access.isAllowed(allowedRoles));
    else if (2 < allowedRoles.length)
      require(String.format("%s/%s/%s role", allowedRoles[0], allowedRoles[1], allowedRoles[2]),
        access.isAllowed(allowedRoles));
    else if (1 < allowedRoles.length)
      require(String.format("%s/%s role", allowedRoles[0], allowedRoles[1]),
        access.isAllowed(allowedRoles));
    else if (0 < allowedRoles.length)
      require(String.format("%s role", allowedRoles[0]),
        access.isAllowed(allowedRoles));
    else throw new DAOPrivilegeException("No roles allowed.");
  }

  /**
   Require has user-level access

   @param access to validate
   @throws DAOPrivilegeException if not user
   */
  protected void requireUser(HubClientAccess access) throws DAOPrivilegeException {
    require(access, UserRole.Type.User);
  }

  /**
   Require has engineer-level access

   @param access to validate
   @throws DAOPrivilegeException if not engineer
   */
  protected void requireEngineer(HubClientAccess access) throws DAOPrivilegeException {
    require(access, UserRole.Type.Engineer);
  }

  /**
   Require the the given runnable throws an exception.

   @param message            for real failure, if the given runnable does not fail
   @param mustThrowException when run, this must throw an exception
   */
  protected void requireNot(String message, Callable<?> mustThrowException) throws DAOValidationException {
    try {
      mustThrowException.call();
    } catch (Exception ignored) {
      return;
    }
    throw new DAOValidationException(message);
  }

}
