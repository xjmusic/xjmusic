// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.integration;

import io.xj.core.CoreModule;
import io.xj.core.exception.ConfigException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.migration.MigrationService;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;

import com.google.inject.Guice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Objects;

public enum IntegrationTestService {
  INSTANCE;
  final Logger log = LoggerFactory.getLogger(IntegrationTestService.class);
  final SQLConnection sqlConnection;
  final Jedis redisConnection;

  IntegrationTestService() {
    log.info("Will prepare integration database.");

    // One database connection remains open until main program exit
    System.setProperty("db.mysql.database", "xj_test");
    System.setProperty("env.test", "true");
    SQLDatabaseProvider dbProvider = Guice.createInjector(new CoreModule())
      .getInstance(SQLDatabaseProvider.class);
    sqlConnection = getSqlConnection(dbProvider);
    if (Objects.isNull(sqlConnection))
      System.exit(1);

    // One Redis connection remains open until main program exit
    System.setProperty("work.queue.name", "xj_test");
    RedisDatabaseProvider redisDatabaseProvider = Guice.createInjector(new CoreModule())
      .getInstance(RedisDatabaseProvider.class);
    redisConnection = redisDatabaseProvider.getClient();
    if (Objects.isNull(redisConnection))
      System.exit(1);

    // Shut it down before program exit
    Runtime.getRuntime().addShutdownHook(new Thread(IntegrationTestService::shutdown));

    // Migrate the test database
    try {
      MigrationService.migrate(dbProvider);
    } catch (ConfigException e) {
      log.error("ConfigException", e);
      System.exit(1);
    }

    // Like a boy scout
    log.info("Did open master connection and prepare integration database.");
  }

  /**
   Get SQL database connection from provider

   @param dbProvider to get from
   @return connection
   */
  private SQLConnection getSqlConnection(SQLDatabaseProvider dbProvider) {
    try {
      return dbProvider.getConnection();
    } catch (DatabaseException e) {
      log.error("DatabaseException", e);
      return null;
    }
  }

  /**
   Get the master connection to integration database

   @return DSL Context
   */
  public static DSLContext getDb() {
    return INSTANCE.sqlConnection.getContext();
  }

  /**
   Flush entire Redis contents and database
   */
  public static void flushRedis() {
    INSTANCE.redisConnection.flushAll();
    INSTANCE.redisConnection.flushDB();
    INSTANCE.log.info("Did flush entire Redis contents and database");
  }

  /**
   Runs on program exit
   */
  private static void shutdown() {
    try {
      INSTANCE.sqlConnection.success();
      INSTANCE.redisConnection.close();
      INSTANCE.log.info("Did close master connection to integration database.");
    } catch (DatabaseException e) {
      e.printStackTrace();
    }

    System.clearProperty("work.queue.name");
  }

}
