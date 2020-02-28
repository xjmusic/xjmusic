// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.common.collect.Lists;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.lib.core.Tables.LIBRARY;
import static io.xj.lib.core.Tables.PROGRAM;

abstract class DAOImpl<E extends Entity> implements DAO<E> {
  private static final Logger log = LoggerFactory.getLogger(DAOImpl.class);
  protected SQLDatabaseProvider dbProvider;

  /**
   Definitely not null, or string "null"

   @param obj to ingest for non-nullness
   @return true if non-null
   */
  static boolean isNonNull(Object obj) {
    return Objects.nonNull(obj) &&
      !Objects.equals("null", String.valueOf(obj));
  }


  /**
   Execute a database CREATE operation

   @param <R>    record type dynamic
   @param db     DSL context
   @param table  to of entity in
   @param entity to of
   @return record
   */
  protected <R extends UpdatableRecord<R>> R executeCreate(DSLContext db, Table<R> table, E entity) throws CoreException {
    R record = db.newRecord(table);
    DAO.setAll(record, entity);

    try {
      record.store();
    } catch (Exception e) {
      log.error("Cannot create record because {}", e.getMessage());
      throw new CoreException(String.format("Cannot create record because %s", e.getMessage()));
    }

    return record;
  }

  /**
   Execute a database UPDATE operation@param <R>        record type dynamic@param table      to update@param db

   @param id of record to update
   */
  protected <R extends UpdatableRecord<R>> void executeUpdate(DSLContext db, Table<R> table, UUID id, E entity) throws CoreException {
    R record = db.newRecord(table);
    DAO.setAll(record, entity);
    DAO.set(record, "id", id);

    if (0 == db.executeUpdate(record))
      throw new CoreException("No records updated.");
  }

  /**
   Require empty Result

   @param name   to require
   @param result to check.
   @throws CoreException if result set is not empty.
   @throws CoreException if something goes wrong.
   */
  <R extends Record> void requireNotExists(String name, Collection<R> result) throws CoreException {
    if (isNonNull(result) && !result.isEmpty()) {
      throw new CoreException("Found" + " " + name);
    }
  }

  /**
   Require empty count of a Result

   @param name  to require
   @param count to check.
   @throws CoreException if result set is not empty.
   @throws CoreException if something goes wrong.
   */
  void requireNotExists(String name, int count) throws CoreException {
    if (0 < count) {
      throw new CoreException("Found" + " " + name);
    }
  }

  /**
   Require that a record isNonNull

   @param name   name of record (for error message)
   @param record to require existence of
   @throws CoreException if not isNonNull
   */
  protected <R extends Record> void requireExists(String name, R record) throws CoreException {
    require(name, "does not exist", isNonNull(record));
  }

  /**
   Require that a entity isNonNull

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws CoreException if not isNonNull
   */
  protected void requireExists(String name, E entity) throws CoreException {
    require(name, "does not exist", isNonNull(entity));
  }

  /**
   Require that a count of a record isNonNull

   @param name  name of record (for error message)
   @param count to require existence of
   @throws CoreException if not isNonNull
   */
  protected void requireExists(String name, int count) throws CoreException {
    require(name, "does not exist", 0 < count);
  }

  /**
   Require user has access to account #

   @param access    control
   @param accountId to check for access to
   @throws CoreException if not admin
   */
  void requireAccount(Access access, UUID accountId) throws CoreException {
    require("access to account #" + accountId, access.hasAccount(accountId));
  }

  /**
   Require user has admin access

   @param access control
   @throws CoreException if not admin
   */
  protected void requireTopLevel(Access access) throws CoreException {
    require("top-level access", access.isTopLevel());
  }

  /**
   Require has user-level access

   @param access control
   @throws CoreException if not user
   */
  void requireUser(Access access) throws CoreException {
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.USER))
      throw new CoreException("No user access");
  }

  /**
   Require has engineer-level access

   @param access control
   @throws CoreException if not engineer
   */
  void requireEngineer(Access access) throws CoreException {
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.ENGINEER))
      throw new CoreException("No engineer access");
  }

  /**
   ASSUMED an entity.parentId() is a libraryId for this class of entity
   Require library-level access to an entity

   @param access control
   @throws CoreException if does not have access
   */
  protected void requireArtist(Access access) throws CoreException {
    // TODO require a specific set of library ids, and check for access to all those libraries
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.ARTIST))
      throw new CoreException("No artist access");
  }

  /**
   Require user has admin access

   @param access control
   @throws CoreException if not admin
   */
  void requireRole(String message, Access access, UserRoleType... roles) throws CoreException {
    require(message, access.isTopLevel() || access.isAllowed(roles));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws CoreException if not true
   */
  protected void require(String name, Boolean mustBeTrue) throws CoreException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param message    condition (for error message)
   @param mustBeTrue to require true
   @param condition  to append
   @throws CoreException if not true
   */
  protected void require(String message, String condition, Boolean mustBeTrue) throws CoreException {
    if (!mustBeTrue) {
      throw new CoreException(message + " " + condition);
    }
  }

  /**
   Execute a database CREATE operation@param <R>        record type dynamic@param db

   @param table    to of
   @param entities to batch insert
   */
  <R extends UpdatableRecord<R>> void executeCreateMany(DSLContext db, Table<R> table, Collection<E> entities) throws CoreException {
    Collection<R> records = Lists.newArrayList();
    for (E entity : entities) {
      R record = db.newRecord(table);
      DAO.setAll(record, entity);
      // also set id if provided, creating a new record with that id
      if (Objects.nonNull(entity.getId()))
        DAO.set(record, "id", entity.getId());
      records.add(record);
    }

    try {
      db.batchInsert(records);
    } catch (Exception e) {
      log.error("Cannot create record because {}", e.getMessage());
      throw new CoreException(String.format("Cannot create record because %s", e.getMessage()));
    }
  }

  @Override
  public void createMany(Access access, Collection<E> entities) throws CoreException {
    for (E entity : entities) create(access, entity);
  }

  /**
   Require permission to modify the specified program

   @param db     context
   @param access control
   @param id     of entity to require modification access to
   @throws CoreException on invalid permissions
   */
  void requireProgramModification(DSLContext db, Access access, UUID id) throws CoreException {
    requireArtist(access);

    if (access.isTopLevel())
      requireExists("Program", db.selectCount().from(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Program in Account you have access to", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }


}
