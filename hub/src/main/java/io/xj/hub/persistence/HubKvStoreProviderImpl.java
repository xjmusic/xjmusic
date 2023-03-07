// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import io.xj.lib.app.AppEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class HubKvStoreProviderImpl implements HubKvStoreProvider {
  private static final Logger LOG = LoggerFactory.getLogger(HubKvStoreProviderImpl.class);
  private static String dbRedisHost;
  private static int dbRedisPort;

  public HubKvStoreProviderImpl(
    AppEnvironment env
  ) {
    dbRedisHost = env.getRedisHost();
    dbRedisPort = env.getRedisPort();
    LOG.info("Will connect to Redis host:{} port:{}", dbRedisHost, dbRedisPort);
  }

  /**
   * Redis server host, of config
   *
   * @return host
   */
  private static String host() {
    return dbRedisHost;
  }

  /**
   * Redis server port, of config
   *
   * @return port
   */
  private static int port() {
    return dbRedisPort;
  }


  @Override
  public Jedis getClient() {
    return new Jedis(host(), port());
  }

}
