// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.db;

import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;

import redis.clients.jedis.Jedis;

public class RedisDatabaseProviderImpl implements RedisDatabaseProvider {
  private final Integer port = Config.dbRedisPort();
  private final String host = Config.dbRedisHost();

  @Override
  public Jedis getClient() throws ConfigException {
    return new Jedis(host, port);
  }
}
