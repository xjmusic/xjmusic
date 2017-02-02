// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.db;

import io.outright.xj.core.app.exception.ConfigException;

import redis.clients.jedis.Jedis;

public interface RedisDatabaseProvider {
  Jedis getClient() throws ConfigException;
}
