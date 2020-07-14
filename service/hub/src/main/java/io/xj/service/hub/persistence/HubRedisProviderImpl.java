// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.persistence;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;

class HubRedisProviderImpl implements HubRedisProvider {
  private static String dbRedisHost;
  private static int dbRedisPort;

  @Inject
  public HubRedisProviderImpl(
    Config config
  ) {
    dbRedisHost = config.getString("redis.host");
    dbRedisPort = config.getInt("redis.port");
  }

  /**
   Redis server host, of config

   @return host
   */
  private static String host() {
    return dbRedisHost;
  }

  /**
   Redis server port, of config

   @return port
   */
  private static int port() {
    return dbRedisPort;
  }


  @Override
  public Jedis getClient() {
    return new Jedis(host(), port());
  }

}
