// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.work.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.database.redis.RedisDatabaseProvider;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkState;
import io.xj.core.model.work.WorkType;
import io.xj.core.work.WorkManager;

import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class WorkManagerImpl implements WorkManager {
  private static final Logger log = LoggerFactory.getLogger(WorkManagerImpl.class);
  private static final Integer MILLIS_PER_SECOND = 1000;
  private static final String KEY_JESQUE_CLASS = "class";
  private static final String KEY_JESQUE_ARGS = "args";
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final ChainDAO chainDAO;
  private final AudioDAO audioDAO;
  private final LinkDAO linkDAO;

  @Inject
  WorkManagerImpl(
    AudioDAO audioDAO,
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    RedisDatabaseProvider redisDatabaseProvider
  ) {
    this.audioDAO = audioDAO;
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.redisDatabaseProvider = redisDatabaseProvider;
  }

  @Override
  public void startChainFabrication(ULong chainId) {
    startRecurringJob(WorkType.ChainFabricate, chainId, Config.workChainDelaySeconds(), Config.workChainRecurSeconds());
  }

  @Override
  public void stopChainFabrication(ULong chainId) {
    removeRecurringJob(WorkType.ChainFabricate, chainId);
  }

  @Override
  public void scheduleLinkCraft(ULong linkId, Integer delaySeconds) {
    scheduleJob(WorkType.LinkCraft, linkId, delaySeconds);
  }

  @Override
  public void scheduleLinkDub(ULong linkId, Integer delaySeconds) {
    scheduleJob(WorkType.LinkDub, linkId, delaySeconds);
  }

  @Override
  public void startChainDeletion(ULong chainId) {
    startRecurringJob(WorkType.ChainErase, chainId, Config.workChainDelaySeconds(), Config.workChainDeleteRecurSeconds());
  }

  @Override
  public void stopChainDeletion(ULong chainId) {
    removeRecurringWork(new Job(WorkType.ChainErase.toString(), chainId));
  }

  @Override
  public void doAudioDeletion(ULong audioId) {
    doJob(WorkType.AudioErase, audioId);
  }

  @Override
  public Worker getWorker(JobFactory jobFactory) {
    return redisDatabaseProvider.getQueueWorker(jobFactory);
  }

  @Override
  public Collection<Work> readAllWork() throws Exception {
    Map<ULong, Work> workMap = Maps.newHashMap();

    // Add Expected Work: Chain in 'Erase' state
    chainDAO.readAllInState(Access.internal(), ChainState.Erase).forEach(record -> {
      Work work = buildWork(WorkType.ChainErase, WorkState.Expected, record.getId());
      workMap.put(work.getId(), work);
    });

    // Add Expected Work: Chain in 'Fabricate' state
    chainDAO.readAllInState(Access.internal(), ChainState.Fabricate).forEach(record -> {
      Work work = buildWork(WorkType.ChainFabricate, WorkState.Expected, record.getId());
      workMap.put(work.getId(), work);
    });

    // Overwrite and Add all Queued Work
    Set<String> queuedWork = redisDatabaseProvider.getClient().zrange(computeRedisWorkQueueKey(), 0, -1);
    queuedWork.forEach((value) -> {
      try {
        JSONObject json = new JSONObject(value);
        Work work = buildWork(
          WorkType.valueOf(json.getString(KEY_JESQUE_CLASS)),
          WorkState.Queued,
          ULong.valueOf(json.getJSONArray(KEY_JESQUE_ARGS).getBigInteger(0)));
        workMap.put(work.getId(), work);
      } catch (Exception e) {
        log.error("Failed to parse redis job from value {}", value, e);
      }
    });

    Collection<Work> list = Lists.newArrayList();
    workMap.forEach((workId, work) -> list.add(work));
    return list;
  }


  private static String computeRedisWorkQueueKey() {
    return String.format("%s:queue:%s",
      Config.dbRedisQueueNamespace(),
      Config.workQueueName());
  }

  private static Work buildWork(WorkType type, WorkState state, ULong targetId) {
    Work work = new Work()
      .setState(state)
      .setType(type)
      .setTargetId(targetId);
    work.setId(computeWorkId(type, targetId));
    return work;
  }

  /**
   Uses the work type ordinal as an integer prefix
   to prevent collisions between types
   otherwise determined by entity id

   @param type of work
   @param id   of target
   @return type-unique work id
   */
  private static BigInteger computeWorkId(WorkType type, ULong id) {
    return new BigInteger(String.format("%d%s", type.ordinal() + 1, id));
  }

  /**
   Start a recurring Job

   @param workType     type of job
   @param id           of entity
   @param delaySeconds to wait # seconds
   @param recurSeconds to repeat every # seconds
   */
  private void startRecurringJob(WorkType workType, ULong id, Integer delaySeconds, Integer recurSeconds) {
    log.info("Start recurring job:{}, entityId:{}, delaySeconds:{}, recurSeconds:{}", workType.toString(), id, delaySeconds, recurSeconds);
    enqueueRecurringWork(new Job(workType.toString(), id), delaySeconds, recurSeconds);
  }

  /**
   Stop a recurring Job

   @param workType type of job
   @param id       of entity
   */
  private void removeRecurringJob(WorkType workType, ULong id) {
    log.info("Remove recurring job:{}, entityId:{}", workType.toString(), id);
    removeRecurringWork(new Job(workType.toString(), id));
  }

  /**
   Schedule a Job

   @param workType     type of job
   @param id           of entity
   @param delaySeconds to wait # seconds
   */
  private void scheduleJob(WorkType workType, ULong id, Integer delaySeconds) {
    log.info("Schedule job:{}, entityId:{}, delaySeconds:{}", workType.toString(), id, delaySeconds);
    enqueueDelayedWork(new Job(workType.toString(), id), delaySeconds);
  }

  /**
   Do a Job

   @param workType type of job
   @param id       of entity
   */
  private void doJob(WorkType workType, ULong id) {
    log.info("Do job:{}, entityId:{}", workType.toString(), id);
    enqueueWork(new Job(workType.toString(), id));
  }

  /**
   Enqueue work

   @param job to enqueue
   */
  private void enqueueWork(Job job) {
    Client client = getQueueClient();
    client.enqueue(Config.workQueueName(), job);
    client.end();
  }

  /**
   Delayed-Enqueue work

   @param job          to enqueue
   @param delaySeconds timestamp when the job will run
   */
  private void enqueueDelayedWork(Job job, long delaySeconds) {
    Client client = getQueueClient();
    client.delayedEnqueue(Config.workQueueName(), job, System.currentTimeMillis() + delaySeconds * MILLIS_PER_SECOND);
    client.end();
  }

  /**
   Recurring-Enqueue work

   @param job          to enqueue
   @param delaySeconds timestamp when the job will run
   @param recurSeconds in millis how often the job will run
   */
  private void enqueueRecurringWork(Job job, long delaySeconds, long recurSeconds) {
    Client client = getQueueClient();
    client.recurringEnqueue(Config.workQueueName(), job, System.currentTimeMillis() + delaySeconds * MILLIS_PER_SECOND, recurSeconds * MILLIS_PER_SECOND);
    client.end();
  }

  /**
   Remove Recurring-queued work

   @param job to remove from queue
   */
  private void removeRecurringWork(Job job) {
    Client client = getQueueClient();
    client.removeRecurringEnqueue(Config.workQueueName(), job);
    client.end();
  }

  /**
   Open and keep-alive a connection to the Redis server

   @return Jesque client
   */
  private Client getQueueClient() {
    return redisDatabaseProvider.getQueueClient();
  }
}
