// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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
import io.xj.lib.util.MultiStopwatch;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import static io.xj.lib.util.MultiStopwatch.MILLIS_PER_SECOND;
import static io.xj.lib.util.MultiStopwatch.NANOS_PER_MILLI;
import static io.xj.lib.util.MultiStopwatch.NANOS_PER_SECOND;

/**
 The Lab Nexus Distributed Work Manager (Implementation)
 <p>
 https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 */
@Singleton
public class NexusWorkImpl implements NexusWork {
  private static final Logger LOG = LoggerFactory.getLogger(NexusWorkImpl.class);
  private ScheduledFuture<?> schedule;
  private final ChainDAO chainDAO;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final FabricatorFactory fabricatorFactory;
  private final HubClientAccess access = HubClientAccess.internal();
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final ScheduledExecutorService scheduler;
  private final SegmentDAO segmentDAO;
  private final TelemetryProvider telemetryProvider;
  private final boolean janitorEnabled;
  private final boolean medicEnabled;
  private final int bufferPreviewSeconds;
  private final int bufferProductionSeconds;
  private final int cycleMillis;
  private final int eraseSegmentsOlderThanSeconds;
  private final int janitorCycleSeconds;
  private final int medicCycleSeconds;
  private final int reviveChainFabricatedBehindSeconds;
  private final int reviveChainProductionGraceSeconds;
  private final long healthCycleStalenessThresholdNanos;

  private long nextCycleNanos;

  private long nextJanitorNanos = 0;
  private long nextMedicNanos = 0;
  private static final String DEFAULT_NAME_PREVIEW = "preview";
  private static final String DEFAULT_NAME_PRODUCTION = "production";
  private static final String METRIC_CHAIN_FORMAT = "chain.%s.%s";
  private static final String METRIC_FABRICATED_AHEAD_SECONDS = "fabricated_ahead_seconds";
  private static final String METRIC_SEGMENT_CREATED = "segment_created";
  private MultiStopwatch timer;

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
    cycleMillis = config.getInt("work.cycleMillis");
    eraseSegmentsOlderThanSeconds = config.getInt("work.eraseSegmentsOlderThanSeconds");
    healthCycleStalenessThresholdNanos = config.getInt("work.healthCycleStalenessThresholdSeconds") * NANOS_PER_SECOND;
    janitorCycleSeconds = config.getInt("work.janitorCycleSeconds");
    janitorEnabled = config.getBoolean("work.janitorEnabled");
    medicCycleSeconds = config.getInt("work.medicCycleSeconds");
    medicEnabled = config.getBoolean("work.medicEnabled");
    reviveChainFabricatedBehindSeconds = config.getInt("fabrication.reviveChainFabricatedBehindSeconds");
    reviveChainProductionGraceSeconds = config.getInt("fabrication.reviveChainProductionGraceSeconds");
    scheduler = Executors.newSingleThreadScheduledExecutor();

