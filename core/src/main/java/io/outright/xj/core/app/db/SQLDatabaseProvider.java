// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.db;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;

import java.sql.Connection;

public interface SQLDatabaseProvider {
  Connection getConnection() throws ConfigException;
  void commitAndClose(Connection conn) throws DatabaseException;
  void rollback(Connection conn) throws DatabaseException;
  String getUrl() throws ConfigException;
  String getUser() throws ConfigException;
  String getPass() throws ConfigException;
}
