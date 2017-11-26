// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.database.sql.SQLDatabaseProvider;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

public class DAOImpl {
  //  private static Logger log = LoggerFactory.getLogger(DAOImpl.class);
  SQLDatabaseProvider dbProvider;

  /**
   Transform a record into a record of a particular table, if not null

   @param table  to transform record into
   @param record to transform
   @return record of table
   */
  <R extends Record> R recordInto(Table<R> table, Record record) {
    return Objects.isNull(record) ? null : record.into(table);
  }

  /**
   Transform a result into a result of a particular table, if not null

   @param table  to transform result into
   @param result to transform
   @return result of table
   */
  <R extends Record> Result<R> resultInto(Table<R> table, Result result) {
    return Objects.isNull(result) ? null : result.into(table);
  }

  /**
   Execute a database CREATE operation

   @param db            context
   @param table         to create
   @param fieldValueMap of fields to create, and values to create with
   @param <R>           record type dynamic
   @return record
   */
  <R extends UpdatableRecord<R>> R executeCreate(DSLContext db, Table<R> table, Map<Field, Object> fieldValueMap) throws BusinessException {
    R record = db.newRecord(table);
    fieldValueMap.forEach(record::setValue);

    try {
      record.store();
    } catch (Exception e) {
      throw new BusinessException("Cannot create record: " + e.getMessage());
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
  <R extends UpdatableRecord<R>> int executeUpdate(DSLContext db, Table<R> table, Map<Field, Object> fieldValueMap) throws BusinessException {
    R record = db.newRecord(table);
    fieldValueMap.forEach(record::setValue);

    try {
      return db.executeUpdate(record);
    } catch (Exception e) {
      throw new BusinessException("Cannot update record: " + e.getMessage());
    }
  }

  /**
   Require empty Result

   @param name   to require
   @param result to check.
   @throws BusinessException if result set is not empty.
   @throws Exception         if something goes wrong.
   */
  <R extends Record> void requireNotExists(String name, Result<R> result) throws Exception {
    if (exists(result) && result.size() > 0) {
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
   Require that a record exists

   @param name   name of record (for error message)
   @param record to require existence of
   @throws BusinessException if not exists
   */
  void requireExists(String name, Record record) throws BusinessException {
    require(name, "does not exist", exists(record));
  }

  /**
   Require that a count of a record exists

   @param name  name of record (for error message)
   @param count to require existence of
   @throws BusinessException if not exists
   */
  void requireExists(String name, int count) throws BusinessException {
    require(name, "does not exist", 0 < count);
  }

  /**
   Require that a count of any of a list of record exists

   @param message for error, if thrown
   @param counts  to require existence of any of
   @throws BusinessException if not exists
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
   Whether a record exists

   @param obj to check for existence
   */
  Boolean exists(Object obj) {
    return Objects.nonNull(obj);
  }

  /**
   Require user has access to account #

   @param access    control
   @param accountId to check for access to
   @throws BusinessException if not admin
   */
  void requireAccount(Access access, ULong accountId) throws BusinessException {
    require("access to account #" + accountId, access.hasAccount(accountId));
  }

  /**
   Require a given object is not null

   @param description  what is it
   @param cannotBeNull value
   @throws BusinessException if null
   */
  void requireNonNull(String description, Object cannotBeNull) throws BusinessException {
    require(description, Objects.nonNull(cannotBeNull));
  }

  /**
   Require a given integer is greater than zero

   @param description           what is it
   @param mustBeGreaterThanZero value
   @throws BusinessException if null
   */
  void requireGreaterThanZero(String description, Integer mustBeGreaterThanZero) throws BusinessException {
    require(description, "must be greater than zero", mustBeGreaterThanZero > 0);
  }

  /**
   Require a given integer is greater than zero

   @param description           what is it
   @param mustBeGreaterThanZero value
   @throws BusinessException if null
   */
  void requireGreaterThanZero(String description, UInteger mustBeGreaterThanZero) throws BusinessException {
    require(description, "must be greater than zero", mustBeGreaterThanZero.compareTo(UInteger.valueOf(0)) == 1);
  }

  /**
   Require a given integer is greater than zero

   @param description           what is it
   @param mustBeGreaterThanZero value
   @throws BusinessException if null
   */
  void requireGreaterThanZero(String description, BigInteger mustBeGreaterThanZero) throws BusinessException {
    require(description, "must be greater than zero", mustBeGreaterThanZero.compareTo(BigInteger.ZERO) == 1);
  }

  /**
   Require a given integer is greater than zero

   @param description           what is it
   @param mustBeGreaterThanZero value
   @throws BusinessException if null
   */
  void requireGreaterThanZero(String description, ULong mustBeGreaterThanZero) throws BusinessException {
    require(description, "must be greater than zero", mustBeGreaterThanZero.compareTo(ULong.valueOf(0)) == 1);
  }

  /**
   Require user has admin access

   @param access control
   @throws BusinessException if not admin
   */
  void requireTopLevel(Access access) throws BusinessException {
    require("top-level access", access.isTopLevel());
  }

  /**
   Require user has admin access

   @param access control
   @throws BusinessException if not admin
   */
  void requireRole(String message, Access access, String... matchAnyOfRoles) throws BusinessException {
    require(message, access.isTopLevel() || access.matchAnyOf(matchAnyOfRoles));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws BusinessException if not true
   */
  public void require(String name, Boolean mustBeTrue) throws BusinessException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param description name of condition (for error message)
   @param mustBeTrue  to require true
   @param condition   to append
   @throws BusinessException if not true
   */
  public void require(String description, String condition, Boolean mustBeTrue) throws BusinessException {
    if (!mustBeTrue) {
      throw new BusinessException(description + " " + condition);
    }
  }

  /**
   Prevent a condition

   @param name        name of condition to prevent (for error message)
   @param mustBeFalse to prevent
   @throws BusinessException if not false
   */
  private void prevent(String name, Boolean mustBeFalse) throws BusinessException {
    if (mustBeFalse) {
      throw new BusinessException(name + " not allowed");
    }
  }

  /**
   Check if array contains an element

   @param findElement to check for
   @param elements    in which to check
   @return true if found
   */
  private Boolean arrayContains(String findElement, String[] elements) {
    if (findElement != null) {
      for (String element : elements) {
        if (element.equals(findElement)) {
          return true;
        }
      }
    }
    return false;
  }

}
