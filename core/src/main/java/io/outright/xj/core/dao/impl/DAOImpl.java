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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DAOImpl {
  private static Logger log = LoggerFactory.getLogger(DAOImpl.class);
  SQLDatabaseProvider dbProvider;

  /**
   * Execute a database CREATE operation
   *
   * @param db            context
   * @param table         to create
   * @param fieldValueMap of fields to create, and values to create with
   * @param <R>           record type dynamic
   * @return record
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
   * Execute a database UPDATE operation
   *
   * @param db            context
   * @param table         to update
   * @param fieldValueMap of fields to update, and values to update with
   * @param <R>           record type dynamic
   * @return record
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
   * Require empty ResultSet
   *
   * @param resultSet to check.
   * @throws BusinessException if result set is not empty.
   * @throws Exception         if something goes wrong.
   */
  void requireEmptyResultSet(ResultSet resultSet) throws Exception {
    try {
      if (resultSet.next()) {
        throw new BusinessException("Found " + CamelCasify.ifNeededUpper(resultSet.getMetaData().getTableName(1)));
      }
    } catch (SQLException e) {
      throw new DatabaseException("SQLException: " + e.getMessage());
    }
  }

  /**
   * Require that a record exists
   *
   * @param name   name of record (for error message)
   * @param record to require existence of
   * @throws BusinessException if not exists
   */
  void requireRecordExists(String name, Record record) throws BusinessException {
    require("existence of " + name, record != null);
  }

  /**
   * Require user has admin access
   *
   * @param access control
   * @throws BusinessException if not admin
   */
  void requireTopLevel(AccessControl access) throws BusinessException {
    require("top-level access", access.isTopLevel());
  }

  /**
   * Require that a record exists
   *
   * @param name      name of record (for error message)
   * @param condition to require truew
   * @throws BusinessException if not exists
   */
  void require(String name, Boolean condition) throws BusinessException {
    if (!condition) {
      throw new BusinessException(name + " required; not met");
    }
  }

}
