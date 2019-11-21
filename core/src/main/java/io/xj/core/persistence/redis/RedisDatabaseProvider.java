// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.redis;

import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import redis.clients.jedis.Jedis;

public interface RedisDatabaseProvider {

  /**
   Jedis all-purpose Redis client
   @return instance
   */
  Jedis getClient();

  /**
   Jesque Redis work queue client
   @return instance
   */
  Client getQueueClient();

  /**
   Jesque instantiate a worker with given a Job Factory
   @param jobFactory to instantiate worker for
   @return worker
   */
  Worker getQueueWorker(JobFactory jobFactory);
}
