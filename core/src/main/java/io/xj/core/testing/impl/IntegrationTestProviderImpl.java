//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.testing.impl;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.migration.Migration;
import io.xj.core.testing.IntegrationTestProvider;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Objects;

public class IntegrationTestProviderImpl implements IntegrationTestProvider {
  final Logger log = LoggerFactory.getLogger(IntegrationTestProviderImpl.class);
  final Jedis redisConnection;
  SQLConnection sqlConnection;

  @Inject
  IntegrationTestProviderImpl(
    SQLDatabaseProvider sqlDatabaseProvider
  ) {
    Injector injector = Guice.createInjector(new CoreModule());
    log.info("Will prepare integration database.");

    // One database connection remains open until main program exit
    System.setProperty("db.mysql.database", "xj_test");
    System.setProperty("env.test", "true");
    try {
      sqlConnection = sqlDatabaseProvider.getConnection();
    } catch (CoreException e) {
      log.error("Failed to get SQL connection", e);
      System.exit(1);
    }

    // One Redis connection remains open until main program exit
    System.setProperty("work.queue.name", "xj_test");
    RedisDatabaseProvider redisDatabaseProvider = injector.getInstance(RedisDatabaseProvider.class);
    redisConnection = redisDatabaseProvider.getClient();
    if (Objects.isNull(redisConnection)) {
      log.error("Failed to get Redis connection");
      System.exit(1);
    }

    // Migrate the test database; it turns out this consumes negligible overhead
    try {
      injector.getInstance(Migration.class).migrate();
    } catch (CoreException e) {
      log.error("CoreException", e);
      System.exit(1);
    }

    // Like a boy scout
    log.info("Did open master connection and prepare integration database.");
  }

  @Override
  public void shutdown() {
    try {
      sqlConnection.success();
    } catch (CoreException e) {
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
    return sqlConnection.getContext();
  }
}
