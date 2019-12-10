// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence;

import com.zaxxer.hikari.HikariDataSource;
import io.xj.core.exception.CoreException;
import org.jooq.DSLContext;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import java.sql.Connection;

public interface SQLDatabaseProvider {

  /**
   Get a DSL from the DataSource

   @return DSL from the DataSource
   */
  DSLContext getDSL();

  /**
   Get the DataSource

   @return DataSource
   */
  HikariDataSource getDataSource();

  /**
   * Shutdown Connection Pool
   */
  void shutdown();
}
