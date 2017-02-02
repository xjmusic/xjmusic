package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.util.CamelCasify;

import org.jooq.Record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DAOImpl {
  SQLDatabaseProvider dbProvider;

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
    if (record == null) {
      throw new BusinessException(name + " not found");
    }
  }

  /**
   * Require user has admin access
   *
   * @param access control
   * @throws BusinessException if not admin
   */
  void requireAdmin(AccessControl access) throws BusinessException {
    if (!access.isAdmin()) {
      throw new BusinessException("not admin");
    }
  }

}
