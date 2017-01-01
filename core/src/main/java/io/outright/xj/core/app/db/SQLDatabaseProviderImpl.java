// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.db;

import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLDatabaseProviderImpl implements SQLDatabaseProvider {
  private static Logger log = LoggerFactory.getLogger(SQLDatabaseProviderImpl.class);

  private final String url = "jdbc:mysql://" + Config.dbMysqlHost()
    + ":" + Config.dbMysqlPort()
    + "/" + Config.dbMysqlDatabase()
    + "?useSSL=false";
  private final String user = Config.dbMysqlUser();
  private final String pass = Config.dbMysqlPass();

  @Override
  public Connection getConnectionTransaction() throws ConfigException {
    try {
      Connection connection = DriverManager.getConnection(url, user, pass);
      connection.setAutoCommit(false);
      return connection;
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new ConfigException(e.getMessage());
    }
  }

  @Override
  public void commitAndClose(Connection conn) throws DatabaseException {
    try {
      conn.commit();
      conn.close();
    } catch (Exception eCommitClose) {
      try {
        conn.rollback();
        conn.close();
        throw databaseFailure(
          "to commit and close database transaction (" +eCommitClose.toString()+"); " +
          "rolled back and closed OK");
      } catch (Exception eRollbackClose) {
        try {
          conn.close();
          throw databaseFailure(
            "to commit database transaction (" +eCommitClose.toString()+"), " +
            "to rollback and close (" + eRollbackClose.toString() + "); " +
            "closed OK");
        } catch (Exception eClose) {
          throw databaseFailure(
            "to commit database transaction (" +eCommitClose.toString()+", " +
            "to rollback and close (" + eRollbackClose.toString() + "), " +
            "to close ("+ eClose.toString() + ")");
        }
      }
    }
  }

  @Override
  public void rollbackAndClose(Connection conn) throws DatabaseException {
    try {
      conn.rollback();
      conn.close();
    } catch (SQLException eRollbackClose) {
      try {
        conn.close();
        throw databaseFailure(
          "to rollback and close (" + eRollbackClose.toString() + "); " +
          "closed OK");
      } catch (SQLException eClose) {
        throw databaseFailure(
          "to rollback and close (" + eRollbackClose.toString() + "), " +
          "to close ("+ eClose.toString() + ")");
      }
    }
  }

  @Override
  public String getUrl() throws ConfigException {
    return url;
  }

  @Override
  public String getUser() throws ConfigException {
    return user;
  }

  @Override
  public String getPass() throws ConfigException {
    return pass;
  }

  /**
   * All Database failure uses this for central logging and exception
   * @param toDoSomething that failed "to do something"
   * @return DatabaseException
   */
  private DatabaseException databaseFailure(String toDoSomething) {
    log.error("Failure " + toDoSomething);
    return new DatabaseException("Failure " + toDoSomething);
  }

}
