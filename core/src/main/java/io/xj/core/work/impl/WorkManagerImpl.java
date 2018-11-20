// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work.impl;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkState;
import io.xj.core.model.work.WorkType;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.work.WorkManager;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkManagerImpl implements WorkManager {
  private static final Logger log = LoggerFactory.getLogger(WorkManagerImpl.class);
  private static final Integer MILLIS_PER_SECOND = 1000;
  private static final String KEY_JESQUE_CLASS = "class";
  private static final String KEY_JESQUE_VARS = "vars";
  private final PlatformMessageDAO platformMessageDAO;
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final ChainDAO chainDAO;
  private final AudioDAO audioDAO;
  private final SequenceDAO sequenceDAO;
  private final PatternDAO patternDAO;

  @Inject
  WorkManagerImpl(
    AudioDAO audioDAO,
    ChainDAO chainDAO,
    SequenceDAO sequenceDAO,
    PatternDAO patternDAO,
    PlatformMessageDAO platformMessageDAO,
    RedisDatabaseProvider redisDatabaseProvider
  ) {
    this.audioDAO = audioDAO;
    this.chainDAO = chainDAO;
    this.sequenceDAO = sequenceDAO;
    this.patternDAO = patternDAO;
    this.platformMessageDAO = platformMessageDAO;
    this.redisDatabaseProvider = redisDatabaseProvider;
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
   Build a job for enqueuing

   @param type     of job
   @param targetId for job
   @return new job
   */
  private static Job buildJob(WorkType type, BigInteger targetId) {
    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, targetId.toString());
    return new Job(type.toString(), vars);
  }

  /**
   Build a job for enqueing

   @param type     of job
   @param sourceId for job
   @param targetId for job
   @return new job
   */
  private static Job buildJob(WorkType type, BigInteger sourceId, BigInteger targetId) {
    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_SOURCE_ID, sourceId.toString());
    vars.put(Work.KEY_TARGET_ID, targetId.toString());
    return new Job(type.toString(), vars);
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
  public void scheduleSegmentFabricate(Integer delaySeconds, BigInteger segmentId) {
    scheduleJob(WorkType.SegmentFabricate, delaySeconds, segmentId);
  }

  @Override
  public void startChainErase(BigInteger chainId) {
    startRecurringJob(WorkType.ChainErase, Config.workChainDelaySeconds(), Config.workChainEraseRecurSeconds(), chainId);
  }

  @Override
  public void stopChainErase(BigInteger chainId) {
    removeRecurringWork(buildJob(WorkType.ChainErase, chainId));
  }

  @Override
  public void doSequenceErase(BigInteger sequenceId) {
    doJob(WorkType.SequenceErase, sequenceId);
  }

  @Override
  public void doPatternErase(BigInteger patternId) {
    doJob(WorkType.PatternErase, patternId);
  }

  @Override
  public void doAudioErase(BigInteger audioId) {
    doJob(WorkType.AudioErase, audioId);
  }

  @Override
  public void doInstrumentClone(BigInteger sourceId, BigInteger targetId) {
    doJob(WorkType.InstrumentClone, sourceId, targetId);
  }

  @Override
  public void doAudioClone(BigInteger sourceId, BigInteger targetId) {
    doJob(WorkType.AudioClone, sourceId, targetId);
  }

  @Override
  public void doSequenceClone(BigInteger sourceId, BigInteger targetId) {
    doJob(WorkType.SequenceClone, sourceId, targetId);
  }

  @Override
  public void doPatternClone(BigInteger sourceId, BigInteger targetId) {
    doJob(WorkType.PatternClone, sourceId, targetId);
  }

  @Override
  public Worker getWorker(JobFactory jobFactory) {
    return redisDatabaseProvider.getQueueWorker(jobFactory);
  }

  @Override
  public Collection<Work> readAllWork() throws Exception {
    Map<BigInteger, Work> workMap = audioDAO.readAllInState(Access.internal(), AudioState.Erase).stream().map(record -> buildWork(WorkType.AudioErase, WorkState.Expected, record.getId())).collect(Collectors.toMap(Entity::getId, work -> work, (a, work1) -> work1));

    // Add Expected Work: Sequence in 'Erase' state
    sequenceDAO.readAllInState(Access.internal(), SequenceState.Erase).stream().map(record -> buildWork(WorkType.SequenceErase, WorkState.Expected, record.getId())).forEach(work -> workMap.put(work.getId(), work));

    // Add Expected Work: Pattern in 'Erase' state
    patternDAO.readAllInState(Access.internal(), PatternState.Erase).stream().map(record -> buildWork(WorkType.PatternErase, WorkState.Expected, record.getId())).forEach(work -> workMap.put(work.getId(), work));

    // Add Expected Work: Chain in 'Erase' state
    chainDAO.readAllInState(Access.internal(), ChainState.Erase).stream().map(record -> buildWork(WorkType.ChainErase, WorkState.Expected, record.getId())).forEach(work -> workMap.put(work.getId(), work));

    // Add Expected Work: Chain in 'Fabricate' state
    chainDAO.readAllInState(Access.internal(), ChainState.Fabricate).stream().map(record -> buildWork(WorkType.ChainFabricate, WorkState.Expected, record.getId())).forEach(work -> workMap.put(work.getId(), work));

    // Overwrite and Add all Queued Work
    Jedis client = redisDatabaseProvider.getClient();
    Set<String> queuedWork = client.zrange(computeRedisWorkQueueKey(), 0L, -1L);
    queuedWork.forEach((value) -> {
      try {
        JSONObject json = new JSONObject(value);
        Work work = buildWork(
          WorkType.valueOf(json.getString(KEY_JESQUE_CLASS)),
          WorkState.Queued,
          json.getJSONObject(KEY_JESQUE_VARS).getBigInteger(Work.KEY_TARGET_ID));
        workMap.put(work.getId(), work);
      } catch (Exception e) {
        log.error("Failed to parse redis job from value {}", value, e);
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
          case AudioErase:
          case SequenceErase:
          case PatternErase:
          case ChainFabricate:
          case SegmentFabricate:
            try {
              reinstatedWork.add(reinstate(work));
            } catch (Exception e) {
              instantiationFailure(work, e);
            }
            break;

          case AudioClone:
          case InstrumentClone:
          case SequenceClone:
          case PatternClone:
            // does not warrant job creation
            break;
        }
      }
    });

    return reinstatedWork;
  }

  @Override
  public Boolean isExistingWork(WorkState state, WorkType type, BigInteger targetId) throws Exception {
    for (Work work : readAllWork()) {
      if (Objects.equals(work.getState(), state) &&
        Objects.equals(work.getType(), type) &&
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
    startRecurringJob(work.getType(), Config.workChainDelaySeconds(), Config.workChainEraseRecurSeconds(), work.getTargetId()
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

  /*
   Schedule a Job from one entity to another

   @param workType     type of job
   @param delaySeconds to wait # seconds
   @param fromId       entity to source values and child entities from
   @param toId         entity to clone entities onto
   *
  private void scheduleJob(WorkType workType, Integer delaySeconds, BigInteger fromId, BigInteger toId) {
    log.info("Schedule targeted {} job, delaySeconds:{}, fromId:{}, toId:{}", workType, delaySeconds, fromId, toId);
    enqueueDelayedWork(buildJob(workType, fromId, toId), delaySeconds);
  }
  */

  /**
   Do a Job from one entity to another

   @param workType type of job
   @param sourceId entity to source values and child entities from
   @param targetId entity to clone entities onto
   */
  private void doJob(WorkType workType, BigInteger sourceId, BigInteger targetId) {
    log.info("Do {} job, sourceId:{}, targetId:{}", workType, sourceId, targetId);
    enqueueWork(buildJob(workType, sourceId, targetId));
  }

  /**
   Do a Job

   @param workType type of job
   @param targetId of entity
   */
  private void doJob(WorkType workType, BigInteger targetId) {
    log.info("Do {} job, targetId:{}", workType, targetId);
    enqueueWork(buildJob(workType, targetId));
  }

  /**
   Enqueue work (delayed until zero seconds from now)

   @param job to enqueue
   */
  private void enqueueWork(Job job) {
    Client client = getQueueClient();
    client.delayedEnqueue(Config.workQueueName(), job, System.currentTimeMillis()+ Config.workEnqueueNowDelayMillis());
    client.end();
  }

  /**
   Delayed-Enqueue work

   @param job          to enqueue
   @param delaySeconds timestamp when the job will run
   */
  private void enqueueDelayedWork(Job job, long delaySeconds) {
    Client client = getQueueClient();
    client.delayedEnqueue(Config.workQueueName(), job, System.currentTimeMillis() + (delaySeconds * MILLIS_PER_SECOND));
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
    client.recurringEnqueue(Config.workQueueName(), job, System.currentTimeMillis() + (delaySeconds * MILLIS_PER_SECOND), recurSeconds * MILLIS_PER_SECOND);
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
