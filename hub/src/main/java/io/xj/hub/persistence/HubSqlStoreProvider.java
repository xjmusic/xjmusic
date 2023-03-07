// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;

public interface HubSqlStoreProvider {

  /**
   * Get a DSL from the DataSource
   *
   * @return DSL from the DataSource
   */
  DSLContext getDSL();

  /**
   * Get the DataSource
   *
   * @return DataSource
   */
  HikariDataSource getDataSource();

  /**
   * Shutdown Connection Pool
   */
  void shutdown();

  /**
   * Get the schemas managed by Flyway. These schema names are case-sensitive. If not specified, Flyway uses
   * the default schema for the database connection. If <i>defaultSchemaName</i> is not specified, then the first of
   * this list also acts as default schema.
   *
   * @return database schemas
   */
  String getSchemas();
}
