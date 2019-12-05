// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.sql;

import io.xj.core.exception.CoreException;

import javax.sql.ConnectionPoolDataSource;
import java.sql.Connection;

public interface SQLDatabaseProvider {
  /**
   Get a SQL Database connection

   @return Connection to SQL Database
   @throws CoreException if the application is not configured correctly
   */
  Connection getConnection() throws CoreException;

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
  String getPassword() throws CoreException;
}
