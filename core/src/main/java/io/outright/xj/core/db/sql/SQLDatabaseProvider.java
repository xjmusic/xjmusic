// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.db.sql;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;

public interface SQLDatabaseProvider {
  /**
   * Get a SQL Database connection, in a transaction.
   * IMPORTANT NOTE: When finished, be sure to call success() or failure()
   * @return Connection to SQL Database
   * @throws DatabaseException if the application is not configured correctly
   */
  SQLConnection getConnection() throws DatabaseException;

  /**
   * Get a SQL Database connection, potentially in a transaction.
   * IMPORTANT NOTE: When finished, be sure to call success() or failure()
   * @return Connection to SQL Database
   * @throws DatabaseException if the application is not configured correctly
   */
  SQLConnection getConnection(Boolean isTransaction) throws DatabaseException;

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