    LOG.debug("Instantiated OK");
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/boss", operationName = "run")
  public void run() {
    if (System.nanoTime() < nextCycleNanos) return;
    nextCycleNanos = System.nanoTime() + cycleMillis * NANOS_PER_MILLI;
    try {
      doFabrication();
      if (medicEnabled) doMedic();
      if (janitorEnabled) doJanitor();
    } catch (Exception e) {
      if (!Strings.isNullOrEmpty(e.getMessage()))
        didFailWhile("Running Nexus Work", e);
      else
        didFailWhile("Running Nexus Work", e.getClass().getSimpleName(), "");
    }
    timer.lap();
    LOG.info("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
  }

  /**
   Do fabrication
   */
  private void doFabrication() {

    // Get active chain IDs
    Collection<Chain> activeChains;
    try {
      activeChains = getActiveChains();
    } catch (DAOFatalException | DAOPrivilegeException e) {
      didFailWhile("Getting list of active chain IDs", e);
      return;
    }

    // Fabricate all active chains
    activeChains.forEach(this::fabricateChain);
  }

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.
   <p>
   [#177021797] Medic relies on precomputed  telemetry of fabrication latency
   */
  private void doMedic() {
    if (System.nanoTime() < nextMedicNanos) return;
    nextMedicNanos = System.nanoTime() + (medicCycleSeconds * NANOS_PER_SECOND);
    timer.section("Medic");

    LOG.info("Total elapsed time: {}", timer.totalsToString());
    try {
      Instant thresholdChainProductionStartedBefore = Instant.now().minusSeconds(reviveChainProductionGraceSeconds);

      Map<String, String> stalledChainIds = Maps.newHashMap();
      var fabricatingChains = chainDAO.readManyInState(access, Chain.State.Fabricate);
      LOG.info("Medic will check {} fabricating Chain{}", fabricatingChains.size(), 1 < fabricatingChains.size() ? "s" : "");
      fabricatingChains
        .stream()
        .filter((chain) ->
          Chain.Type.Production.equals(chain.getType()) &&
            Instant.parse(chain.getStartAt()).isBefore(thresholdChainProductionStartedBefore))
        .forEach(chain -> {
          if (chain.getFabricatedAheadSeconds() < -reviveChainFabricatedBehindSeconds) {
            LOG.warn("Chain {} is stalled, fabricatedAheadSeconds={}",
              chainDAO.getIdentifier(chain), chain.getFabricatedAheadSeconds());
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

    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException e) {
      didFailWhile("Medic checking & reviving all", e);
    }
  }


  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/janitor", operationName = "doWork")
  protected void doJanitor() {
    if (System.nanoTime() < nextJanitorNanos) return;
    nextJanitorNanos = System.nanoTime() + (janitorCycleSeconds * NANOS_PER_SECOND);
    timer.section("Janitor");

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
      LOG.info("Found no segments to erase");
    else
      LOG.info("Found {} segments to erase", segmentIdsToErase.size());

    for (String segmentId : segmentIdsToErase) {
      try {
        segmentDAO.destroy(access, segmentId);
        LOG.info("Did erase Segment[{}]", segmentId);
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
  private List<Chain> getActiveChains() throws DAOPrivilegeException, DAOFatalException {
    return new ArrayList<>(chainDAO.readManyInState(access, Chain.State.Fabricate));
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/chain", operationName = "doWork")
  public void fabricateChain(Chain chain) {
    try {
      int workBufferSeconds = bufferSecondsFor(chain);
      Optional<Segment> nextSegment = chainDAO.buildNextSegmentOrCompleteTheChain(access, chain,
        Instant.now().plusSeconds(workBufferSeconds),
        Instant.now().minusSeconds(workBufferSeconds));
      if (nextSegment.isEmpty()) return;

      Segment segment = segmentDAO.create(access, nextSegment.get());
      LOG.debug("Created Segment {}", segment);
      telemetryProvider.getStatsDClient().
        incrementCounter(getChainMetricName(chain, METRIC_SEGMENT_CREATED));

      // Fabricate this segment and measure sections of time
      fabricateSegment(chain, segment, timer);

      // bums
      var fabricatedAheadSeconds = computeFabricatedAheadSeconds(chain);
      telemetryProvider.getStatsDClient()
        .gauge(getChainMetricName(chain, METRIC_FABRICATED_AHEAD_SECONDS), fabricatedAheadSeconds);
      chainDAO.update(access, chain.getId(), chain.toBuilder()
        .setFabricatedAheadSeconds(fabricatedAheadSeconds)
        .build());

    } catch (DAOPrivilegeException | DAOExistenceException | DAOValidationException | DAOFatalException e) {
      var body = String.format("Failed to create Segment of Chain[%s] (%s) because %s\n\n%s",
        chainDAO.getIdentifier(chain),
        chain.getType(),
        e.getMessage(),
        Text.formatStackTrace(e));

      notification.publish(body,
        String.format("%s-Chain[%s] Failure",
          chain.getType(),
          chainDAO.getIdentifier(chain)));

      LOG.error("Failed to created Segment in Chain[{}] reason={}", chainDAO.getIdentifier(chain), e.getMessage());

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
    return computeFabricatedAheadSeconds(chain, segmentDAO.readMany(access, ImmutableList.of(chain.getId())));
  }

  @Override
  public float computeFabricatedAheadSeconds(Chain chain, Collection<Segment> segments) {
    var lastDubbedSegment = segmentDAO.getLastDubbed(segments);
    var dubbedUntil = lastDubbedSegment.isPresent() ?
      Instant.parse(lastDubbedSegment.get().getEndAt()) :
      Instant.parse(chain.getStartAt());
    var now = Instant.now();
    return (float) (dubbedUntil.toEpochMilli() - now.toEpochMilli()) / MILLIS_PER_SECOND;
  }

  @Override
  public void work() {
    timer = MultiStopwatch.start();
    //noinspection InfiniteLoopStatement
    while (true) this.run();
  }

  /**
   Determine the buffer for a specified type of chain

   @param chain to get buffer for, based on its type
   @return buffer # of seconds this type of chain
   */
  private int bufferSecondsFor(Chain chain) {
    return switch (chain.getType()) {
      case Production -> bufferProductionSeconds;
      case Preview -> bufferPreviewSeconds;
      case UNRECOGNIZED -> 0;
    };
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
  protected void fabricateSegment(Chain chain, Segment segment, MultiStopwatch timer) {
    Fabricator fabricator;

    timer.section("Prepare");
    try {
      LOG.debug("[segId={}] will prepare fabricator", segment.getId());
      fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment);
    } catch (NexusException e) {
      didFailWhile("creating fabricator", e, segment.getId(), chainDAO.getIdentifier(chain), chain.getType().toString());
      return;
    }

    timer.section("Craft");
    try {
      LOG.debug("[segId={}] will do craft work", segment.getId());
      segment = doCraftWork(fabricator, segment);
    } catch (Exception e) {
      didFailWhile("doing Craft work", e, segment.getId(), chainDAO.getIdentifier(chain), chain.getType().toString());
      revert(chain, segment, fabricator);
      return;
    }

    timer.section("Dub");
    try {
      segment = doDubMasterWork(fabricator, segment);
    } catch (Exception e) {
      didFailWhile("doing Dub Master work", e, segment.getId(), chainDAO.getIdentifier(chain), chain.getType().toString());
      return;
    }

    timer.section("Ship");
    try {
      doDubShipWork(fabricator);
    } catch (Exception e) {
      didFailWhile("doing Dub Ship work", e, segment.getId(), chainDAO.getIdentifier(chain), chain.getType().toString());
      return;
    }

    try {
      finishWork(fabricator, segment);
    } catch (Exception e) {
      didFailWhile("finishing work", e, segment.getId(), chainDAO.getIdentifier(chain), chain.getType().toString());
    }

    LOG.info("Fabricated {} Chain[{}] offset={} Segment[{}]",
      chain.getType(),
      chainDAO.getIdentifier(chain),
      segment.getOffset(),
      segmentDAO.getIdentifier(segment));
  }

  /**
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   [#171553408] Remove all Queue mechanics in favor of a cycle happening in Main class for as long as the application is alive, that does nothing but search for active chains, search for segments that need work, and work on them. Zero need for a work queue-- that's what the Chain-Segment state machine is!@param segmentId
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "revert")
  private void revert(Chain chain, Segment segment, Fabricator fabricator) {
    try {
      updateSegmentState(fabricator, segment, fabricator.getSegment().getState(), Segment.State.Planned);
      segmentDAO.revert(access, segment.getId());
    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException | NexusException e) {
      didFailWhile("reverting and re-queueing segment", e, segment.getId(), chain.getId(), chain.getType().toString());
    }
  }

  /**
   Finish work on Segment@param segmentId
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "finishWork")
  private void finishWork(Fabricator fabricator, Segment segment) throws NexusException {
    updateSegmentState(fabricator, segment, Segment.State.Dubbing, Segment.State.Dubbed);
    LOG.debug("[segId={}] Worked for {} seconds", segment.getId(), fabricator.getElapsedSeconds());
  }

  /**
   Craft a Segment, or fail

   @param fabricator to craft
   @param segment    fabricating
   @throws NexusException on configuration failure
   @throws NexusException on craft failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doCraftWork")
  private Segment doCraftWork(Fabricator fabricator, Segment segment) throws NexusException {
    var updated = updateSegmentState(fabricator, segment, Segment.State.Planned, Segment.State.Crafting);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.rhythm(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
    return updated;
  }

  /**
   Dub a Segment, or fail

   @param fabricator to dub master
   @param segment    fabricating
   @return updated Segment
   @throws NexusException on craft failure
   @throws NexusException on dub failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doDubWork")
  protected Segment doDubMasterWork(Fabricator fabricator, Segment segment) throws NexusException {
    var updated = updateSegmentState(fabricator, segment, Segment.State.Crafting, Segment.State.Dubbing);
    dubFactory.master(fabricator).doWork();
    return updated;
  }

  /**
   Ship a Segment, or fail

   @param fabricator to ship
   @throws NexusException on craft failure
   @throws NexusException on ship failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doShipWork")
  protected void doDubShipWork(Fabricator fabricator) throws NexusException {
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
    didFailWhile(message, e.getMessage(), Text.formatStackTrace(e));
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param detail  to include in body
   @param debug   to include in body
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "didFailWhile")
  private void didFailWhile(String message, String detail, String debug) {
    var body = String.format("Failed while %s because %s\n\n%s",
      message,
      detail,
      debug);

    notification.publish(body, "Failure");

    LOG.error("Failed while {} because {}",
      message,
      detail);
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
        .setSegmentId(segmentId)
        .setType(SegmentMessage.Type.Error)
        .setBody(body)
        .build());

    } catch (DAOValidationException | DAOPrivilegeException | DAOExistenceException | DAOFatalException e) {
      LOG.error("[segId={}] Could not create SegmentMessage, reason={}", segmentId, e.getMessage());
    }
  }

  /**
   Update Segment to Working state

   @param fabricator to update
   @param fromState  of existing segment
   @param toState    of new segment
   @return updated Segment
   @throws NexusException if record is invalid
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "updateSegmentState")
  private Segment updateSegmentState(Fabricator fabricator, Segment segment, Segment.State fromState, Segment.State toState) throws NexusException {
    if (fromState != segment.getState())
      throw new NexusException(String.format("Segment[%s] %s requires Segment must be in %s state.", segment.getId(), toState, fromState));
    fabricator.updateSegment(fabricator.getSegment().toBuilder().setState(toState).build());
    LOG.debug("[segId={}] Segment transitioned to state {} OK", segment.getId(), toState);
    return fabricator.getSegment();
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

  @Override
  public boolean isHealthy() {
    return nextCycleNanos > System.nanoTime() - healthCycleStalenessThresholdNanos;
  }
}
