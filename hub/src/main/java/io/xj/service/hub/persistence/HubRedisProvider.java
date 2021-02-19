// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.persistence;

import redis.clients.jedis.Jedis;

public interface HubRedisProvider {

  /**
   Jedis all-purpose Redis client

   @return instance
   */
  Jedis getClient();
}
