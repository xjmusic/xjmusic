// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import redis.clients.jedis.Jedis;

public interface HubKvStoreProvider {

  /**
   * Jedis all-purpose Redis client
   *
   * @return instance
   */
  Jedis getClient();
}
