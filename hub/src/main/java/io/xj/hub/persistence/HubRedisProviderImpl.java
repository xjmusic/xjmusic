// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.google.inject.Inject;
import io.xj.lib.app.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

class HubRedisProviderImpl implements HubRedisProvider {
  private static final Logger LOG = LoggerFactory.getLogger(HubRedisProviderImpl.class);
  private static String dbRedisHost;
  private static int dbRedisPort;

  @Inject
  public HubRedisProviderImpl(
    Environment env
  ) {
    dbRedisHost = env.getRedisHost();
    dbRedisPort = env.getRedisPort();
    LOG.info("Will connect to Redis host:{} port:{}", dbRedisHost, dbRedisPort);
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
