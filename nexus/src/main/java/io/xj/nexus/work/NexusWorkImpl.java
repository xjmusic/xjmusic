// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.hub.enums.TemplateType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dub.DubFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.xj.lib.telemetry.MultiStopwatch.MILLIS_PER_SECOND;

/**
 The Lab Nexus Distributed Work Manager (Implementation)
 <p>
 https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 */
@Singleton
public class NexusWorkImpl implements NexusWork {
  private static final Logger LOG = LoggerFactory.getLogger(NexusWorkImpl.class);
  private static final String DEFAULT_NAME_PREVIEW = "preview";
  private static final String DEFAULT_NAME_PRODUCTION = "production";
  private static final String METRIC_CHAIN_FORMAT = "chain.%s.%s";
  private static final String METRIC_FABRICATED_AHEAD_SECONDS = "fabricated_ahead_seconds";
  private static final String METRIC_SEGMENT_CREATED = "segment_created";
  private static final String METRIC_CHAIN_REVIVED = "chain_revived";
  private static final String METRIC_SEGMENT_ERASED = "segment_erased";
  private final NexusWorkChainManager nexusWorkChainManager;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final FabricatorFactory fabricatorFactory;
  private final HubClient hubClient;
  private final HubClientAccess access = HubClientAccess.internal();
  private final Map<UUID, HubContent> chainSourceMaterial = Maps.newHashMap();
  private final Map<UUID, Long> chainNextIngestMillis = Maps.newHashMap();
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final SegmentManager segmentManager;
  private final TelemetryProvider telemetryProvider;
  private final boolean janitorEnabled;
  private final boolean medicEnabled;
  private final int cycleMillis;
  private final int eraseSegmentsOlderThanSeconds;
  private final int ingestCycleSeconds;
  private final int janitorCycleSeconds;
  private final int medicCycleSeconds;
  private final int reviveChainFabricatedBehindSeconds;
  private final int reviveChainProductionGraceSeconds;
  private final long healthCycleStalenessThresholdMillis;
  private final ChainManager chainManager;
  private MultiStopwatch timer;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextMedicMillis = 0;
  private boolean alive = true;

