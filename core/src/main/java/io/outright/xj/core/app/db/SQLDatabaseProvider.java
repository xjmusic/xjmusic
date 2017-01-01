// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.db;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;

import java.sql.Connection;

public interface SQLDatabaseProvider {
  /**
   * Get a SQL Database connection.
   * IMPORTANT NOTE: When finished, be sure to commitAndClose() or rollbackAndClose().
   * @return Connection to SQL Database
   * @throws ConfigException if the application is not configured correctly
   */
  Connection getConnectionTransaction() throws ConfigException;

  /**
   * Commit the SQL Database transaction and close the connection.
   * @param conn Connection to SQL Database
   * @throws DatabaseException if something goes wrong
   */
  void commitAndClose(Connection conn) throws DatabaseException;

  /**
   * Commit the SQL Database transaction and close the connection.
   * @param conn Connection to SQL Database
   * @throws DatabaseException if something goes wrong
   */
  void rollbackAndClose(Connection conn) throws DatabaseException;

  /**
   * Get the SQL Database URL
   * @return URL
   * @throws ConfigException if the application is not configured correctly
   */
  String getUrl() throws ConfigException;

  /**
   * Get the SQL Database User
   * @return User
   * @throws ConfigException if the application is not configured correctly
   */
  String getUser() throws ConfigException;

  /**
   * Get the SQL Database password
   * @return password
   * @throws ConfigException if the application is not configured correctly
   */
  String getPass() throws ConfigException;
}
