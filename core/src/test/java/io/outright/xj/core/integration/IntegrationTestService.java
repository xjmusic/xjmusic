// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.integration;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.migration.MigrationService;

import com.google.inject.Guice;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum IntegrationTestService {
  INSTANCE;
  private Logger log = LoggerFactory.getLogger(IntegrationTestService.class);
  private SQLConnection sqlConnection;

  IntegrationTestService() {
    log.info("Will prepare integration database.");

    // One database connection remains open until main program exit
    System.setProperty("db.mysql.database", "xj_test");
    SQLDatabaseProvider dbProvider = Guice.createInjector(new CoreModule())
      .getInstance(SQLDatabaseProvider.class);
    try {
      sqlConnection = dbProvider.getConnection();
    } catch (DatabaseException e) {
      log.error("DatabaseException: " + e);
      System.exit(1);
    }

    // Shut it down before program exit
    Runtime.getRuntime().addShutdownHook(new Thread(IntegrationTestService::shutdown));

    // Migrate the test database
    try {
      MigrationService.migrate(dbProvider);
    } catch (ConfigException e) {
      log.error("ConfigException: " + e);
      System.exit(1);
    }

    // Like a boy scout
    log.info("Did open master connection and prepare integration database.");
  }

  /**
   * Get the master connection to integration database
   *
   * @return DSL Context
   */
  public static DSLContext getDb() {
    return INSTANCE.sqlConnection.getContext();
  }

  /**
   * Runs on program exit
   */
  private static void shutdown() {
    try {
      INSTANCE.sqlConnection.success();
      INSTANCE.log.info("Did close master connection to integration database.");
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }

}
