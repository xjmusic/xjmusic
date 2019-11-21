//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.testing.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.core.dao.DAORecord;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.migration.Migration;
import io.xj.core.testing.IntegrationTestProvider;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.util.Objects;

@Singleton
public class IntegrationTestProviderImpl implements IntegrationTestProvider {
  final Logger log = LoggerFactory.getLogger(IntegrationTestProviderImpl.class);
  final Jedis redisConnection;
  Connection sqlConnection;

  /**
   Since this class is a singleton, the process here in its constructor
   will happen only once for a whole test suite
   */
  @Inject
  IntegrationTestProviderImpl(
    SQLDatabaseProvider sqlDatabaseProvider,
    RedisDatabaseProvider redisDatabaseProvider,
    Migration migration
  ) {
    System.setProperty("work.queue.name", "xj_test");
    System.setProperty("db.postgres.database", "xj_test");
    System.setProperty("env.test", "true");
    log.info("Will prepare integration database.");

    // Migrate the test database
    try {
      migration.migrate();
    } catch (CoreException e) {
      log.error("CoreException", e);
      System.exit(1);
    }

    // One database connection remains open until main program exit
    try {
      sqlConnection = sqlDatabaseProvider.getConnection();
    } catch (CoreException e) {
      log.error("Failed to get SQL connection", e);
      System.exit(1);
    }

    // One Redis connection remains open until main program exit
    redisConnection = redisDatabaseProvider.getClient();
    if (Objects.isNull(redisConnection)) {
      log.error("Failed to get Redis connection");
      System.exit(1);
    }

    // Prepared
    log.info("Did open master connection and prepare integration database.");
  }

  @Override
  public void shutdown() {
    try {
      sqlConnection.close();
    } catch (Exception e) {
      log.error("Failed to shutdown SQL connection", e);
    }
    redisConnection.close();
    log.info("Did close master connection to integration database.");

    System.clearProperty("work.queue.name");
  }

  @Override
  public void flushRedis() {
    redisConnection.flushDB();
    log.info("Did flush entire Redis contents and database");
  }

  @Override
  public DSLContext getDb() {
    return DAORecord.DSL(sqlConnection);
  }
}
