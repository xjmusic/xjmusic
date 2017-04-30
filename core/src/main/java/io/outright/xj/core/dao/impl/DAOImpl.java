package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.util.CamelCasify;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.types.ULong;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class DAOImpl {
  //  private static Logger log = LoggerFactory.getLogger(DAOImpl.class);
  SQLDatabaseProvider dbProvider;

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
   Require empty ResultSet

   @param resultSet to check.
   @throws BusinessException if result set is not empty.
   @throws Exception         if something goes wrong.
   */
  void requireEmptyResultSet(ResultSet resultSet) throws Exception {
    requireEmptyResultSet("Found " + CamelCasify.ifNeededUpper(resultSet.getMetaData().getTableName(1)), resultSet);
  }

  /**
   Require empty ResultSet, else throw message

   @param resultSet to check.
   @throws BusinessException if result set is not empty.
   @throws Exception         if something goes wrong.
   */
  void requireEmptyResultSet(String message, ResultSet resultSet) throws Exception {
    try {
      if (resultSet.next()) {
        throw new BusinessException(message);
      }
    } catch (SQLException e) {
      throw new DatabaseException("SQLException: " + e.getMessage());
    }
  }

  /**
   Require that a record exists

   @param name   name of record (for error message)
   @param record to require existence of
   @throws BusinessException if not exists
   */
  void requireRecordExists(String name, Record record) throws BusinessException {
    require(name, "must exist", record != null);
  }

  /**
   Require state is in an array of states

   @param state         to check
   @param allowedStates required to be in
   @throws BusinessException if not in required states
   */
  void onlyAllowTransitions(String state, String... allowedStates) throws BusinessException {
    prevent("transition to " + state, !arrayContains(state, allowedStates));
  }

  /**
   Require user has access to account #

   @param access    control
   @param accountId to check for access to
   @throws BusinessException if not admin
   */
  void requireAccount(AccessControl access, ULong accountId) throws BusinessException {
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
   Require user has admin access

   @param access control
   @throws BusinessException if not admin
   */
  void requireTopLevel(AccessControl access) throws BusinessException {
    require("top-level access", access.isTopLevel());
  }

  /**
   Require user has admin access

   @param access control
   @throws BusinessException if not admin
   */
  void requireRole(String message, AccessControl access, String... matchAnyOfRoles) throws BusinessException {
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
