// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.work;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ChainDAO;
import io.xj.service.hub.dao.PlatformMessageDAO;
import io.xj.service.hub.entity.MessageType;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.PlatformMessage;
import io.xj.service.hub.model.Work;
import io.xj.service.hub.model.WorkState;
import io.xj.service.hub.model.WorkType;
import io.xj.service.hub.persistence.RedisDatabaseProvider;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class WorkManagerImpl implements WorkManager {
  private static final Logger log = LoggerFactory.getLogger(WorkManagerImpl.class);
  private static final Integer MILLIS_PER_SECOND = 1000;
  private static final String KEY_JESQUE_CLASS = "class";
  private static final String KEY_JESQUE_VARS = "vars";
  private static String dbRedisQueueNamespace;
  private static String workQueueName;
  private final PlatformMessageDAO platformMessageDAO;
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final ChainDAO chainDAO;
  private int workChainDelayRecurSeconds;
  private int workChainEraseRecurSeconds;
  private int workChainRecurSeconds;

  @Inject
  WorkManagerImpl(
    ChainDAO chainDAO,
    PlatformMessageDAO platformMessageDAO,
    RedisDatabaseProvider redisDatabaseProvider,
    Config config
  ) {
    this.chainDAO = chainDAO;
    this.platformMessageDAO = platformMessageDAO;
    this.redisDatabaseProvider = redisDatabaseProvider;

    dbRedisQueueNamespace = config.getString("redis.queueNamespace");
    workQueueName = config.getString("work.queueName");
    workChainDelayRecurSeconds = config.getInt("work.chainDelayRecurSeconds");
    workChainEraseRecurSeconds = config.getInt("work.chainEraseRecurSeconds");
    workChainRecurSeconds = config.getInt("work.chainRecurSeconds");
  }

  /**
   Compute the key for the Redis work queue

   @return computed key
   */
  private static String computeRedisWorkQueueKey() {
    return String.format("%s:queue:%s", dbRedisQueueNamespace, workQueueName);
  }

  /**
   build a Work of properties

   @param type     of work
   @param state    of work
   @param targetId of work
   @return new work
   */
  private static Work buildWork(WorkType type, WorkState state, UUID targetId) {
    return (Work) new Work()
      .setState(state)
      .setType(type)
      .setTargetId(targetId)
      .setId(UUID.randomUUID());
  }

  /**
   Build a job for enqueuing

   @param type     of job
   @param targetId for job
   @return new job
   */
  private static Job buildJob(WorkType type, UUID targetId) {
    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, targetId.toString());
    return new Job(type.toString(), vars);
  }

  @Override
  public void startChainFabrication(UUID chainId) {
    startRecurringJob(WorkType.ChainFabricate, workChainDelayRecurSeconds, workChainRecurSeconds, chainId);
  }

  @Override
  public void stopChainFabrication(UUID chainId) {
    removeRecurringJob(WorkType.ChainFabricate, chainId);
  }

  @Override
  public void scheduleSegmentFabricate(Integer delaySeconds, UUID segmentId) {
    scheduleJob(WorkType.SegmentFabricate, delaySeconds, segmentId);
  }

  @Override
  public void startChainErase(UUID chainId) {
    startRecurringJob(WorkType.ChainErase, workChainDelayRecurSeconds, workChainEraseRecurSeconds, chainId);
  }

  @Override
  public void stopChainErase(UUID chainId) {
    removeRecurringWork(buildJob(WorkType.ChainErase, chainId));
  }

  @Override
  public Worker getWorker(JobFactory jobFactory) {
    return redisDatabaseProvider.getQueueWorker(jobFactory);
  }

  @Override
  public Collection<Work> readAllWork() throws Exception {
    Map<String, Work> workMap = Maps.newHashMap();

    // Add Expected Work: Chain in 'Erase' state
    chainDAO.readAllInState(Access.internal(), ChainState.Erase).stream().map(record -> buildWork(WorkType.ChainErase, WorkState.Expected, record.getId())).forEach(work -> workMap.put(work.getTargetKey(), work));

    // Add Expected Work: Chain in 'Fabricate' state
    chainDAO.readAllInState(Access.internal(), ChainState.Fabricate).stream().map(record -> buildWork(WorkType.ChainFabricate, WorkState.Expected, record.getId())).forEach(work -> workMap.put(work.getTargetKey(), work));

    // Overwrite and Add all Queued Work
    Jedis client = redisDatabaseProvider.getClient();
    Set<String> queuedWork = client.zrange(computeRedisWorkQueueKey(), 0L, -1L);
    queuedWork.forEach((value) -> {
      try {
        JSONObject json = new JSONObject(value);
        Work work = buildWork(
          WorkType.valueOf(json.getString(KEY_JESQUE_CLASS)),
          WorkState.Queued,
          UUID.fromString(json.getJSONObject(KEY_JESQUE_VARS).getString(Work.KEY_TARGET_ID)));
        workMap.put(work.getTargetKey(), work);
      } catch (Exception e) {
        log.error("Failed to parse redis job create value {}", value, e);
      }
    });
    client.close();

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
          case ChainFabricate:
          case SegmentFabricate:
            try {
              reinstatedWork.add(reinstate(work));
            } catch (Exception e) {
              instantiationFailure(work, e);
            }
            break;

          default:
            // does not warrant job creation
            break;
        }
      }
    });

    return reinstatedWork;
  }

  @Override
  public Boolean isExistingWork(WorkState state, WorkType type, UUID targetId) throws Exception {
    for (Work work : readAllWork()) {
      if (work.getState() == state &&
        work.getType() == type &&
        Objects.equals(work.getTargetId(), targetId)) {
        return true;
      }
    }
    return false;
  }

  /**
   Reinstate work

   @param work to reinstate
   @return reinstated work
   */
  private Work reinstate(Work work) throws Exception {
    startRecurringJob(work.getType(), workChainDelayRecurSeconds, workChainEraseRecurSeconds, work.getTargetId());
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
   Start a recurring Job

   @param workType     type of job
   @param delaySeconds to wait # seconds
   @param recurSeconds to repeat every # seconds
   @param entityId     of entity
   */
  private void startRecurringJob(WorkType workType, Integer delaySeconds, Integer recurSeconds, UUID entityId) {
    log.info("Start recurring {} job, delaySeconds:{}, recurSeconds:{}, entityId:{}", workType, delaySeconds, recurSeconds, entityId);
    enqueueRecurringWork(buildJob(workType, entityId), delaySeconds, recurSeconds);
  }

  /**
   Stop a recurring Job

   @param workType type of job
   @param entityId of entity
   */
  private void removeRecurringJob(WorkType workType, UUID entityId) {
    log.info("Remove recurring {} job, entityId:{}", workType, entityId);
    removeRecurringWork(buildJob(workType, entityId));
  }

  /**
   Schedule a Job

   @param workType     type of job
   @param delaySeconds to wait # seconds
   @param entityId     of entity
   */
  private void scheduleJob(WorkType workType, Integer delaySeconds, UUID entityId) {
    log.info("Schedule {} job, delaySeconds:{}, entityId:{}", workType, delaySeconds, entityId);
    enqueueDelayedWork(buildJob(workType, entityId), delaySeconds);
  }

  /**
   Delayed-Enqueue work

   @param job          to enqueue
   @param delaySeconds timestamp when the job will run
   */
  private void enqueueDelayedWork(Job job, long delaySeconds) {
    Client client = getQueueClient();
    client.delayedEnqueue(workQueueName, job, System.currentTimeMillis() + (delaySeconds * MILLIS_PER_SECOND));
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
    client.recurringEnqueue(workQueueName, job, System.currentTimeMillis() + (delaySeconds * MILLIS_PER_SECOND), recurSeconds * MILLIS_PER_SECOND);
    client.end();
  }

  /**
   Remove Recurring-queued work

   @param job to remove of queue
   */
  private void removeRecurringWork(Job job) {
    Client client = getQueueClient();
    client.removeRecurringEnqueue(workQueueName, job);
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
