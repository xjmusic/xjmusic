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
  public Connection getConnection() throws ConfigException {
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
    } catch (SQLException e) {
      log.error("Failed to commit database transaction", e);
      rollback(conn);
    }

    try {
      conn.close();
    } catch (SQLException e) {
      log.error("Failed to close database transaction", e);
      throw new DatabaseException("Failed to close database connection");
    }
  }

  @Override
  public void rollback(Connection conn) throws DatabaseException {
    try {
      conn.rollback();
      throw new DatabaseException("Failed to commit database transaction");
    } catch (SQLException f) {
      log.error("Failed to rollback database transaction", f);
      throw new DatabaseException("Failed to commit database transaction, and failed to rollback");
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


}