  @Inject
  public NexusWorkImpl(
    ChainManager chainManager,
    Environment env,
    CraftFactory craftFactory,
    DubFactory dubFactory,
    FabricatorFactory fabricatorFactory,
    HubClient hubClient,
    NexusEntityStore store,
    NexusWorkChainManager nexusWorkChainManager,
    NotificationProvider notification,
    SegmentManager segmentManager,
    TelemetryProvider telemetryProvider
  ) {
    this.chainManager = chainManager;
    this.nexusWorkChainManager = nexusWorkChainManager;
    this.craftFactory = craftFactory;
    this.dubFactory = dubFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.hubClient = hubClient;
    this.notification = notification;
    this.segmentManager = segmentManager;
    this.store = store;
    this.telemetryProvider = telemetryProvider;

    cycleMillis = env.getWorkCycleMillis();
    eraseSegmentsOlderThanSeconds = env.getWorkEraseSegmentsOlderThanSeconds();
    healthCycleStalenessThresholdMillis = env.getWorkHealthCycleStalenessThresholdSeconds() * MILLIS_PER_SECOND;
    ingestCycleSeconds = env.getWorkIngestCycleSeconds();
    janitorCycleSeconds = env.getWorkJanitorCycleSeconds();
    janitorEnabled = env.isWorkJanitorEnabled();
    medicCycleSeconds = env.getWorkMedicCycleSeconds();
    medicEnabled = env.isWorkMedicEnabled();
    reviveChainFabricatedBehindSeconds = env.getFabricationReviveChainFabricatedBehindSeconds();
    reviveChainProductionGraceSeconds = env.getFabricationReviveChainProductionGraceSeconds();
    Executors.newSingleThreadScheduledExecutor();

    LOG.debug("Instantiated OK");
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void run() {
    if (System.currentTimeMillis() < nextCycleMillis) return;
    nextCycleMillis = System.currentTimeMillis() + cycleMillis;

    // Poll the chain manager
    nexusWorkChainManager.poll();

    // Replace an empty list so there is no possibility of nonexistence
    Collection<Chain> activeChains = Lists.newArrayList();
    try {
      try {
        activeChains.addAll(getActiveChains());
      } catch (ManagerFatalException | ManagerPrivilegeException e) {
        didFailWhile("Getting list of active chain IDs", e);
        return;
      }

      // Fabricate all active chains
      for (Chain chain : activeChains) {
        ingestMaterialIfNecessary(chain);
        fabricateChain(chain);
      }

      if (medicEnabled) doMedic();
      if (janitorEnabled) doJanitor();
    } catch (Exception e) {
      didFailWhile("Running Nexus Work", e);
    }

    // End lap & do telemetry on all fabricated chains
    timer.lap();
    LOG.info("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
  }

  /**
   Ingest Content from Hub
   */
  private void ingestMaterialIfNecessary(Chain chain) {
    if (chainNextIngestMillis.containsKey(chain.getId()) &&
      System.currentTimeMillis() < chainNextIngestMillis.get(chain.getId())) return;
    chainNextIngestMillis.put(chain.getId(), System.currentTimeMillis() + ingestCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Ingest");

    try {
      // read the source material
      var material = hubClient.ingest(access, chain.getTemplateId());
      chainSourceMaterial.put(chain.getId(), material);
      LOG.debug("Ingested {} entities of source material for Chain[{}]", material.size(), Chains.getIdentifier(chain));

    } catch (HubClientException e) {
      didFailWhile("Ingesting source material from Hub", e);
    }
  }

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.
   <p>
   [#177021797] Medic relies on precomputed  telemetry of fabrication latency
   */
  private void doMedic() {
    if (System.currentTimeMillis() < nextMedicMillis) return;
    nextMedicMillis = System.currentTimeMillis() + (medicCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Medic");

    try {
      // After a chain starts, it has N seconds before being judged stalled
      Instant thresholdChainProductionStartedBefore = Instant.now().minusSeconds(reviveChainProductionGraceSeconds);

      Map<UUID, String> stalledChainIds = Maps.newHashMap();
      var fabricatingChains = chainManager.readManyInState(ChainState.FABRICATE);
      LOG.info("Medic will check {} fabricating {}",
        fabricatingChains.size(), 1 < fabricatingChains.size() ? "Chains" : "Chain");
      fabricatingChains
        .stream()
        .filter((chain) ->
          TemplateType.Production.toString().equals(chain.getType().toString()) &&
            Instant.parse(chain.getStartAt()).isBefore(thresholdChainProductionStartedBefore))
        .forEach(chain -> {
          if (chain.getFabricatedAheadSeconds() < reviveChainFabricatedBehindSeconds) {
            LOG.warn("Chain {} is stalled, fabricatedAheadSeconds={}",
              Chains.getIdentifier(chain), chain.getFabricatedAheadSeconds());
            stalledChainIds.put(chain.getId(),
              String.format("fabricatedAheadSeconds=%s", chain.getFabricatedAheadSeconds()));
          }
        });

      // revive all stalled chains
      for (UUID stalledChainId : stalledChainIds.keySet())
        revive(stalledChainId, stalledChainIds.get(stalledChainId));

      LOG.info("Total elapsed time: {}", timer.totalsToString());

    } catch (ManagerFatalException | ManagerPrivilegeException e) {
      didFailWhile("Medic checking & reviving all", e);
    }
  }

  /**
   Revive a chain

   @param chainId to revive
   @param reason  this chain is being revived
   */
  private void revive(UUID chainId, String reason) {
    try {
      chainManager.revive(chainId, reason);
      // [#173968355] Nexus deletes entire chain when no current segments are left.
      chainManager.destroy(chainId);
      telemetryProvider.put(METRIC_CHAIN_REVIVED, StandardUnit.Count, 1);
    } catch (ManagerFatalException | ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException e) {
      LOG.error("Failed to revive chain after fatal error!", e);
    }
  }


  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  protected void doJanitor() {
    if (System.currentTimeMillis() < nextJanitorMillis) return;
    nextJanitorMillis = System.currentTimeMillis() + (janitorCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Janitor");

    // Seek segments to erase
    Collection<UUID> gcSegIds;
    try {
      gcSegIds = getSegmentIdsToErase();
    } catch (NexusException e) {
      didFailWhile("Checking for segments to erase", e);
      return;
    }

    // Erase segments if necessary
    if (gcSegIds.isEmpty())
      LOG.info("Found no segments to erase");
    else
      LOG.info("Will garbage collect {} segments", gcSegIds.size());

    for (UUID segmentId : gcSegIds) {
      try {
        segmentManager.destroy(segmentId);
        LOG.debug("collected garbage Segment[{}]", segmentId);
      } catch (ManagerFatalException | ManagerPrivilegeException | ManagerExistenceException e) {
        LOG.warn("Error while destroying Segment[{}]", segmentId);
      }
    }

    telemetryProvider.put(METRIC_SEGMENT_ERASED, StandardUnit.Count, gcSegIds.size());
  }

  /**
   Get the IDs of all Chains in the store whose state is currently in Fabricate

   @return active Chain IDS
   @throws ManagerPrivilegeException on access control failure
   @throws ManagerFatalException     on internal failure
   */
  private List<Chain> getActiveChains() throws ManagerPrivilegeException, ManagerFatalException {
    return new ArrayList<>(chainManager.readManyInState(ChainState.FABRICATE));
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void fabricateChain(Chain chain) {
    try {
      timer.section("ComputeAhead");
      var fabricatedAheadSeconds = computeFabricatedAheadSeconds(chain);
      chain = updateFabricatedAheadSeconds(chain, fabricatedAheadSeconds);
      var templateConfig = chainManager.getTemplateConfig(chain.getId());
      if (fabricatedAheadSeconds > templateConfig.getBufferAheadSeconds()) return;

      timer.section("BuildNext");
      Optional<Segment> nextSegment = chainManager.buildNextSegmentOrCompleteTheChain(chain,
        Instant.now().plusSeconds(templateConfig.getBufferAheadSeconds()),
        Instant.now().minusSeconds(templateConfig.getBufferAheadSeconds()));
      if (nextSegment.isEmpty()) return;

      Segment segment = segmentManager.create(nextSegment.get());
      LOG.debug("Created Segment {}", segment);
      telemetryProvider.put(getChainMetricName(chain, METRIC_SEGMENT_CREATED), StandardUnit.Count, 1.0);

      Fabricator fabricator;
      timer.section("Prepare");
      LOG.debug("[segId={}] will prepare fabricator", segment.getId());
      fabricator = fabricatorFactory.fabricate(chainSourceMaterial.get(chain.getId()), segment);

      timer.section("Craft");
      LOG.debug("[segId={}] will do craft work", segment.getId());
      segment = doCraftWork(fabricator, segment);

      timer.section("Dub");
      segment = doDubMasterWork(fabricator, segment);

      // Update the chain fabricated-ahead seconds before shipping data
      var segmentLengthSeconds = Segments.getLengthSeconds(segment);
      fabricatedAheadSeconds += segmentLengthSeconds;
      chain = updateFabricatedAheadSeconds(chain, fabricatedAheadSeconds);

      timer.section("Ship");
      doDubShipWork(fabricator);
      finishWork(fabricator, segment);

      LOG.info("Chain[{}] offset={} Segment[{}] ahead {}s fabricated OK",
        Chains.getIdentifier(chain),
        segment.getOffset(),
        Segments.getIdentifier(segment),
        fabricatedAheadSeconds);

    } catch (ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException | ManagerFatalException | NexusException | ValueException e) {
      var body = String.format("Failed to create Segment of Chain[%s] (%s) because %s\n\n%s",
        Chains.getIdentifier(chain),
        chain.getType(),
        e.getMessage(),
        Text.formatStackTrace(e));

      notification.publish(body,
        String.format("%s-Chain[%s] Failure",
          chain.getType(),
          Chains.getIdentifier(chain)));

      LOG.error("Failed to created Segment in Chain[{}] reason={}", Chains.getIdentifier(chain), e.getMessage());

      revive(chain.getId(), body);
    }
  }

  @Override
  public void work() {
    timer = MultiStopwatch.start();
    while (alive) this.run();
  }

  @Override
  public void stop() {
    alive = false;
  }

  /**
   Get the name for a given chain and metric

   @param chain      to get name for
   @param metricName to get metric name for
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
    return TemplateType.Production.toString().equals(chain.getType().toString()) ?
      (!Strings.isNullOrEmpty(chain.getShipKey()) ? chain.getShipKey() : DEFAULT_NAME_PRODUCTION) :
      DEFAULT_NAME_PREVIEW;
  }

  /**
   Finish work on Segment@param segmentId
   */
  private void finishWork(Fabricator fabricator, Segment segment) throws NexusException {
    updateSegmentState(fabricator, segment, SegmentState.DUBBING, SegmentState.DUBBED);
    LOG.debug("[segId={}] Worked for {} seconds", segment.getId(), fabricator.getElapsedSeconds());
  }

  /**
   Craft a Segment, or fail

   @param fabricator to craft
   @param segment    fabricating
   @throws NexusException on configuration failure
   @throws NexusException on craft failure
   */
  private Segment doCraftWork(Fabricator fabricator, Segment segment) throws NexusException {
    var updated = updateSegmentState(fabricator, segment, SegmentState.PLANNED, SegmentState.CRAFTING);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.rhythm(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
    craftFactory.percLoop(fabricator).doWork();
    craftFactory.transition(fabricator).doWork();
    craftFactory.background(fabricator).doWork();
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
  protected Segment doDubMasterWork(Fabricator fabricator, Segment segment) throws NexusException {
    var updated = updateSegmentState(fabricator, segment, SegmentState.CRAFTING, SegmentState.DUBBING);
    dubFactory.master(fabricator).doWork();
    return updated;
  }

  /**
   Ship a Segment, or fail

   @param fabricator to ship
   @throws NexusException on craft failure
   @throws NexusException on ship failure
   */
  protected void doDubShipWork(Fabricator fabricator) throws NexusException {
    dubFactory.ship(fabricator).doWork();
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param e       exception (optional)
   */
  private void didFailWhile(String message, Exception e) {
    var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    LOG.error("Failed while {} because {}", message, detail, e);

    notification.publish(String.format("Failed while %s because %s\n\n%s", message, detail, Text.formatStackTrace(e)), "Failure");
  }

  /**
   Update Segment to Working state

   @param fabricator to update
   @param fromState  of existing segment
   @param toState    of new segment
   @return updated Segment
   @throws NexusException if record is invalid
   */
  private Segment updateSegmentState(Fabricator fabricator, Segment segment, SegmentState fromState, SegmentState toState) throws NexusException {
    if (fromState != segment.getState())
      throw new NexusException(String.format("Segment[%s] %s requires Segment must be in %s state.", segment.getId(), toState, fromState));
    var seg = fabricator.getSegment();
    seg.setState(toState);
    fabricator.updateSegment(seg);
    LOG.debug("[segId={}] Segment transitioned to state {} OK", segment.getId(), toState);
    return fabricator.getSegment();
  }


  /**
   Whether this Segment is before a given threshold, first by end-at if available, else begin-at

   @param eraseBefore threshold to filter before
   @return true if segment is before threshold
   */
  protected boolean isBefore(Segment segment, Instant eraseBefore) {
    if (Values.isSet(segment.getEndAt())) return Instant.parse(segment.getEndAt()).isBefore(eraseBefore);
    if (Values.isSet(segment.getBeginAt())) return Instant.parse(segment.getBeginAt()).isBefore(eraseBefore);
    return false;
  }

  /**
   Get the IDs of all Segments that we ought to erase

   @return list of IDs of Segments we ought to erase
   */
  private Collection<UUID> getSegmentIdsToErase() throws NexusException {
    Instant eraseBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    Collection<UUID> segmentIds = Lists.newArrayList();
    for (UUID chainId : store.getAllChains().stream()
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
    return nexusWorkChainManager.isHealthy()
      && nextCycleMillis > System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

  /**
   Update a chain's fabricate ahead seconds

   @param chain                  to update
   @param fabricatedAheadSeconds value to set
   @return updated chain
   @throws ManagerFatalException      on failure
   @throws ManagerPrivilegeException  on failure
   @throws ManagerValidationException on failure
   @throws ManagerExistenceException  on failure
   */
  private Chain updateFabricatedAheadSeconds(Chain chain, float fabricatedAheadSeconds) throws ManagerFatalException, ManagerPrivilegeException, ManagerValidationException, ManagerExistenceException {
    telemetryProvider.put(getChainMetricName(chain, METRIC_FABRICATED_AHEAD_SECONDS), StandardUnit.Seconds, fabricatedAheadSeconds);

    return chainManager.update(chain.getId(), chain.fabricatedAheadSeconds((double) fabricatedAheadSeconds));
  }

  /**
   [#177072936] Mk1 UI each chain shows current fabrication latency

   @param chain fabricating
   */
  private float computeFabricatedAheadSeconds(Chain chain) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException {
    return Chains.computeFabricatedAheadSeconds(chain, segmentManager.readMany(ImmutableList.of(chain.getId())));
  }
}
