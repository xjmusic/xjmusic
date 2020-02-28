// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.persistence;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import net.greghaines.jesque.ConfigBuilder;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.client.ClientImpl;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import net.greghaines.jesque.worker.WorkerImpl;
import redis.clients.jedis.Jedis;

class RedisDatabaseProviderImpl implements RedisDatabaseProvider {
  private static String dbRedisHost;
  private static String dbRedisQueueNamespace;
  private static int dbRedisPort;
  private static int dbRedisTimeout;
  private String workQueueName;

  @Inject
  public RedisDatabaseProviderImpl(
    Config config
  ) {
    dbRedisHost = config.getString("redis.host");
    dbRedisQueueNamespace = config.getString("redis.queueNamespace");
    dbRedisPort = config.getInt("redis.port");
    dbRedisTimeout = config.getInt("redis.timeoutSeconds");
    workQueueName = config.getString("work.queueName");
  }

  /**
   Redis server host, of config

   @return host
   */
  private static String host() {
    return dbRedisHost;
  }

  /**
   Redis namespace, of config

   @return namespace
   */
  private static String namespace() {
    return dbRedisQueueNamespace;
  }

  /**
   Redis server port, of config

   @return port
   */
  private static int port() {
    return dbRedisPort;
  }

  /**
   Redis server timeout, of config

   @return timeout
   */
  private static int timeout() {
    return dbRedisTimeout;
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
    return new WorkerImpl(getQueueConfig(), ImmutableList.of(workQueueName), jobFactory);
  }

}
