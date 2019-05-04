// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app.impl;

import io.xj.core.app.Health;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import com.google.inject.Inject;

import redis.clients.jedis.Jedis;

import java.util.Objects;

import static redis.clients.jedis.Protocol.Keyword.PONG;

/**
 Implementation of application health check
 */
public class HealthImpl implements Health {
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final SQLDatabaseProvider sqlDatabaseProvider;

  @Inject
  public HealthImpl(RedisDatabaseProvider redisDatabaseProvider, SQLDatabaseProvider sqlDatabaseProvider) {
    this.redisDatabaseProvider = redisDatabaseProvider;
    this.sqlDatabaseProvider = sqlDatabaseProvider;
  }

  @Override
  public void check() throws Exception {

    // throw exception if Redis database cannot be pinged
    Jedis client = redisDatabaseProvider.getClient();
    String pingResult = client.ping();
    if (!Objects.equals(PONG.toString(), pingResult)) {
      client.close();
      throw new CoreException("Redis server ping result: " + pingResult);
    }
    client.close();

    // throw exception if SQL database cannot complete a transaction
    sqlDatabaseProvider.getConnection().success();

  }
}
