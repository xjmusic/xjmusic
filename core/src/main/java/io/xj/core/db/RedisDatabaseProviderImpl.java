// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.db;

import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;

import redis.clients.jedis.Jedis;

public class RedisDatabaseProviderImpl implements RedisDatabaseProvider {
  private final Integer port = Config.dbRedisPort();
  private final String host = Config.dbRedisHost();

  @Override
  public Jedis getClient() throws ConfigException {
    return new Jedis(host, port);
  }
}
