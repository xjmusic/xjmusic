// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.db;

import io.outright.xj.core.app.exception.ConfigException;

import redis.clients.jedis.Jedis;

public interface RedisDatabaseProvider {
  Jedis getConnection() throws ConfigException;
}
