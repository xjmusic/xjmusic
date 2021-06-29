// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import datadog.trace.api.Trace;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.SegmentMessage;
import io.xj.lib.entity.Entities;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.dub.DubFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.persistence.NexusEntityStore;
import org.glassfish.jersey.internal.guava.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 The Lab Nexus Distributed Work Manager (Implementation)
 <p>
 https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 */
@Singleton
public class NexusWorkImpl implements NexusWork {
  private static final Logger LOG = LoggerFactory.getLogger(NexusWorkImpl.class);
  private Chain chain;
  private Fabricator fabricator;
  private Segment segment;
  private final ChainDAO chainDAO;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final FabricatorFactory fabricatorFactory;
  private final HubClientAccess access = HubClientAccess.internal();
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final SegmentDAO segmentDAO;
  private final TelemetryProvider telemetryProvider;
  private final boolean medicEnabled;
  private final int bufferPreviewSeconds;
  private final int bufferProductionSeconds;
  private final int eraseSegmentsOlderThanSeconds;
  private final int reviveChainFabricatedBehindSeconds;
  private final int reviveChainProductionGraceSeconds;
  private static final String DEFAULT_NAME_PREVIEW = "preview";
  private static final String DEFAULT_NAME_PRODUCTION = "production";
  private static final String METRIC_CHAIN_FORMAT = "chain.%s.%s";
  private static final String METRIC_FABRICATED_AHEAD_SECONDS = "fabricated_ahead_seconds";
  private static final String METRIC_SEGMENT_CREATED = "segment_created";
  private static final float MILLI = 1000;
  private static final float MILLIS_PER_SECOND = MILLI;
  private static final float NANOS_PER_SECOND = MILLI * MILLI * MILLI;
  private final ScheduledExecutorService scheduler;
  private ScheduledFuture<?> schedule;

  // Name pool threads
  private final int scheduleDelayMillis;

  @Inject
  public NexusWorkImpl(
    ChainDAO chainDAO,
    Config config,
    CraftFactory craftFactory,
    DubFactory dubFactory,
    FabricatorFactory fabricatorFactory,
    NexusEntityStore store,
    NotificationProvider notification,
    SegmentDAO segmentDAO,
    TelemetryProvider telemetryProvider
  ) {
    this.chainDAO = chainDAO;
    this.craftFactory = craftFactory;
    this.dubFactory = dubFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.notification = notification;
    this.segmentDAO = segmentDAO;
    this.store = store;
    this.telemetryProvider = telemetryProvider;

    bufferPreviewSeconds = config.getInt("work.bufferPreviewSeconds");
    bufferProductionSeconds = config.getInt("work.bufferProductionSeconds");
    eraseSegmentsOlderThanSeconds = config.getInt("work.eraseSegmentsOlderThanSeconds");
    medicEnabled = config.getBoolean("fabrication.medicEnabled");
    reviveChainFabricatedBehindSeconds = config.getInt("fabrication.reviveChainFabricatedBehindSeconds");
    reviveChainProductionGraceSeconds = config.getInt("fabrication.reviveChainProductionGraceSeconds");

    scheduleDelayMillis = config.getInt("work.scheduleDelayMillis");
    int workConcurrency = config.getInt("work.concurrency");

    scheduler = Executors.newScheduledThreadPool(workConcurrency, new ThreadFactoryBuilder()
      .setNameFormat(NexusWork.class.getSimpleName() + "-%d")
      .setDaemon(true)
      .build());

    LOG.debug("Instantiated OK");
  }

