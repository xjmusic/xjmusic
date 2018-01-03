// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkState;
import io.xj.core.model.work.WorkType;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.work.WorkManager;

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
  private final PlatformMessageDAO platformMessageDAO;
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final ChainDAO chainDAO;
  private final AudioDAO audioDAO;

  @Inject
  WorkManagerImpl(
    ChainDAO chainDAO,
    PlatformMessageDAO platformMessageDAO,
    RedisDatabaseProvider redisDatabaseProvider,
    AudioDAO audioDAO
  ) {
    this.chainDAO = chainDAO;
    this.platformMessageDAO = platformMessageDAO;
    this.redisDatabaseProvider = redisDatabaseProvider;
    this.audioDAO = audioDAO;
  }

  @Override
  public void startChainFabrication(BigInteger chainId) {
    startRecurringJob(WorkType.ChainFabricate, Config.workChainDelaySeconds(), Config.workChainRecurSeconds(), chainId);
  }

  @Override
  public void stopChainFabrication(BigInteger chainId) {
    removeRecurringJob(WorkType.ChainFabricate, chainId);
  }

  @Override
  public void scheduleLinkFabricate(Integer delaySeconds, BigInteger linkId) {
    scheduleJob(WorkType.LinkFabricate, delaySeconds, linkId);
  }

  @Override
  public void startChainErase(BigInteger chainId) {
    startRecurringJob(WorkType.ChainErase, Config.workChainDelaySeconds(), Config.workChainDeleteRecurSeconds(), chainId);
  }

  @Override
  public void stopChainErase(BigInteger chainId) {
    removeRecurringWork(buildJob(WorkType.ChainErase, chainId));
  }

  @Override
  public void startAudioErase(BigInteger audioId) {
    doJob(WorkType.AudioErase, audioId);
  }

  @Override
  public void stopAudioErase(BigInteger audioId) {
    removeRecurringWork(buildJob(WorkType.AudioErase, audioId));
  }

  @Override
  public void scheduleInstrumentClone(Integer delaySeconds, BigInteger fromId, BigInteger toId) {
    scheduleJob(WorkType.InstrumentClone, delaySeconds, fromId, toId);
  }

  @Override
  public void scheduleAudioClone(Integer delaySeconds, BigInteger fromId, BigInteger toId) {
    scheduleJob(WorkType.AudioClone, delaySeconds, fromId, toId);
  }

  @Override
  public void schedulePatternClone(Integer delaySeconds, BigInteger fromId, BigInteger toId) {
    scheduleJob(WorkType.PatternClone, delaySeconds, fromId, toId);
  }

  @Override
  public void schedulePhaseClone(Integer delaySeconds, BigInteger fromId, BigInteger toId) {
    scheduleJob(WorkType.PhaseClone, delaySeconds, fromId, toId);
  }

  @Override
  public Worker getWorker(JobFactory jobFactory) {
    return redisDatabaseProvider.getQueueWorker(jobFactory);
  }

  @Override
  public Collection<Work> readAllWork() throws Exception {
    Map<BigInteger, Work> workMap = Maps.newHashMap();

    // Add Expected Work: Audio in 'Erase' state
    audioDAO.readAllInState(Access.internal(), AudioState.Erase).forEach(record -> {
      Work work = buildWork(WorkType.AudioErase, WorkState.Expected, record.getId());
      workMap.put(work.getId(), work);
    });

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
          json.getJSONArray(KEY_JESQUE_ARGS).getBigInteger(0));
        workMap.put(work.getId(), work);
      } catch (Exception e) {
        log.error("Failed to parse redis job from value {}", value, e);
      }
    });

    Collection<Work> allWork = Lists.newArrayList();
    workMap.forEach((workId, work) -> allWork.add(work));
    return allWork;
  }

  @Override
  public Collection<Work> reinstateAllWork() throws Exception {
    Collection<Work> reinstatedWork = Lists.newArrayList();
    Collection<Work> allWork = readAllWork();

    allWork.forEach((work) -> {
      if (WorkState.Expected == work.getState()) {
        switch (work.getType()) {

          case ChainErase:
          case AudioErase:
          case ChainFabricate:
            try {
              reinstatedWork.add(reinstate(work));
            } catch (Exception e) {
              instantiationFailure(work, e);
            }
            break;

          case AudioClone:
          case InstrumentClone:
          case PatternClone:
          case PhaseClone:
            // does not warrant job creation
            break;
        }
      }
    });

    return reinstatedWork;
  }

  /**
   Reinstate work

   @param work to reinstate
   @return reinstated work
   */
  private Work reinstate(Work work) throws Exception {
    startRecurringJob(work.getType(), Config.workChainDelaySeconds(), Config.workChainDeleteRecurSeconds(), work.getTargetId()
    );
    work.setState(WorkState.Queued);
    platformMessageDAO.create(Access.internal(),
      new PlatformMessage()
        .setType(MessageType.Warning.toString())
        .setBody(String.format("Reinstated %s", work)));
    log.warn("Reinstated {}", work);
    return work;
  }

  /**
   Report an instantiation failure

   @param work that failed to instantiate
   @param e    cause of failure
   */
  private void instantiationFailure(Work work, Exception e) {
    try {
      platformMessageDAO.create(Access.internal(),
        new PlatformMessage()
          .setType(MessageType.Error.toString())
          .setBody(String.format("Failed to instantiate %s because %s", work, e)));
    } catch (Exception e1) {
      log.error("Failed to instantiate {} and failed to report platform message {} {}", work, e, e1);
    }
    log.error("Failed to instantiate {}", work, e);
  }

  /**
   Compute the key for the Redis work queue

   @return computed key
   */
  private static String computeRedisWorkQueueKey() {
    return String.format("%s:queue:%s",
      Config.dbRedisQueueNamespace(),
      Config.workQueueName());
  }

  /**
   build a Work from properties

   @param type     of work
   @param state    of work
   @param targetId of work
   @return new work
   */
  private static Work buildWork(WorkType type, WorkState state, BigInteger targetId) {
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
  private static BigInteger computeWorkId(WorkType type, BigInteger id) {
    return new BigInteger(String.format("%d0000%s", type.ordinal() + 1, id));
  }

  /**
   Start a recurring Job

   @param workType     type of job
   @param delaySeconds to wait # seconds
   @param recurSeconds to repeat every # seconds
   @param entityId     of entity
   */
  private void startRecurringJob(WorkType workType, Integer delaySeconds, Integer recurSeconds, BigInteger entityId) {
    log.info("Start recurring {} job, delaySeconds:{}, recurSeconds:{}, entityId:{}", workType, delaySeconds, recurSeconds, entityId);
    enqueueRecurringWork(buildJob(workType, entityId), delaySeconds, recurSeconds);
  }

  /**
   Stop a recurring Job

   @param workType type of job
   @param entityId of entity
   */
  private void removeRecurringJob(WorkType workType, BigInteger entityId) {
    log.info("Remove recurring {} job, entityId:{}", workType, entityId);
    removeRecurringWork(buildJob(workType, entityId));
  }

  /**
   Schedule a Job

   @param workType     type of job
   @param delaySeconds to wait # seconds
   @param entityId     of entity
   */
  private void scheduleJob(WorkType workType, Integer delaySeconds, BigInteger entityId) {
    log.info("Schedule {} job, delaySeconds:{}, entityId:{}", workType, delaySeconds, entityId);
    enqueueDelayedWork(buildJob(workType, entityId), delaySeconds);
  }

  /**
   Schedule a Job from one entity to another

   @param workType     type of job
   @param delaySeconds to wait # seconds
   @param fromId       entity to source values and child entities from
   @param toId         entity to clone entities onto
   */
  private void scheduleJob(WorkType workType, Integer delaySeconds, BigInteger fromId, BigInteger toId) {
    log.info("Schedule targeted {} job, delaySeconds:{}, fromId:{}, toId:{}", workType, delaySeconds, fromId, toId);
    enqueueDelayedWork(buildJob(workType, fromId, toId), delaySeconds);
  }

  /**
   Do a Job

   @param workType type of job
   @param id       of entity
   */
  private void doJob(WorkType workType, BigInteger id) {
    log.info("Do {} job, entityId:{}", workType, id);
    enqueueWork(buildJob(workType, id));
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
   Build a job for Jedis enqueing

   @param type of job
   @param args for job
   @return new job
   */
  private static Job buildJob(WorkType type, Object... args) {
    return new Job(type.toString(), args);
  }

  /**
   Open and keep-alive a connection to the Redis server

   @return Jesque client
   */
  private Client getQueueClient() {
    return redisDatabaseProvider.getQueueClient();
  }
}
