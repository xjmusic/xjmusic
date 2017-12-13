// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.redis.impl;

import io.xj.core.config.Config;
import io.xj.core.persistence.redis.RedisDatabaseProvider;

import com.google.common.collect.ImmutableList;

import net.greghaines.jesque.ConfigBuilder;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.client.ClientImpl;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import net.greghaines.jesque.worker.WorkerImpl;
import redis.clients.jedis.Jedis;

public class RedisDatabaseProviderImpl implements RedisDatabaseProvider {

  @Override
  public Jedis getClient() {
    return new Jedis(host(), port());
  }

  @Override
  public Client getQueueClient()  {
    return new ClientImpl(getQueueConfig());
  }

  @Override
  public Worker getQueueWorker(JobFactory jobFactory) {
    return new WorkerImpl(getQueueConfig(), ImmutableList.of(Config.workQueueName()), jobFactory);
  }

  /**
   Redis server host, from config

   @return host
   */
  private static String host() {
    return Config.dbRedisHost();
  }

  /**
   Redis namespace, from config

   @return namespace
   */
  private static String namespace() {
    return Config.dbRedisQueueNamespace();
  }

  /**
   Redis server port, from config

   @return port
   */
  private static int port(){
    return Config.dbRedisPort();
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
      .build();
  }

}
