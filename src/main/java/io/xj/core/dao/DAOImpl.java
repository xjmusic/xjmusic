// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableRecord;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class DAOImpl<E extends Entity> implements DAO<E> {
  private static final Logger log = LoggerFactory.getLogger(DAOImpl.class);
  protected SQLDatabaseProvider dbProvider;

  /**
   Definitely not null, or string "null"

   @param obj to ingest for non-nullness
   @return true if non-null
   */
  public static boolean isNonNull(Object obj) {
    return Objects.nonNull(obj) &&
      !Objects.equals("null", String.valueOf(obj));
  }



  /**
   Execute a database CREATE operation

   @param <R>    record type dynamic
   @param table  to of entity in
   @param entity to of
   @return record
   */
  public <R extends UpdatableRecord<R>> R executeCreate(Table<R> table, E entity) throws CoreException {
    DSLContext db = dbProvider.getDSL();
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
   Execute a database UPDATE operation@param <R>        record type dynamic@param table      to update

   @param id of record to update
   */
  public <R extends UpdatableRecord<R>> void executeUpdate(Table<R> table, UUID id, E entity) throws CoreException {
    DSLContext db = dbProvider.getDSL();
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
  public <R extends Record> void requireNotExists(String name, Collection<R> result) throws CoreException {
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
  public void requireNotExists(String name, int count) throws CoreException {
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
  public <R extends Record> void requireExists(String name, R record) throws CoreException {
    require(name, "does not exist", isNonNull(record));
  }

  /**
   Require that a entity isNonNull

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws CoreException if not isNonNull
   */
  public void requireExists(String name, E entity) throws CoreException {
    require(name, "does not exist", isNonNull(entity));
  }

  /**
   Require that a count of a record isNonNull

   @param name  name of record (for error message)
   @param count to require existence of
   @throws CoreException if not isNonNull
   */
  public void requireExists(String name, int count) throws CoreException {
    require(name, "does not exist", 0 < count);
  }

  /**
   Require user has access to account #

   @param access    control
   @param accountId to check for access to
   @throws CoreException if not admin
   */
  public void requireAccount(Access access, UUID accountId) throws CoreException {
    require("access to account #" + accountId, access.hasAccount(accountId));
  }

  /**
   Require user has admin access

   @param access control
   @throws CoreException if not admin
   */
  public void requireTopLevel(Access access) throws CoreException {
    require("top-level access", access.isTopLevel());
  }

  /**
   Require has user-level access

   @param access control
   @throws CoreException if not user
   */
  public void requireUser(Access access) throws CoreException {
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.USER))
      throw new CoreException("No user access");
  }

  /**
   Require has engineer-level access

   @param access control
   @throws CoreException if not engineer
   */
  public void requireEngineer(Access access) throws CoreException {
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.ENGINEER))
      throw new CoreException("No engineer access");
  }

  /**
   ASSUMED an entity.parentId() is a libraryId for this class of entity
   Require library-level access to an entity

   @param access control
   @throws CoreException if does not have access
   */
  public void requireArtist(Access access) throws CoreException {
    // TODO require a specific set of library ids, and check for access to all those libraries
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.ARTIST))
      throw new CoreException("No library access");
  }

  /**
   Require user has admin access

   @param access control
   @throws CoreException if not admin
   */
  public void requireRole(String message, Access access, UserRoleType... roles) throws CoreException {
    require(message, access.isTopLevel() || access.isAllowed(roles));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws CoreException if not true
   */
  public void require(String name, Boolean mustBeTrue) throws CoreException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param message    condition (for error message)
   @param mustBeTrue to require true
   @param condition  to append
   @throws CoreException if not true
   */
  public void require(String message, String condition, Boolean mustBeTrue) throws CoreException {
    if (!mustBeTrue) {
      throw new CoreException(message + " " + condition);
    }
  }

  /**
   Execute a database CREATE operation@param <R>        record type dynamic

   @param table    to of
   @param entities to batch insert
   */
  protected <R extends UpdatableRecord<R>> void executeCreateMany(Table<R> table, Collection<E> entities) throws CoreException {
    DSLContext db = dbProvider.getDSL();

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
   Utility to do a bunch of cloning,
   that keeps an inner UUID -> UUID map of all original ids to cloned ids
   and then swaps out all parent ids for the cloned parent ids
   of each successive cloning.
   */
  public class Cloner {
    Map<UUID, UUID> clonedIds = Maps.newConcurrentMap();

    /**
     Instantiate a cloner with a new database dataSource
     */
    public Cloner() {
    }

    /**
     Clone all records with a specified parent id to a new parent id,
     for each of the belongs-to relationships, if it belongs to a cloned id, replace the value with the cloned belongs-to id
     and return a UUID -> UUID map of each original record to the newly cloned id record

     @param table         in which to clone records (rows)
     @param idField       id column
     @param parentIdField parent id column
     @param fromParentId  to match records with
     @param toParentId    to of new records
     @param <R>           type of record
     */
    public <R extends TableRecord<?>> void clone(
      Table<R> table,
      TableField<R, UUID> idField,
      Collection<TableField<R, UUID>> belongsToIdFields,
      TableField<R, UUID> parentIdField,
      UUID fromParentId,
      UUID toParentId
    ) throws CoreException {
      Collection<R> toInsert = Lists.newArrayList();
      DSLContext db = dbProvider.getDSL();
      db.selectFrom(table)
        .where(parentIdField.eq(fromParentId))
        .fetch()
        .forEach(record -> {
          UUID originalId = record.get(idField);
          UUID clonedId = UUID.randomUUID();
          clonedIds.put(originalId, clonedId);

          // for each of the belongs-to relationships, if it belongs to a cloned id, replace the value with the cloned belongs-to id
          belongsToIdFields.forEach(belongsToIdField -> {
            if (Objects.nonNull(record.get(belongsToIdField))
              && clonedIds.containsKey(record.get(belongsToIdField)))
              record.set(belongsToIdField, clonedIds.get(record.get(belongsToIdField)));
          });
          record.set(idField, clonedId);
          record.set(parentIdField, toParentId);
          toInsert.add(record);
        });

      int[] rows = db.batchInsert(toInsert).execute();
      if (rows.length != toInsert.size())
        throw new CoreException(String.format("Only created %d out create %d intended %s records", rows.length, toInsert.size(), table.getName()));
    }

  }

}
