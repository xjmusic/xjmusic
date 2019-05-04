// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.sql;

import io.xj.core.exception.CoreException;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.impl.SQLConnection;

public interface SQLDatabaseProvider {
  /**
   Get a SQL Database connection, in a transaction.
   IMPORTANT NOTE: When finished, be sure to call success() or failure()

   @return Connection to SQL Database
   @throws CoreException if the application is not configured correctly
   */
  SQLConnection getConnection() throws CoreException;

  /**
   Get a SQL Database connection, potentially in a transaction.
   IMPORTANT NOTE: When finished, be sure to call success() or failure()

   @param isTransaction if it's a transaction
   @return Connection to SQL Database
   @throws CoreException if the application is not configured correctly
   */
  SQLConnection getConnection(Boolean isTransaction) throws CoreException;

  /**
   Get the SQL Database URL

   @return URL
   @throws CoreException if the application is not configured correctly
   */
  String getUrl() throws CoreException;

  /**
   Get the SQL Database User

   @return User
   @throws CoreException if the application is not configured correctly
   */
  String getUser() throws CoreException;

  /**
   Get the SQL Database password

   @return password
   @throws CoreException if the application is not configured correctly
   */
  String getPass() throws CoreException;
}