  @Override
  public void start() {
    schedule = scheduler.scheduleWithFixedDelay(this, scheduleDelayMillis, scheduleDelayMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public void finish() {
    if (Objects.nonNull(schedule)) schedule.cancel(false);
    scheduler.shutdown();
    try {
      if (scheduler.awaitTermination(1, TimeUnit.MINUTES))
        LOG.debug("Executor service did terminate OK");
      else
        LOG.error("Executor service failed to terminate!");
    } catch (InterruptedException e) {
      LOG.error("Timout waiting to for termination of executor service!", e);
    }
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/boss", operationName = "run")
  public void run() {
    doFabrication();
    doMedic();
    doJanitor();
  }

  /**
   Do fabrication
   */
  private void doFabrication() {
    // Get active chain IDs
    Collection<String> activeIds;
    try {
      activeIds = getActiveChainIds();
    } catch (DAOFatalException | DAOPrivilegeException e) {
      didFailWhile("Getting list of active chain IDs", e);
      return;
    }

    // Fabricate all active chains
    activeIds.forEach(this::fabricateChain);
  }

  /**
   Do medic
   */
  private void doMedic() {
    // Medic
    if (medicEnabled) try {
      long t = Instant.now().toEpochMilli();
      checkAndReviveAll();
      LOG.debug("Medic did run in {}ms OK", Instant.now().toEpochMilli() - t);
    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException e) {
      didFailWhile("Medic checking & reviving all", e);
    }
  }


  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/janitor", operationName = "doWork")
  protected void doJanitor() {
    // Seek segments to erase
    Collection<String> segmentIdsToErase;
    try {
      segmentIdsToErase = getSegmentIdsToErase();
    } catch (NexusException e) {
      didFailWhile("Checking for segments to erase", e);
      return;
    }

    // Erase segments if necessary
    if (segmentIdsToErase.isEmpty())
      LOG.debug("Found no segments to erase");
    else
      LOG.debug("Found {} segments to erase", segmentIdsToErase.size());

    for (String segmentId : segmentIdsToErase) {
      try {
        segmentDAO.destroy(access, segmentId);
        LOG.debug("Did erase Segment[{}]", segmentId);
      } catch (DAOFatalException | DAOPrivilegeException | DAOExistenceException e) {
        LOG.warn("Error while destroying Segment[{}]", segmentId);
      }
    }

    telemetryProvider.getStatsDClient().incrementCounter("segment.erased", segmentIdsToErase.size());
  }

  /**
   Get the IDs of all Chains in the store whose state is currently in Fabricate

   @return active Chain IDS
   @throws DAOPrivilegeException on access control failure
   @throws DAOFatalException     on internal failure
   */
  private Collection<String> getActiveChainIds() throws DAOPrivilegeException, DAOFatalException {
    return chainDAO.readManyInState(access, Chain.State.Fabricate)
      .stream()
      .flatMap(Entities::flatMapIds)
      .collect(Collectors.toList());
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/chain", operationName = "doWork")
  public void fabricateChain(String chainId) {
    long startedAt = System.nanoTime();
    Chain chain;
    try {
      chain = chainDAO.readOne(access, chainId);
      if (Chain.State.Fabricate != chain.getState()) {
        LOG.error("Cannot fabricate Chain[{}] in non-Fabricate ({}) state!", chain.getId(), chain.getState());
        return;
      }
    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      LOG.error("Cannot find Chain[{}]", chainId);
      return;
    }

    try {
      int workBufferSeconds = bufferSecondsFor(chain);

      Optional<Segment> segment = chainDAO.buildNextSegmentOrCompleteTheChain(access, chain,
        Instant.now().plusSeconds(workBufferSeconds),
        Instant.now().minusSeconds(workBufferSeconds));
      if (segment.isEmpty()) return;
      Segment createdSegment = segmentDAO.create(access, segment.get());
      LOG.debug("Created Segment {}", createdSegment);
      telemetryProvider.getStatsDClient().
        incrementCounter(getChainMetricName(chain, METRIC_SEGMENT_CREATED));

      // FUTURE: fork/join thread possible for this sub-runnable of the fabrication worker
      fabricateSegment(createdSegment.getId(), chainId, chain.getType().toString());
      LOG.info("Fabricated in {}s Segment[{}] of {}-Chain[{}] offset:{}",
        (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND,
        createdSegment.getId(),
        chain.getType(),
        chain.getId(),
        createdSegment.getOffset());

      // bums
      var fabricatedAheadSeconds = computeFabricatedAheadSeconds(chain);
      telemetryProvider.getStatsDClient()
        .gauge(getChainMetricName(chain, METRIC_FABRICATED_AHEAD_SECONDS), fabricatedAheadSeconds);
      chainDAO.update(access, chain.getId(), chain.toBuilder()
        .setFabricatedAheadSeconds(fabricatedAheadSeconds)
        .build());

    } catch (DAOPrivilegeException | DAOExistenceException | DAOValidationException | DAOFatalException e) {
      var body = String.format("Failed to create Segment of Chain[%s] (%s) because %s\n\n%s",
        chain.getId(),
        chain.getType(),
        e.getMessage(),
        Text.formatStackTrace(e));

      notification.publish(body,
        String.format("%s-Chain[%s] Failure",
          chain.getType(),
          chain.getId()));

      LOG.error("Failed to created Segment in chainId={}, reason={}", chainId, e.getMessage());

      try {
        chainDAO.revive(access, chain.getId(), body);
      } catch (DAOFatalException | DAOPrivilegeException | DAOExistenceException | DAOValidationException e2) {
        LOG.error("Failed to revive chain after fatal error!", e2);
      }
    }
  }

  /**
   [#177072936] Mk1 UI each chain shows current fabrication latency

   @param chain fabricating
   */
  private float computeFabricatedAheadSeconds(Chain chain) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var lastDubbedSegment = segmentDAO.readLastDubbedSegment(access, chain.getId());
    var dubbedUntil = lastDubbedSegment.isPresent() ?
      Instant.parse(lastDubbedSegment.get().getEndAt()) :
      Instant.parse(chain.getStartAt());
    var now = Instant.now();
    return (dubbedUntil.toEpochMilli() - now.toEpochMilli()) / MILLIS_PER_SECOND;
  }

  /**
   Determine the buffer for a specified type of chain

   @param chain to get buffer for, based on its type
   @return buffer # of seconds this type of chain
   */
  private int bufferSecondsFor(Chain chain) {
    switch (chain.getType()) {
      default:
      case Production:
        return bufferProductionSeconds;
      case Preview:
        return bufferPreviewSeconds;
    }
  }

  /**
   Get the name for a given chain and metric

   @param chain      to get name for
   @param metricName to get get name for
   @return name for the given chain and metric
   */
  private String getChainMetricName(Chain chain, String metricName) {
    return String.format(METRIC_CHAIN_FORMAT, getChainName(chain), metricName);
  }

  /**
   Get the name for a given chain

   @param chain to get name for
   @return name for the given chain
   */
  private String getChainName(Chain chain) {
    return Chain.Type.Production.equals(chain.getType()) ?
      (!Strings.isNullOrEmpty(chain.getEmbedKey()) ? chain.getEmbedKey() : DEFAULT_NAME_PRODUCTION) :
      DEFAULT_NAME_PREVIEW;
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doWork")
  protected void fabricateSegment(String segmentId, String chainId, String chainType) {
    try {
      LOG.debug("[segId={}] will read Segment for fabrication", segmentId);
      segment = segmentDAO.readOne(access, segmentId);
      chain = chainDAO.readOne(access, segment.getChainId());
    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException e) {
      didFailWhile("retrieving", e, segmentId, chain.getId(), chain.getType().toString());
      return;
    }

    try {
      LOG.debug("[segId={}] will prepare fabricator", segmentId);
      fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment);
    } catch (NexusException e) {
      didFailWhile("creating fabricator", e, segmentId, chain.getId(), chain.getType().toString());
      return;
    }

    try {
      LOG.debug("[segId={}] will do craft work", segmentId);
      doCraftWork(segmentId);
    } catch (Exception e) {
      didFailWhile("doing Craft work", e, segmentId, chain.getId(), chain.getType().toString());
      revert(segmentId, chainId, chainType);
      return;
    }

    try {
      doDubWork(segmentId);
    } catch (Exception e) {
      didFailWhile("doing Dub work", e, segmentId, chain.getId(), chain.getType().toString());
      return;
    }

    try {
      finishWork(segmentId);
    } catch (Exception e) {
      didFailWhile("finishing work", e, segmentId, chain.getId(), chain.getType().toString());
    }
  }

  /**
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   [#171553408] Remove all Queue mechanics in favor of a cycle happening in Main class for as long as the application is alive, that does nothing but search for active chains, search for segments that need work, and work on them. Zero need for a work queue-- that's what the Chain-Segment state machine is!@param segmentId
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "revert")
  private void revert(String segmentId, String chainId, String chainType) {
    try {
      updateSegmentState(fabricator.getSegment().getState(), Segment.State.Planned, segmentId);
      segmentDAO.revert(access, segment.getId());
    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException | NexusException e) {
      didFailWhile("reverting and re-queueing segment", e, segmentId, chainId, chainType);
    }
  }

  /**
   Finish work on Segment@param segmentId
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "finishWork")
  private void finishWork(String segmentId) throws NexusException {
    updateSegmentState(Segment.State.Dubbing, Segment.State.Dubbed, segmentId);
    LOG.debug("[segId={}] Worked for {} seconds", segmentId, fabricator.getElapsedSeconds());
  }

  /**
   Craft a Segment, or fail

   @param segmentId fabricating
   @throws NexusException on configuration failure
   @throws NexusException on craft failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doCraftWork")
  private void doCraftWork(String segmentId) throws NexusException {
    updateSegmentState(Segment.State.Planned, Segment.State.Crafting, segmentId);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.rhythm(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
  }

  /**
   Dub a Segment, or fail

   @param segmentId fabricating
   @throws NexusException on craft failure
   @throws NexusException on dub failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doDubWork")
  protected void doDubWork(String segmentId) throws NexusException {
    updateSegmentState(Segment.State.Crafting, Segment.State.Dubbing, segmentId);
    dubFactory.master(fabricator).doWork();
    dubFactory.ship(fabricator).doWork();
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param message   phrased like "Doing work"
   @param e         exception (optional)
   @param segmentId fabricating
   @param chainId   fabricating
   @param chainType fabricating
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "didFailWhile")
  private void didFailWhile(String message, Exception e, String segmentId, String chainId, String chainType) {
    var body = String.format("Failed while %s for Segment[%s] of Chain[%s] (%s) because %s\n\n%s",
      message,
      segmentId,
      chainId,
      chainType,
      e.getMessage(),
      Text.formatStackTrace(e));

    createSegmentErrorMessage(body, segmentId);

    notification.publish(body,
      String.format("%s-Chain[%s] Failure",
        chainType,
        chainId));

    LOG.error("Failed while {} for Segment[{}] of Chain[{}] ({}) because {}",
      message,
      segmentId,
      chainId,
      chainType,
      e.getMessage());
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param e       exception (optional)
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "didFailWhile")
  private void didFailWhile(String message, Exception e) {
    var body = String.format("Failed while %s because %s\n\n%s",
      message,
      e.getMessage(),
      Text.formatStackTrace(e));

    notification.publish(body, "Failure");

    LOG.error("Failed while {} because {}",
      message,
      e.getMessage());
  }

  /**
   Create a segment error message
   <p>
   [#177522463] Chain fabrication: segment messages broadcast somewhere the whole music team can see

   @param body      of message
   @param segmentId fabricating
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "createSegmentErrorMessage")
  protected void createSegmentErrorMessage(String body, String segmentId) {
    try {
      segmentDAO.create(access, SegmentMessage.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segment.getId())
        .setType(SegmentMessage.Type.Error)
        .setBody(body)
        .build());

    } catch (DAOValidationException | DAOPrivilegeException | DAOExistenceException | DAOFatalException e) {
      LOG.error("[segId={}] Could not create SegmentMessage, reason={}", segmentId, e.getMessage());
    }
  }

  /**
   Update Segment to Working state

   @param fromState of existing segment
   @param toState   of new segment
   @param segmentId fabricating
   @throws NexusException if record is invalid
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "updateSegmentState")
  private void updateSegmentState(Segment.State fromState, Segment.State toState, String segmentId) throws NexusException {
    if (fromState != segment.getState())
      throw new NexusException(String.format("Segment[%s] %s requires Segment must be in %s state.", segmentId, toState, fromState));
    fabricator.updateSegment(fabricator.getSegment().toBuilder().setState(toState).build());
    segment = fabricator.getSegment();
    LOG.debug("[segId={}] Segment transitioned to state {} OK", segmentId, toState);
  }


  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.
   <p>
   [#177021797] Medic relies on precomputed  telemetry of fabrication latency

   @throws DAOFatalException      on failure
   @throws DAOPrivilegeException  on failure
   @throws DAOValidationException on failure
   @throws DAOExistenceException  on failure
   */
  @Trace(resourceName = "nexus/medic", operationName = "checkAndReviveAll")
  public void checkAndReviveAll() throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    Instant thresholdChainProductionStartedBefore = Instant.now().minusSeconds(reviveChainProductionGraceSeconds);

    Map<String, String> stalledChainIds = Maps.newHashMap();
    chainDAO.readManyInState(access, Chain.State.Fabricate)
      .stream()
      .filter((chain) ->
        Chain.Type.Production.equals(chain.getType()) &&
          Instant.parse(chain.getStartAt()).isBefore(thresholdChainProductionStartedBefore))
      .forEach(chain -> {
        if (chain.getFabricatedAheadSeconds() < -reviveChainFabricatedBehindSeconds) {
          LOG.warn("Chain {} is stalled, fabricatedAheadSeconds={}",
            chain.getId(), chain.getFabricatedAheadSeconds());
          stalledChainIds.put(chain.getId(),
            String.format("fabricatedAheadSeconds=%s", chain.getFabricatedAheadSeconds()));
        }
      });

    // revive all stalled chains
    for (String stalledChainId : stalledChainIds.keySet()) {
      chainDAO.revive(access, stalledChainId, stalledChainIds.get(stalledChainId));
      // [#173968355] Nexus deletes entire chain when no current segments are left.
      chainDAO.destroy(access, stalledChainId);
    }

    telemetryProvider.getStatsDClient().incrementCounter("chain.revived", stalledChainIds.size());
  }

  /**
   Whether this Segment is before a given threshold, first by end-at if available, else begin-at

   @param eraseBefore threshold to filter before
   @return true if segment is before threshold
   */
  protected boolean isBefore(Segment segment, Instant eraseBefore) {
    return Value.isSet(segment.getEndAt()) ?
      Instant.parse(segment.getEndAt()).isBefore(eraseBefore) :
      Instant.parse(segment.getBeginAt()).isBefore(eraseBefore);
  }

  /**
   Get the IDs of all Segments that we ought to erase

   @return list of IDs of Segments we ought to erase
   */
  @Trace(resourceName = "nexus/janitor", operationName = "getSegmentIdsToErase")
  private Collection<String> getSegmentIdsToErase() throws NexusException {
    Instant eraseBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    Collection<String> segmentIds = Lists.newArrayList();
    for (String chainId : store.getAllChains().stream()
      .flatMap(Entities::flatMapIds)
      .collect(Collectors.toList()))
      store.getAllSegments(chainId)
        .stream()
        .filter(segment -> isBefore(segment, eraseBefore))
        .flatMap(Entities::flatMapIds)
        .forEach(segmentIds::add);
    return segmentIds;
  }

}
