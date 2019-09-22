//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.entity.EntityFactory;
import io.xj.core.model.entity.Resource;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.util.Text;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.xj.core.Tables.LIBRARY;

public class DAOImpl {
  private static final Logger log = LoggerFactory.getLogger(DAOImpl.class);
  protected SQLDatabaseProvider dbProvider;

  /**
   Execute a database CREATE operation

   @param db            context
   @param table         to create
   @param fieldValueMap of fields to create, and values to create with
   @param <R>           record type dynamic
   @return record
   */
  static <R extends UpdatableRecord<R>> R executeCreate(DSLContext db, Table<R> table, Map<Field, Object> fieldValueMap) throws CoreException {
    R record = db.newRecord(table);
    fieldValueMap.forEach(record::setValue);

    try {
      record.store();
    } catch (Exception e) {
      log.error("Cannot create record because {}", e.getMessage());
      throw new CoreException(String.format("Cannot create record because %s", e.getMessage()));
    }

    return record;
  }

  /**
   Execute a database UPDATE operation

   @param db            context
   @param table         to update
   @param fieldValueMap of fields to update, and values to update with
   @param <R>           record type dynamic
   @return record
   */
  static <R extends UpdatableRecord<R>> int executeUpdate(DSLContext db, Table<R> table, Map<Field, Object> fieldValueMap) throws CoreException {
    R record = db.newRecord(table);
    fieldValueMap.forEach(record::setValue);

    try {
      return db.executeUpdate(record);
    } catch (Exception e) {
      log.error("Cannot update record because {}", e.getMessage());
      throw new CoreException(String.format("Cannot update record because %s", e.getMessage()));
    }
  }

