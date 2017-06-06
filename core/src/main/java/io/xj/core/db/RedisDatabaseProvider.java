// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.db;

import io.xj.core.app.exception.ConfigException;

import redis.clients.jedis.Jedis;

public interface RedisDatabaseProvider {
  Jedis getClient() throws ConfigException;
}
