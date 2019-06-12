// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.redis.impl;

import com.google.common.collect.ImmutableList;
import io.xj.core.config.Config;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import net.greghaines.jesque.ConfigBuilder;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.client.ClientImpl;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import net.greghaines.jesque.worker.WorkerImpl;
import redis.clients.jedis.Jedis;

public class RedisDatabaseProviderImpl implements RedisDatabaseProvider {
/*
  private final JedisPool jedisPool;
  private final Client queueClient;
*/

  public RedisDatabaseProviderImpl() {
/*
FUTURE: use Jedis Pool and Client Pool
    jedisPool = new JedisPool(host(), port());
    queueClient = new ClientPoolImpl(getQueueConfig(), jedisPool);
*/
  }

  /**
   Redis server host, from config

   @return host
   */
  private static String host() {
    return Config.getDbRedisHost();
  }

  /**
   Redis namespace, from config

   @return namespace
   */
  private static String namespace() {
    return Config.getDbRedisQueueNamespace();
  }

  /**
   Redis server port, from config

   @return port
   */
  private static int port() {
    return Config.getDbRedisPort();
  }

  /**
   Redis server timeout, from config

   @return timeout
   */
  private static int timeout() {
    return Config.getDbRedisTimeout();
  }

  /**
   Jesque work queue configuration

   @return config
   */
  private static net.greghaines.jesque.Config getQueueConfig() {
    return new ConfigBuilder()
      .withHost(host())
      .withPort(port())
      .withNamespace(namespace())
      .withTimeout(timeout())
      .build();
  }

  @Override
  public Jedis getClient() {
    return new Jedis(host(), port());
  }

  @Override
  public Client getQueueClient() {
    return new ClientImpl(getQueueConfig());
  }

  @Override
  public Worker getQueueWorker(JobFactory jobFactory) {
    return new WorkerImpl(getQueueConfig(), ImmutableList.of(Config.getWorkQueueName()), jobFactory);
  }

}
