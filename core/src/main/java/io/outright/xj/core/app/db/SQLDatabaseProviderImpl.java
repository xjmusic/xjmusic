// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.db;

import io.outright.xj.core.Xj;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLDatabaseProviderImpl implements SQLDatabaseProvider {
  private static Logger log = LoggerFactory.getLogger(SQLDatabaseProviderImpl.class);

  private final String dbSchemaName = Config.dbMysqlDatabase();
  private final String url = "jdbc:mysql://" + Config.dbMysqlHost()
    + ":" + Config.dbMysqlPort()
    + "/" + dbSchemaName
    + "?useSSL=false";
  private final String user = Config.dbMysqlUser();
  private final String pass = Config.dbMysqlPass();

  @Override
  public Connection getConnectionTransaction() throws DatabaseException {
    try {
      Connection connection = getConnection();
      connection.setAutoCommit(false);
      return connection;
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new DatabaseException("SQLException: " + e.getMessage());
    }
  }

  @Override
  public Connection getConnection() throws DatabaseException {
    try {
      return DriverManager.getConnection(url, user, pass);
    } catch (SQLException e) {
      log.error(e.getMessage(), e);
      throw new DatabaseException("SQLException: " + e.getMessage());
    }
  }

  @Override
  public void commitAndClose(Connection conn) throws DatabaseException {
    try {
      commit(conn);
      close(conn);
    } catch (Exception e) {
      close(conn);
      throw e;
    }
  }

  @Override
  public void rollbackAndClose(Connection conn) {
    try {
      conn.rollback();
      conn.close();
    } catch (SQLException eRollbackClose) {
      try {
        conn.close();
        logFailed(
          "to rollback and close (" + eRollbackClose.toString() + "); " +
          "closed OK");
      } catch (SQLException eClose) {
        logFailed(
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

  @Override
  public DSLContext getContext(Connection conn) {
    return DSL.using(conn, SQLDialect.MYSQL, getSettings());
  }

  @Override
  public void commit(Connection conn) throws DatabaseException{
    try {
      conn.commit();
    } catch (Exception eCommit) {
      try {
        conn.rollback();
        throw failureException(
          "to commit transaction (" +eCommit.toString()+"); " +
            "rolled back OK");
      } catch (Exception eRollback) {
        throw failureException(
          "to commit database transaction (" +eCommit.toString()+", " +
            "to rollback (" + eRollback.toString() + ") ");
      }
    }
  }

  @Override
  public void close(Connection conn) throws DatabaseException{
    try {
      conn.close();
    } catch (Exception eClose) {
      throw failureException(
        "to close connection (" +eClose.toString()+"); " +
          "rolled back and closed OK");
    }
  }

  /**
   * Get SQL Database jOOQ settings
   * @return jOOQ Settings
   */
  private Settings getSettings() {
    return new Settings()
      .withRenderMapping(new RenderMapping()
        .withSchemata(
          new MappedSchema().withInput(Xj.XJ.getName())
            .withOutput(dbSchemaName)
        )
      );
  }

  /**
   * All Database failure uses this for central logging and exception
   * @param toDoSomething that failed "to do something"
   * @return DatabaseException
   */
  private DatabaseException failureException(String toDoSomething) {
    logFailed(toDoSomething);
    return new DatabaseException("Failed " + toDoSomething);
  }

  /**
   * All Database failure uses this for central logging
   * @param toDoSomething that failed "to do something"
   */
  private void logFailed(String toDoSomething) {
    log.error("Failed " + toDoSomething);
  }

}
