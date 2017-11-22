// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.work.impl;

import org.jooq.types.ULong;

import com.google.inject.Inject;

import io.xj.core.app.config.Config;
import io.xj.core.database.redis.RedisDatabaseProvider;
import io.xj.core.model.job.JobType;
import io.xj.core.work.WorkManager;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkManagerImpl implements WorkManager {
  private static Logger log = LoggerFactory.getLogger(WorkManagerImpl.class);
  private static final Integer MILLIS_PER_SECOND = 1000;
  private final RedisDatabaseProvider redisDatabaseProvider;

  @Inject
  WorkManagerImpl(
    RedisDatabaseProvider redisDatabaseProvider
  ) {
    this.redisDatabaseProvider = redisDatabaseProvider;
  }

  @Override
  public void startChainFabrication(ULong chainId) {
    startRecurringJob(JobType.ChainFabricate, chainId, Config.workChainDelaySeconds(), Config.workChainRecurSeconds());
  }

  @Override
  public void stopChainFabrication(ULong chainId) {
    removeRecurringJob(JobType.ChainFabricate, chainId);
  }

  @Override
  public void scheduleLinkCraft(ULong linkId, Integer delaySeconds) {
    scheduleJob(JobType.LinkCraft, linkId, delaySeconds);
  }

  @Override
  public void scheduleLinkDub(ULong linkId, Integer delaySeconds) {
    scheduleJob(JobType.LinkDub, linkId, delaySeconds);
  }

  @Override
  public void startChainDeletion(ULong chainId) {
    startRecurringJob(JobType.ChainErase, chainId, Config.workChainDelaySeconds(), Config.workChainDeleteRecurSeconds());
  }

  @Override
  public void stopChainDeletion(ULong chainId) {
    removeRecurringWork(new Job(JobType.ChainErase.toString(), chainId));
  }

  @Override
  public void doAudioDeletion(ULong audioId) {
    doJob(JobType.AudioErase, audioId);
  }

  @Override
  public Worker getWorker(JobFactory jobFactory) {
    return redisDatabaseProvider.getQueueWorker(jobFactory);
  }


  /**
   * Start a recurring Job
   *
   * @param jobType      type of job
   * @param id           of entity
   * @param delaySeconds to wait # seconds
   * @param recurSeconds to repeat every # seconds
   */
  private void startRecurringJob(JobType jobType, ULong id, Integer delaySeconds, Integer recurSeconds) {
    log.info("Start recurring job:{}, entityId:{}, delaySeconds:{}, recurSeconds:{}", jobType.toString(), id, delaySeconds, recurSeconds);
    enqueueRecurringWork(new Job(jobType.toString(), id), delaySeconds, recurSeconds);
  }

  /**
   * Stop a recurring Job
   *
   * @param jobType type of job
   * @param id      of entity
   */
  private void removeRecurringJob(JobType jobType, ULong id) {
    log.info("Remove recurring job:{}, entityId:{}", jobType.toString(), id);
    removeRecurringWork(new Job(jobType.toString(), id));
  }

  /**
   * Schedule a Job
   *
   * @param jobType      type of job
   * @param id           of entity
   * @param delaySeconds to wait # seconds
   */
  private void scheduleJob(JobType jobType, ULong id, Integer delaySeconds) {
    log.info("Schedule job:{}, entityId:{}, delaySeconds:{}", jobType.toString(), id, delaySeconds);
    enqueueDelayedWork(new Job(jobType.toString(), id), delaySeconds);
  }

  /**
   * Do a Job
   *
   * @param jobType type of job
   * @param id      of entity
   */
  private void doJob(JobType jobType, ULong id) {
    log.info("Do job:{}, entityId:{}", jobType.toString(), id);
    enqueueWork(new Job(jobType.toString(), id));
  }

  /**
   * Enqueue work
   *
   * @param job to enqueue
   */
  private void enqueueWork(Job job) {
    Client client = getQueueClient();
    client.enqueue(Config.workQueueName(), job);
    client.end();
  }

  /**
   * Delayed-Enqueue work
   *
   * @param job          to enqueue
   * @param delaySeconds timestamp when the job will run
   */
  private void enqueueDelayedWork(Job job, long delaySeconds) {
    Client client = getQueueClient();
    client.delayedEnqueue(Config.workQueueName(), job, System.currentTimeMillis() + delaySeconds * MILLIS_PER_SECOND);
    client.end();
  }

  /**
   * Recurring-Enqueue work
   *
   * @param job          to enqueue
   * @param delaySeconds timestamp when the job will run
   * @param recurSeconds in millis how often the job will run
   */
  private void enqueueRecurringWork(Job job, long delaySeconds, long recurSeconds) {
    Client client = getQueueClient();
    client.recurringEnqueue(Config.workQueueName(), job, System.currentTimeMillis() + delaySeconds * MILLIS_PER_SECOND, recurSeconds * MILLIS_PER_SECOND);
    client.end();
  }

  /**
   * Remove Recurring-queued work
   *
   * @param job to remove from queue
   */
  private void removeRecurringWork(Job job) {
    Client client = getQueueClient();
    client.removeRecurringEnqueue(Config.workQueueName(), job);
    client.end();
  }

  /**
   * Open and keep-alive a connection to the Redis server
   *
   * @return Jesque client
   */
  private Client getQueueClient() {
    return redisDatabaseProvider.getQueueClient();
  }
}
