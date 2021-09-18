// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.SegmentMessage;
import io.xj.api.SegmentMessageType;
import io.xj.api.SegmentState;
import io.xj.hub.enums.TemplateType;
import io.xj.lib.entity.Entities;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.service.ChainService;
import io.xj.nexus.Chains;
import io.xj.nexus.service.SegmentService;
import io.xj.nexus.Segments;
import io.xj.nexus.service.exception.ServiceExistenceException;
import io.xj.nexus.service.exception.ServiceFatalException;
import io.xj.nexus.service.exception.ServicePrivilegeException;
import io.xj.nexus.service.exception.ServiceValidationException;
import io.xj.nexus.dub.DubFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.hub_client.client.HubContent;
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
  private final ChainService chainService;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final FabricatorFactory fabricatorFactory;
  private final HubClient hubClient;
  private final HubClientAccess access = HubClientAccess.internal();
  private final Map<UUID, HubContent> chainSourceMaterial = Maps.newHashMap();
  private final Map<UUID, Long> chainNextIngestMillis = Maps.newHashMap();
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final SegmentService segmentService;
  private final TelemetryProvider telemetryProvider;
  private final boolean janitorEnabled;
  private final boolean medicEnabled;
  private final int bufferPreviewSeconds;
  private final int bufferProductionSeconds;
  private final int cycleMillis;
  private final int eraseSegmentsOlderThanSeconds;
  private final int ingestCycleSeconds;
  private final int janitorCycleSeconds;
  private final int medicCycleSeconds;
  private final int reviveChainFabricatedBehindSeconds;
  private final int reviveChainProductionGraceSeconds;
  private final long healthCycleStalenessThresholdMillis;
  private MultiStopwatch timer;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextMedicMillis = 0;
  private boolean alive = true;
  private final NexusWorkChainManager chainManager;

  @Inject
  public NexusWorkImpl(
    ChainService chainService,
    Config config,
    CraftFactory craftFactory,
    DubFactory dubFactory,
    FabricatorFactory fabricatorFactory,
    HubClient hubClient,
    NexusEntityStore store,
    NotificationProvider notification,
    SegmentService segmentService,
    TelemetryProvider telemetryProvider,
    NexusWorkChainManager nexusWorkChainManager
  ) {
    this.chainService = chainService;
    this.craftFactory = craftFactory;
    this.dubFactory = dubFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.hubClient = hubClient;
    this.notification = notification;
    this.segmentService = segmentService;
    this.store = store;
    this.telemetryProvider = telemetryProvider;
    this.chainManager = nexusWorkChainManager;

    bufferPreviewSeconds = config.getInt("work.bufferPreviewSeconds");
    bufferProductionSeconds = config.getInt("work.bufferProductionSeconds");
    cycleMillis = config.getInt("work.cycleMillis");
    eraseSegmentsOlderThanSeconds = config.getInt("work.eraseSegmentsOlderThanSeconds");
    healthCycleStalenessThresholdMillis = config.getInt("work.healthCycleStalenessThresholdSeconds") * MILLIS_PER_SECOND;
    ingestCycleSeconds = config.getInt("work.ingestCycleSeconds");
    janitorCycleSeconds = config.getInt("work.janitorCycleSeconds");
    janitorEnabled = config.getBoolean("work.janitorEnabled");
    medicCycleSeconds = config.getInt("work.medicCycleSeconds");
    medicEnabled = config.getBoolean("work.medicEnabled");
    reviveChainFabricatedBehindSeconds = config.getInt("fabrication.reviveChainFabricatedBehindSeconds");
    reviveChainProductionGraceSeconds = config.getInt("fabrication.reviveChainProductionGraceSeconds");
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
    chainManager.poll();

    // Replace an empty list so there is no possibility of nonexistence
    Collection<Chain> activeChains = Lists.newArrayList();
    try {
      try {
        activeChains.addAll(getActiveChains());
      } catch (ServiceFatalException | ServicePrivilegeException e) {
        didFailWhile("Getting list of active chain IDs", e);
        return;
      }

      // Fabricate all active chains
      activeChains.forEach(chain -> {
        ingestMaterialIfNecessary(chain);
        fabricateChain(chain);
      });

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
      var fabricatingChains = chainService.readManyInState(ChainState.FABRICATE);
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
      for (UUID stalledChainId : stalledChainIds.keySet()) {
        chainService.revive(stalledChainId, stalledChainIds.get(stalledChainId));
        // [#173968355] Nexus deletes entire chain when no current segments are left.
        chainService.destroy(stalledChainId);
      }

      telemetryProvider.put(METRIC_CHAIN_REVIVED, StandardUnit.Count, stalledChainIds.size());
      LOG.info("Total elapsed time: {}", timer.totalsToString());

    } catch (ServiceFatalException | ServicePrivilegeException | ServiceValidationException | ServiceExistenceException e) {
      didFailWhile("Medic checking & reviving all", e);
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
    Collection<UUID> segmentIdsToErase;
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

    for (UUID segmentId : segmentIdsToErase) {
      try {
        segmentService.destroy(segmentId);
        LOG.info("Did erase Segment[{}]", segmentId);
      } catch (ServiceFatalException | ServicePrivilegeException | ServiceExistenceException e) {
        LOG.warn("Error while destroying Segment[{}]", segmentId);
      }
    }

    telemetryProvider.put(METRIC_SEGMENT_ERASED, StandardUnit.Count, segmentIdsToErase.size());
  }

  /**
   Get the IDs of all Chains in the store whose state is currently in Fabricate

   @return active Chain IDS
   @throws ServicePrivilegeException on access control failure
   @throws ServiceFatalException     on internal failure
   */
  private List<Chain> getActiveChains() throws ServicePrivilegeException, ServiceFatalException {
    return new ArrayList<>(chainService.readManyInState(ChainState.FABRICATE));
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void fabricateChain(Chain chain) {
    try {
      timer.section("ComputeAhead");
      var fabricatedAheadSeconds = computeFabricatedAheadSeconds(chain);
      chain = updateFabricatedAheadSeconds(chain, fabricatedAheadSeconds);
      if (TemplateType.Preview.toString().equals(chain.getType().toString())
        && fabricatedAheadSeconds > bufferPreviewSeconds) return;
      else if (fabricatedAheadSeconds > bufferProductionSeconds) return;

      timer.section("BuildNext");
      int workBufferSeconds = bufferSecondsFor(chain);
      Optional<Segment> nextSegment = chainService.buildNextSegmentOrCompleteTheChain(chain,
        Instant.now().plusSeconds(workBufferSeconds),
        Instant.now().minusSeconds(workBufferSeconds));
      if (nextSegment.isEmpty()) return;

      Segment segment = segmentService.create(nextSegment.get());
      LOG.debug("Created Segment {}", segment);
      telemetryProvider.put(getChainMetricName(chain, METRIC_SEGMENT_CREATED), StandardUnit.Count, 1.0);

      Fabricator fabricator;
      timer.section("Prepare");
      try {
        LOG.debug("[segId={}] will prepare fabricator", segment.getId());
        fabricator = fabricatorFactory.fabricate(chainSourceMaterial.get(chain.getId()), segment);
      } catch (NexusException e) {
        didFailWhile("creating fabricator", e, segment.getId(), Chains.getIdentifier(chain), chain.getType().toString());
        return;
      }

      timer.section("Craft");
      try {
        LOG.debug("[segId={}] will do craft work", segment.getId());
        segment = doCraftWork(fabricator, segment);
      } catch (Exception e) {
        didFailWhile("doing Craft work", e, segment.getId(), Chains.getIdentifier(chain), chain.getType().toString());
        revert(chain, segment, fabricator);
        return;
      }

      timer.section("Dub");
      try {
        segment = doDubMasterWork(fabricator, segment);
      } catch (Exception e) {
        didFailWhile("doing Dub Master work", e, segment.getId(), Chains.getIdentifier(chain), chain.getType().toString());
        return;
      }

      // Update the chain fabricated-ahead seconds before shipping data
      var segmentLengthSeconds = Segments.getLengthSeconds(segment);
      fabricatedAheadSeconds += segmentLengthSeconds;
      chain = updateFabricatedAheadSeconds(chain, fabricatedAheadSeconds);

      timer.section("Ship");
      try {
        doDubShipWork(fabricator);
      } catch (Exception e) {
        didFailWhile("doing Dub Ship work", e, segment.getId(), Chains.getIdentifier(chain), chain.getType().toString());
        return;
      }

      try {
        finishWork(fabricator, segment);
      } catch (Exception e) {
        didFailWhile("finishing work", e, segment.getId(), Chains.getIdentifier(chain), chain.getType().toString());
      }

      LOG.info("Chain[{}] offset={} Segment[{}] ahead {}s fabricated OK",
        Chains.getIdentifier(chain),
        segment.getOffset(),
        Segments.getIdentifier(segment),
        fabricatedAheadSeconds);

    } catch (ServicePrivilegeException | ServiceExistenceException | ServiceValidationException | ServiceFatalException e) {
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

      try {
        chainService.revive(chain.getId(), body);
      } catch (ServiceFatalException | ServicePrivilegeException | ServiceExistenceException | ServiceValidationException e2) {
        LOG.error("Failed to revive chain after fatal error!", e2);
      }
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
   Determine the buffer for a specified type of chain

   @param chain to get buffer for, based on its type
   @return buffer # of seconds this type of chain
   */
  private int bufferSecondsFor(Chain chain) {
    return switch (chain.getType()) {
      case PRODUCTION -> bufferProductionSeconds;
      case PREVIEW -> bufferPreviewSeconds;
    };
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
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   [#171553408] Remove all Queue mechanics in favor of a cycle happening in Main class for as long as the application is alive, that does nothing but search for active chains, search for segments that need work, and work on them. Zero need for a work queue-- that's what the Chain-Segment state machine is!@param segmentId
   */
  private void revert(Chain chain, Segment segment, Fabricator fabricator) {
    try {
      updateSegmentState(fabricator, segment, fabricator.getSegment().getState(), SegmentState.PLANNED);
      segmentService.revert(access, segment.getId());
    } catch (ServiceFatalException | ServicePrivilegeException | ServiceValidationException | ServiceExistenceException | NexusException e) {
      didFailWhile("reverting and re-queueing segment", e, segment.getId(), Chains.getIdentifier(chain), chain.getType().toString());
    }
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

   @param message      phrased like "Doing work"
   @param e            exception (optional)
   @param segmentId    fabricating
   @param chainId      fabricating
   @param templateType fabricating
   */
  private void didFailWhile(String message, Exception e, UUID segmentId, String chainId, String templateType) {
    var body = String.format("Failed while %s for Segment[%s] of Chain[%s] (%s) because %s",
      message,
      segmentId,
      chainId,
      templateType,
      e.getMessage());

    createSegmentErrorMessage(body, segmentId);

    notification.publish(body,
      String.format("%s-Chain[%s] Failure",
        templateType,
        chainId));

    LOG.error("Failed while {} for Segment[{}] of Chain[{}] ({}) because {}",
      message,
      segmentId,
      chainId,
      templateType,
      e);
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
   Create a segment error message
   <p>
   [#177522463] Chain fabrication: segment messages broadcast somewhere the whole music team can see

   @param body      of message
   @param segmentId fabricating
   */
  protected void createSegmentErrorMessage(String body, UUID segmentId) {
    try {
      var msg = new SegmentMessage();
      msg.setId(UUID.randomUUID());
      msg.setSegmentId(segmentId);
      msg.setType(SegmentMessageType.ERROR);
      msg.setBody(body);
      segmentService.create(access, msg);

    } catch (ServiceValidationException | ServicePrivilegeException | ServiceExistenceException | ServiceFatalException e) {
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
    return Value.isSet(segment.getEndAt()) ?
      Instant.parse(segment.getEndAt()).isBefore(eraseBefore) :
      Instant.parse(segment.getBeginAt()).isBefore(eraseBefore);
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
    return chainManager.isHealthy()
      && nextCycleMillis > System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

  /**
   Update a chain's fabricate ahead seconds

   @param chain                  to update
   @param fabricatedAheadSeconds value to set
   @return updated chain
   @throws ServiceFatalException      on failure
   @throws ServicePrivilegeException  on failure
   @throws ServiceValidationException on failure
   @throws ServiceExistenceException  on failure
   */
  private Chain updateFabricatedAheadSeconds(Chain chain, float fabricatedAheadSeconds) throws ServiceFatalException, ServicePrivilegeException, ServiceValidationException, ServiceExistenceException {
    telemetryProvider.put(getChainMetricName(chain, METRIC_FABRICATED_AHEAD_SECONDS), StandardUnit.Seconds, fabricatedAheadSeconds);

    return chainService.update(chain.getId(), chain.fabricatedAheadSeconds((double) fabricatedAheadSeconds));
  }

  /**
   [#177072936] Mk1 UI each chain shows current fabrication latency

   @param chain fabricating
   */
  private float computeFabricatedAheadSeconds(Chain chain) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException {
    return Chains.computeFabricatedAheadSeconds(chain, segmentService.readMany(ImmutableList.of(chain.getId())));
  }
}