  /**
   Require empty Result

   @param name   to require
   @param result to check.
   @throws CoreException if result set is not empty.
   @throws CoreException if something goes wrong.
   */
  static <R extends Record> void requireNotExists(String name, Collection<R> result) throws CoreException {
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
  static void requireNotExists(String name, int count) throws CoreException {
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
  static void requireExists(String name, Record record) throws CoreException {
    require(name, "does not exist", isNonNull(record));
  }

  /**
   Require that a entity isNonNull

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws CoreException if not isNonNull
   */
  static void requireExists(String name, Entity entity) throws CoreException {
    require(name, "does not exist", isNonNull(entity));
  }

  /**
   Require that a count of a record isNonNull

   @param name  name of record (for error message)
   @param count to require existence of
   @throws CoreException if not isNonNull
   */
  static void requireExists(String name, int count) throws CoreException {
    require(name, "does not exist", 0 < count);
  }

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
   Require user has access to account #

   @param access    control
   @param accountId to check for access to
   @throws CoreException if not admin
   */
  static void requireAccount(Access access, ULong accountId) throws CoreException {
    require("access to account #" + accountId, access.hasAccount(accountId.toBigInteger()));
  }

  /**
   Require user has admin access

   @param access control
   @throws CoreException if not admin
   */
  static void requireTopLevel(Access access) throws CoreException {
    require("top-level access", access.isTopLevel());
  }

  /**
   ASSUMED an entity.parentId() is a libraryId for this class of entity
   Require library-level access to an entity

   @param db     context
   @param access control
   @param entity to require library access to
   @throws CoreException if does not have access
   */
  static void requireLibraryAccess(DSLContext db, Access access, Entity entity) throws CoreException {
    if (access.isTopLevel())
      requireExists("Library access",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ID.eq(ULong.valueOf(entity.getParentId())))
          .fetchOne(0, int.class));
    else
      requireExists("Library access",
        db.selectCount().from(LIBRARY)
          .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .and(LIBRARY.ID.eq(ULong.valueOf(entity.getParentId())))
          .fetchOne(0, int.class));
  }

  /**
   Require user has admin access

   @param access control
   @throws CoreException if not admin
   */
  static void requireRole(String message, Access access, UserRoleType... roles) throws CoreException {
    require(message, access.isTopLevel() || access.isAllowed(roles));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws CoreException if not true
   */
  static void require(String name, Boolean mustBeTrue) throws CoreException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param message condition (for error message)
   @param mustBeTrue  to require true
   @param condition   to append
   @throws CoreException if not true
   */
  private static void require(String message, String condition, Boolean mustBeTrue) throws CoreException {
    if (!mustBeTrue) {
      throw new CoreException(message + " " + condition);
    }
  }

  /**
   Value, or DSL null object

   @param value to check
   @return value or null
   */
  static Object valueOrNull(Object value) {
    return Objects.nonNull(value) ? value : DSL.val((String) null);
  }

  /**
   Build collection of jooq ULong from global BigInteger

   @param segmentIds to build from
   @return collection of segment ids
   */
  static Collection<ULong> idCollection(Collection<BigInteger> segmentIds) {
    return segmentIds.stream().map(ULong::valueOf).collect(Collectors.toList());
  }

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param records    to source values from
   @param modelClass instance of a single target entity
   @return entity after transmogrification
   @throws CoreException on failure to transmogrify
   */
  static <R extends Record, E extends Entity> Collection<E> modelsFrom(Iterable<R> records, Class<E> modelClass) throws CoreException {
    Collection<E> models = Lists.newArrayList();
    for (R record : records) models.add(modelFrom(record, modelClass));
    return models;
  }

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param records to source values from
   @param factory from which to get a new entity instance
   @return entity after transmogrification
   @throws CoreException on failure to transmogrify
   */
  static <R extends Record, E extends Entity> Collection<E> modelsFrom(Iterable<R> records, EntityFactory<E> factory) throws CoreException {
    Collection<E> models = Lists.newArrayList();
    for (R record : records) models.add(modelFrom(record, factory));
    return models;
  }

  /**
   Transmogrify the field-value pairs from a jOOQ record and set values on the corresponding POJO entity.

   @param record     to source field-values from
   @param modelClass to whose setters the values will be written
   @return entity after transmogrification
   @throws CoreException on failure to transmogrify
   */
  static <R extends Record, E extends Entity> E modelFrom(R record, Class<E> modelClass) throws CoreException {
    if (Objects.isNull(modelClass))
      throw new CoreException("Will not transmogrify null modelClass");

    // new instance of model
    E model;
    try {
      model = modelClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new CoreException(String.format("Could not get a new instance of class %s because %s", modelClass, e));
    }

    // set all values
    modelSetTransmogrified(record, model);

    return model;
  }

  /**
   Transmogrify the field-value pairs from a jOOQ record and set values on the corresponding POJO entity.

   @param record  to source field-values from
   @param factory from which to get a new entity instance
   @return entity after transmogrification
   @throws CoreException on failure to transmogrify
   */
  static <R extends Record, E extends Entity> E modelFrom(R record, EntityFactory<E> factory) throws CoreException {
    if (Objects.isNull(record))
      throw new CoreException("Record does not exist");

    // new instance of model
    E model;
    try {
      model = factory.newInstance();
    } catch (Exception e) {
      throw new CoreException(String.format("Could not get a new instance from %s because %s", Text.getSimpleName(factory), e));
    }

    // set all values
    modelSetTransmogrified(record, model);

    return model;
  }

  /**
   Set all fields of an Entity using values transmogrified from a jOOQ Record

   @param record to transmogrify values from
   @param model  to set fields of
   @param <R>    type of Record
   @param <E>    type of Entity
   @throws CoreException on failure to set transmogrified values
   */
  static <R extends Record, E extends Resource> void modelSetTransmogrified(R record, E model) throws CoreException {
    if (Objects.isNull(record))
      throw new CoreException("Cannot transmogrify; record does not exist");

    Map<String, Object> fieldValues = record.intoMap();
    for (Map.Entry<String, Object> field : fieldValues.entrySet())
      if (isNonNull(field.getValue())) try {
        String attributeName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field.getKey());
        model.set(attributeName, field.getValue());
      } catch (Exception e) {
        log.error("Could not transmogrify key:{} val:{} because {}", field.getKey(), field.getValue(), e);
        throw new CoreException(String.format("Could not transmogrify key:%s val:%s because %s", field.getKey(), field.getValue(), e));
      }
  }

  /**
   Collection of ULong from collection of BigInteger

   @param libraryIds source collection
   @return collection of ULong
   */
  protected static Collection<ULong> uLongValuesOf(Collection<BigInteger> libraryIds) {
    return libraryIds.stream().map(ULong::valueOf).collect(Collectors.toList());
  }


}
