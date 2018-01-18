// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.util.CamelCasify;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.LINK;

public class DAOImpl {
  private static final Logger log = LoggerFactory.getLogger(DAOImpl.class);
  SQLDatabaseProvider dbProvider;

  /**
   Execute a database CREATE operation

   @param db            context
   @param table         to create
   @param fieldValueMap of fields to create, and values to create with
   @param <R>           record type dynamic
   @return record
   */
  static <R extends UpdatableRecord<R>> R executeCreate(DSLContext db, Table<R> table, Map<Field, Object> fieldValueMap) throws BusinessException {
    R record = db.newRecord(table);
    fieldValueMap.forEach(record::setValue);

    try {
      record.store();
    } catch (Exception e) {
      log.error("Cannot create record", e);
      throw new BusinessException("Cannot create record", e);
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
  static <R extends UpdatableRecord<R>> int executeUpdate(DSLContext db, Table<R> table, Map<Field, Object> fieldValueMap) throws BusinessException {
    R record = db.newRecord(table);
    fieldValueMap.forEach(record::setValue);

    try {
      return db.executeUpdate(record);
    } catch (Exception e) {
      log.error("Cannot update record", e);
      throw new BusinessException("Cannot update record", e);
    }
  }

  /**
   Require empty Result

   @param name   to require
   @param result to check.
   @throws BusinessException if result set is not empty.
   @throws Exception         if something goes wrong.
   */
  static <R extends Record> void requireNotExists(String name, Collection<R> result) throws Exception {
    if (isNonNull(result) && !result.isEmpty()) {
      throw new BusinessException("Found" + " " + name);
    }
  }

  /**
   Require empty count of a Result

   @param name  to require
   @param count to check.
   @throws BusinessException if result set is not empty.
   @throws Exception         if something goes wrong.
   */
  static void requireNotExists(String name, int count) throws Exception {
    if (0 < count) {
      throw new BusinessException("Found" + " " + name);
    }
  }

  /**
   Require that a record isNonNull

   @param name   name of record (for error message)
   @param record to require existence of
   @throws BusinessException if not isNonNull
   */
  static void requireExists(String name, Record record) throws BusinessException {
    require(name, "does not exist", isNonNull(record));
  }

  /**
   Require that a entity isNonNull

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws BusinessException if not isNonNull
   */
  static void requireExists(String name, Entity entity) throws BusinessException {
    require(name, "does not exist", isNonNull(entity));
  }

  /**
   Require that a count of a record isNonNull

   @param name  name of record (for error message)
   @param count to require existence of
   @throws BusinessException if not isNonNull
   */
  static void requireExists(String name, int count) throws BusinessException {
    require(name, "does not exist", 0 < count);
  }

  /**
   Require access to all of a collection of links

   @param db      context
   @param access  control
   @param linkIds to require access to
   */
  static void requireAccessToLinks(DSLContext db, Access access, Collection<ULong> linkIds) throws BusinessException {
    if (!access.isTopLevel()) {
      int accessLinkCount = db.selectCount().from(LINK)
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK.ID.in(linkIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class);
      require(String.format("exactly the provided count (%d) links in chain(s) to which user has access", linkIds.size()), Objects.equals(linkIds.size(), accessLinkCount));
    }
  }

  /**
   Require that a count of any of a list of record isNonNull

   @param message for error, if thrown
   @param counts  to require existence of any of
   @throws BusinessException if not isNonNull
   */
  static void requireExistsAnyOf(String message, int... counts) throws BusinessException {
    Boolean exists = false;
    for (int count : counts) {
      if (0 < count) {
        exists = true;
      }
    }
    if (!exists) {
      throw new BusinessException(message);
    }
  }


  /**
   Definitely not null, or string "null"

   @param obj to evaluate for non-nullness
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
   @throws BusinessException if not admin
   */
  static void requireAccount(Access access, ULong accountId) throws BusinessException {
    require("access to account #" + accountId, access.hasAccount(accountId.toBigInteger()));
  }

  /*
   Require a given object is not null

   @param description  what is it
   @param cannotBeNull value
   @throws BusinessException if null
   *
  static void requireNonNull(String description, Object cannotBeNull) throws BusinessException {
    require(description, Objects.nonNull(cannotBeNull));
  }
  */

  /**
   Require a given integer is greater than zero

   @param description           what is it
   @param mustBeGreaterThanZero value
   @throws BusinessException if null
   */
  static void requireGreaterThanZero(String description, @Nullable Integer mustBeGreaterThanZero) throws BusinessException {
    require(description, "must be greater than zero", Objects.nonNull(mustBeGreaterThanZero) && 0 < mustBeGreaterThanZero);
  }

  /**
   Require user has admin access

   @param access control
   @throws BusinessException if not admin
   */
  static void requireTopLevel(Access access) throws BusinessException {
    require("top-level access", access.isTopLevel());
  }

  /**
   Require user has admin access

   @param access control
   @throws BusinessException if not admin
   */
  static void requireRole(String message, Access access, UserRoleType... roles) throws BusinessException {
    require(message, access.isTopLevel() || access.isAllowed(roles));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws BusinessException if not true
   */
  static void require(String name, Boolean mustBeTrue) throws BusinessException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param description name of condition (for error message)
   @param mustBeTrue  to require true
   @param condition   to append
   @throws BusinessException if not true
   */
  private static void require(String description, String condition, Boolean mustBeTrue) throws BusinessException {
    if (!mustBeTrue) {
      throw new BusinessException(description + " " + condition);
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

   @param linkIds to build from
   @return collection of link ids
   */
  static Collection<ULong> idCollection(Collection<BigInteger> linkIds) {
    Collection<ULong> result = Lists.newArrayList();
    linkIds.forEach((id) -> result.add(ULong.valueOf(id)));
    return result;
  }

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param records    to source values from
   @param modelClass instance of a single target entity
   @return entity after transmogrification
   @throws BusinessException on failure to transmogrify
   */
  static <R extends Record, E extends Entity> Collection<E> modelsFrom(Iterable<R> records, Class<E> modelClass) throws BusinessException {
    Collection<E> models = Lists.newArrayList();
    for (R record : records) {
      models.add(modelFrom(record, modelClass));
    }
    return models;
  }

  /**
   Transmogrify the field-value pairs from a jOOQ record and set values on the corresponding POJO entity.

   @param record     to source field-values from
   @param modelClass to whose setters the values will be written
   @return entity after transmogrification
   @throws BusinessException on failure to transmogrify
   */
  static <R extends Record, E extends Entity> E modelFrom(R record, Class<E> modelClass) throws BusinessException {
    if (Objects.isNull(modelClass) || Objects.isNull(record)) {
      return null;
    }

    E model;
    try {
      model = modelClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new BusinessException(String.format("Could not transmogrify into class: %s", modelClass), e);
    }

    Map<String, Object> fieldValues = record.intoMap();
    for (Map.Entry<String, Object> field : fieldValues.entrySet()) {
      if (isNonNull(field.getValue())) try {
        modelSetTransmogrified(model, field.getKey(), field.getValue());
      } catch (Exception e) {
        log.error("Could not transmogrify key:{} val:{}", field.getKey(), field.getValue(), e);
        throw new BusinessException(String.format("Could not transmogrify key:%s val:%s", field.getKey(), field.getValue()), e);
      }
    }

    // this is necessary to port in some values, e.g. state enums from string setters
    try {
      model.validate();
    } catch (BusinessException e) {
      log.error("Entity threw exception during post-transmogrification validation", e);
      throw new BusinessException("Entity threw exception during post-transmogrification validation", e);
    }

    return model;
  }

  /**
   Set the setter of a POJO Entity, based on the transmogrified value from a jOOQ Record Field

   @param model to set the setter of
   @param key   of the jOOQ Record Field
   @param value from the jOOQ Record Field
   @param <E>   is the type of the entity being written to
   */
  private static <E extends Entity> void modelSetTransmogrified(E model, String key, Object value) throws Exception {
    String setterName = String.format("set%s", CamelCasify.upper(key));

    /*
    Allows for special columns from db transactions to be mapped to special setters,
    while being tolerant of extra columns, e.g. join artifacts
     */
    if (!hasMethod(model, setterName)) {
      log.debug("{} does not have {}(); skipping.", model, setterName);
      return;
    }

    if (isNonNull(value)) switch (value.getClass().getSimpleName()) {

      case "ULong":
      case "BigInteger":
        model.getClass().getMethod(setterName, BigInteger.class)
          .invoke(model, new BigInteger(String.valueOf(value)));
        break;

      case "Timestamp":
        model.getClass().getMethod(setterName, String.class)
          .invoke(model, String.valueOf(value));
        break;

      case "UInteger":
      case "Integer":
        model.getClass().getMethod(setterName, Integer.class)
          .invoke(model, Integer.valueOf(String.valueOf(value)));
        break;

      case "Long":
        model.getClass().getMethod(setterName, Long.class)
          .invoke(model, Long.valueOf(String.valueOf(value)));
        break;

      case "Double":
        model.getClass().getMethod(setterName, Double.class)
          .invoke(model, Double.valueOf(String.valueOf(value)));
        break;

      case "Float":
        model.getClass().getMethod(setterName, Float.class)
          .invoke(model, Float.valueOf(String.valueOf(value)));
        break;

      default:
        model.getClass().getMethod(setterName, String.class)
          .invoke(model, String.valueOf(value));
        break;
    }
  }

  /**
   Check if the target entity has any given method

   @param model      to test for methods
   @param setterName of method to test for
   @param <E>        extends Entity
   @return true if found, else false
   */
  private static <E extends Entity> boolean hasMethod(E model, String setterName) {
    for (Method method : model.getClass().getMethods()) {
      if (Objects.equals(method.getName(), setterName)) return true;
    }
    return false;
  }

  /**
   Collection of ULong from collection of BigInteger

   @param libraryIds source collection
   @return collection of ULong
   */
  protected static Collection<ULong> uLongValuesOf(Collection<BigInteger> libraryIds) {
    Collection<ULong> result = Lists.newArrayList();
    libraryIds.forEach(id -> result.add(ULong.valueOf(id)));
    return result;
  }


}
