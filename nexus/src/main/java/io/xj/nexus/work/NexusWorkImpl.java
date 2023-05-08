// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.opencensus.stats.Measure;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubClientAccess;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.manager.Templates;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.hub.service.ServiceException;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dub.DubFactory;
import io.xj.nexus.fabricator.FabricationFatalException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.lib.lock.LockProvider;
import io.xj.lib.lock.LockException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.Chains;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.ManagerValidationException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.Segments;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.filestore.FileStoreProvider.EXTENSION_JSON;
import static io.xj.lib.util.Values.MILLIS_PER_SECOND;
import static io.xj.lib.util.Values.NANOS_PER_SECOND;
import static io.xj.nexus.work.NexusWorkImpl.Mode.Lab;

@Service
public class NexusWorkImpl implements NexusWork {
  private static final Logger LOG = LoggerFactory.getLogger(NexusWorkImpl.class);
  private static final long INTERNAL_CYCLE_SLEEP_MILLIS = 50;
  private final ChainManager chainManager;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final EntityFactory entityFactory;
  private final FabricatorFactory fabricatorFactory;
  private final LockProvider lockProvider;
  private final HttpClientProvider httpClientProvider;
  private final HubClient hubClient;
  private final HubClientAccess access = HubClientAccess.internal();
  private final JsonProvider jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final Map<UUID, HubContent> chainSourceMaterial = Maps.newHashMap();
  private final Map<UUID, Long> chainNextIngestMillis = Maps.newHashMap();
  private final Measure.MeasureDouble METRIC_FABRICATED_AHEAD_SECONDS;
  private final Measure.MeasureLong METRIC_SEGMENT_CREATED;
  private final Measure.MeasureLong METRIC_SEGMENT_GC;
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final SegmentManager segmentManager;
  private final String shipBaseUrl;
  private final TelemetryProvider telemetryProvider;
  private final boolean janitorEnabled;
  private final boolean medicEnabled;
  private final int chainThresholdFabricatedBehindSeconds;
  private final int cycleMillis;
  private final int eraseSegmentsOlderThanSeconds;
  private final int ignoreSegmentsOlderThanSeconds;
  private final int ingestCycleSeconds;
  private final int janitorCycleSeconds;
  private final int labPollSeconds;
  private final int yardPollSeconds;
  private final int medicCycleSeconds;
  private final int rehydrateFabricatedAheadThreshold;
  private final long healthCycleStalenessThresholdMillis;
  private final NexusWorkImpl.Mode mode;
  private final boolean isRehydrationEnabled;
  private final String envName;
  @Nullable
  private final String shipKey;
  @Nullable
  private final UUID fabricationPreviewTemplatePlaybackId;
  private final String shipBucket;
  private Instant labPollNext;
  private Instant yardPollNext;
  private NexusWorkImpl.State state;
  private MultiStopwatch timer;
  private boolean active = true;
  private boolean allChainsFabricatedAhead = true;
  private long nextCycleMillis = System.currentTimeMillis();
  private long nextJanitorMillis = System.currentTimeMillis();
  private long nextMedicMillis = System.currentTimeMillis();
  private final PreviewNexusAdmin previewNexusAdmin;

  @Autowired
  public NexusWorkImpl(
    AppEnvironment env,
    ChainManager chainManager,
    CraftFactory craftFactory,
    DubFactory dubFactory,
    EntityFactory entityFactory,
    FabricatorFactory fabricatorFactory,
    HttpClientProvider httpClientProvider,
    HubClient hubClient,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider,
    LockProvider lockProvider,
    NexusEntityStore store,
    NotificationProvider notification,
    PreviewNexusAdmin previewNexusAdmin,
    SegmentManager segmentManager,
    TelemetryProvider telemetryProvider
  ) {
    this.chainManager = chainManager;
    this.craftFactory = craftFactory;
    this.dubFactory = dubFactory;
    this.entityFactory = entityFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.httpClientProvider = httpClientProvider;
    this.hubClient = hubClient;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.lockProvider = lockProvider;
    this.notification = notification;
    this.segmentManager = segmentManager;
    this.store = store;
    this.telemetryProvider = telemetryProvider;
    envName = env.getWorkEnvironmentName();

    chainThresholdFabricatedBehindSeconds = env.getFabricationChainThresholdFabricatedBehindSeconds();
    cycleMillis = env.getWorkCycleMillis();
    eraseSegmentsOlderThanSeconds = env.getWorkEraseSegmentsOlderThanSeconds();
    healthCycleStalenessThresholdMillis = env.getWorkHealthCycleStalenessThresholdSeconds() * MILLIS_PER_SECOND;
    ignoreSegmentsOlderThanSeconds = env.getWorkEraseSegmentsOlderThanSeconds();
    ingestCycleSeconds = env.getWorkIngestCycleSeconds();
    janitorCycleSeconds = env.getWorkJanitorCycleSeconds();
    janitorEnabled = env.isWorkJanitorEnabled();
    isRehydrationEnabled = env.isWorkRehydrationEnabled();
    labPollSeconds = env.getWorkLabHubLabPollSeconds();
    medicCycleSeconds = env.getWorkMedicCycleSeconds();
    medicEnabled = env.isWorkMedicEnabled();
    rehydrateFabricatedAheadThreshold = env.getWorkRehydrateFabricatedAheadThreshold();
    shipBaseUrl = env.getShipBaseUrl();
    shipKey = env.getShipKey();
    shipBucket = env.getShipBucket();
    yardPollSeconds = env.getWorkYardPollSeconds();
    this.previewNexusAdmin = previewNexusAdmin;

    labPollNext = Instant.now();
    yardPollNext = Instant.now();
    mode = Strings.isNullOrEmpty(shipKey) ? Lab : NexusWorkImpl.Mode.Yard;
    state = NexusWorkImpl.State.Init;

    fabricationPreviewTemplatePlaybackId = env.getFabricationPreviewTemplatePlaybackId().orElse(null);

    // Telemetry: # Segments Erased
    METRIC_FABRICATED_AHEAD_SECONDS = telemetryProvider.gauge("fabricated_ahead_seconds", "Fabricated Ahead Seconds", "s");
    METRIC_SEGMENT_CREATED = telemetryProvider.count("segment_created", "Segment Created", "");
    METRIC_SEGMENT_GC = telemetryProvider.count("segment_gc", "Segment Garbage Collected", "");
  }

  @Override
  public void start() {
    timer = MultiStopwatch.start();
    LOG.info("Will start Nexus");
    while (active) {
      try {
        runCycle();
      } catch (InterruptedException e) {
        LOG.warn("Nexus interrupted!", e);
        active = false;
      }
    }
  }

  @Override
  public void finish() {
    active = false;
    LOG.info("Did stop Nexus");
  }

  /**
   * This is the internal cycle that's run indefinitely
   */
  private void runCycle() throws InterruptedException {
    if (System.currentTimeMillis() < nextCycleMillis) {
      Thread.sleep(INTERNAL_CYCLE_SLEEP_MILLIS);
      return;
    };
    nextCycleMillis = System.currentTimeMillis() + cycleMillis;

    // Action based on state and mode
    try {
      switch (state) {
        case Init -> {
          switch (mode) {
            case Lab -> state = State.Active;
            case Yard -> loadYard();
          }
        }
        case Loading -> {
          // no op
        }
        case Active -> {
          switch (mode) {
            case Lab -> runLab();
            case Yard -> runYard();
          }
        }
      }
      if (medicEnabled) doMedic();
      if (janitorEnabled) doJanitor();

    } catch (Exception e) {
      didFailWhile(null, "running a work cycle", e, true);
    }

    // End lap & do telemetry on all fabricated chains
    timer.lap();
    LOG.info("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
    nextCycleMillis = System.currentTimeMillis() + cycleMillis;
  }

  /**
   * Run all work when this Nexus is a sidecar to a hub, as in the Lab
   */
  private void runLab() {
    if (Instant.now().isAfter(labPollNext)) {
      state = State.Loading;
      labPollNext = Instant.now().plusSeconds(labPollSeconds);
      if (maintainPreviewTemplate())
        state = State.Active;
      else
        state = State.Fail;
    }

    // Replace an empty list so there is no possibility of nonexistence
    Collection<Chain> activeChains = Lists.newArrayList();
    try {
      activeChains.addAll(new ArrayList<>(chainManager.readManyInState(ChainState.FABRICATE)));
    } catch (ManagerFatalException | ManagerPrivilegeException e) {
      didFailWhile(null, "getting the list of active chain IDs", e, true);
      return;
    }

    // Fabricate all active chains
    for (Chain chain : activeChains) {
      ingestMaterialIfNecessary(chain);
      try {
        fabricateChain(chain);
      } catch (FabricationFatalException e) {
        didFailWhile(chain.getShipKey(), "fabricating", e, false);
        try {
          chainManager.destroy(chain.getId());
        } catch (Exception e2) {
          didFailWhile(chain.getShipKey(), "destroying stale chain", e2, true);
          state = State.Fail;
          return;
        }
      }
    }
  }

  /**
   * Load static content to run yard fabrication
   * <p>
   * Nexus production fabrication from static source (without Hub) https://www.pivotaltracker.com/story/show/177020318
   */
  private void loadYard() {
    try {
      var material = hubClient.load(shipKey);
      material.setTemplateShipKey(shipKey);
      var chain = createChainForTemplate(material.getTemplate())
        .orElseThrow(() -> new HubClientException(String.format("Failed to create chain for Template[%s]", shipKey)));
      chainSourceMaterial.put(chain.getId(), material);
      LOG.debug("Ingested {} entities of source material for Chain[{}]", material.size(), Chains.getIdentifier(chain));
      state = State.Active;

    } catch (HubClientException e) {
      didFailWhile(shipKey, "ingesting source material from Hub", e, false);
      state = State.Fail;
    }
  }

  /**
   * Run all work when this Nexus is in production, as in the Yard
   */
  private void runYard() throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
    if (Instant.now().isAfter(yardPollNext)) {
      yardPollNext = Instant.now().plusSeconds(yardPollSeconds);
      loadYard();
    }

    try {
      fabricateChain(chainManager.readOneByShipKey(shipKey));
    } catch (FabricationFatalException e) {
      didFailWhile(shipKey, "fabricating", e, false);
      state = State.Fail;
    }
  }

  /**
   * Ingest Content from Hub
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
      didFailWhile(chain.getShipKey(), "ingesting source material from Hub", e, false);
    }
  }

  /**
   * Engineer wants platform heartbeat to check for any stale production chains in fabricate state, https://www.pivotaltracker.com/story/show/158897383
   * and if found, send back a failure health check it in order to ensure the Chain remains in an operable state.
   * <p>
   * Medic relies on precomputed  telemetry of fabrication latency https://www.pivotaltracker.com/story/show/177021797
   */
  private void doMedic() {
    if (System.currentTimeMillis() < nextMedicMillis) return;
    nextMedicMillis = System.currentTimeMillis() + (medicCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Medic");

    try {
      var fabricatingChains = chainManager.readManyInState(ChainState.FABRICATE);
      List<UUID> stalledChainIds = Lists.newArrayList();
      LOG.info("Medic will check {} fabricating {}",
        fabricatingChains.size(), 1 < fabricatingChains.size() ? "Chains" : "Chain");
      fabricatingChains
        .stream()
        .filter((chain) ->
          TemplateType.Production.toString().equals(chain.getType().toString()))
        .forEach(chain -> {
          var aheadSeconds = Strings.isNullOrEmpty(chain.getFabricatedAheadAt()) ? 0 : Math.floor(Values.computeRelativeSeconds(Instant.parse(chain.getFabricatedAheadAt())));
          if (aheadSeconds < chainThresholdFabricatedBehindSeconds) {
            LOG.warn("Chain {} is stalled, fabricated ahead {}s", Chains.getIdentifier(chain), aheadSeconds);
            stalledChainIds.add(chain.getId());
          }
        });
      allChainsFabricatedAhead = stalledChainIds.isEmpty();
      LOG.info("Total elapsed time: {}", timer.totalsToString());

    } catch (ManagerFatalException | ManagerPrivilegeException e) {
      didFailWhile(null, "checking & reviving all", e, true);
    }
  }

  /**
   * Do the work-- this is called by the underlying WorkerImpl run() hook
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
      didFailWhile(null, "checking for segments to erase", e, true);
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

    telemetryProvider.put(METRIC_SEGMENT_GC, (long) gcSegIds.size());
  }

  /**
   * Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void fabricateChain(Chain target) throws FabricationFatalException {
    try {
      // On Nexus start, generate a random hash key and write it to a lock file named after the ship key, next to the target chain output json-- e.g. **bump_deep.lock** is written next to the target chain output **bump_deep.json**
      // https://www.pivotaltracker.com/story/show/185119448
      lockProvider.acquire(shipBucket, target.getShipKey());

      timer.section("ComputeAhead");
      var fabricatedAheadAt = computeFabricatedAheadAt(target);
      double aheadSeconds = Math.floor(Values.computeRelativeSeconds(fabricatedAheadAt));
      var chain = chainManager.update(target.getId(), target.fabricatedAheadAt(Values.formatIso8601UTC(fabricatedAheadAt)));
      telemetryProvider.put(METRIC_FABRICATED_AHEAD_SECONDS, aheadSeconds);

      var templateConfig = chainManager.getTemplateConfig(chain.getId());
      if (aheadSeconds > templateConfig.getBufferAheadSeconds()) return;

      timer.section("BuildNext");
      Optional<Segment> nextSegment = chainManager.buildNextSegmentOrCompleteTheChain(chain,
        Instant.now().plusSeconds(templateConfig.getBufferAheadSeconds()),
        Instant.now().minusSeconds(templateConfig.getBufferAheadSeconds()));
      if (nextSegment.isEmpty()) return;

      Segment segment = segmentManager.create(nextSegment.get());
      LOG.debug("Created Segment {}", segment);
      telemetryProvider.put(METRIC_SEGMENT_CREATED, 1L);

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
      aheadSeconds += segmentLengthSeconds;
      fabricatedAheadAt = fabricatedAheadAt.plusNanos((long) (segmentLengthSeconds * NANOS_PER_SECOND));
      chain = chainManager.update(chain.getId(), chain.fabricatedAheadAt(Values.formatIso8601UTC(fabricatedAheadAt)));
      telemetryProvider.put(METRIC_FABRICATED_AHEAD_SECONDS, aheadSeconds);

      // Before writing a segment, Nexus reads the expected lock file and confirms that the hash has not changed.
      // https://www.pivotaltracker.com/story/show/185119448
      lockProvider.check(shipBucket, target.getShipKey());

      timer.section("Ship");
      doDubShipWork(fabricator);
      finishWork(fabricator, segment);

      LOG.info("Chain[{}] offset={} Segment[{}] ahead {}s fabricated OK",
        Chains.getIdentifier(chain),
        segment.getOffset(),
        Segments.getIdentifier(segment),
        aheadSeconds);

    } catch (ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException |
             ManagerFatalException | NexusException | ValueException e) {
      var body = String.format("Failed to create Segment of Chain[%s] (%s) because %s\n\n%s",
        Chains.getIdentifier(target),
        target.getType(),
        e.getMessage(),
        Text.formatStackTrace(e));

      notification.publish(String.format("%s-Chain[%s] Failure",
        target.getType(),
        Chains.getIdentifier(target)), body
      );

      LOG.error("Failed to created Segment in Chain[{}] reason={}", Chains.getIdentifier(target), e.getMessage());

    } catch (LockException e) {
      LOG.error("Failed to acquire fabrication lock for Chain[{}] reason={}", Chains.getIdentifier(target), e.getMessage());
      active = false;
    }
  }

  /**
   * Finish work on Segment
   *
   * @param fabricator to craft
   * @param segment    fabricating
   * @throws NexusException on failure
   */
  private void finishWork(Fabricator fabricator, Segment segment) throws NexusException {
    updateSegmentState(fabricator, segment, SegmentState.DUBBING, SegmentState.DUBBED);
    LOG.debug("[segId={}] Worked for {} seconds", segment.getId(), fabricator.getElapsedSeconds());
  }

  /**
   * Craft a Segment, or fail
   *
   * @param fabricator to craft
   * @param segment    fabricating
   * @throws NexusException on configuration failure
   * @throws NexusException on craft failure
   */
  private Segment doCraftWork(Fabricator fabricator, Segment segment) throws NexusException {
    var updated = updateSegmentState(fabricator, segment, SegmentState.PLANNED, SegmentState.CRAFTING);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.beat(fabricator).doWork();
    craftFactory.hook(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
    craftFactory.percLoop(fabricator).doWork();
    craftFactory.transition(fabricator).doWork();
    craftFactory.background(fabricator).doWork();
    return updated;
  }

  /**
   * Dub a Segment, or fail
   *
   * @param fabricator to dub master
   * @param segment    fabricating
   * @return updated Segment
   * @throws NexusException on craft failure
   * @throws NexusException on dub failure
   */
  protected Segment doDubMasterWork(Fabricator fabricator, Segment segment) throws NexusException {
    var updated = updateSegmentState(fabricator, segment, SegmentState.CRAFTING, SegmentState.DUBBING);
    dubFactory.master(fabricator).doWork();
    return updated;
  }

  /**
   * Ship a Segment, or fail
   *
   * @param fabricator to ship
   * @throws NexusException on craft failure
   * @throws NexusException on ship failure
   */
  protected void doDubShipWork(Fabricator fabricator) throws NexusException {
    dubFactory.ship(fabricator).doWork();
  }

  /**
   * Log and of segment message of error that job failed while (message)@param shipKey  (optional) ship key
   *
   * @param msgWhile      phrased like "Doing work"
   * @param e             exception (optional)
   * @param logStackTrace whether to show the whole stack trace in logs
   */
  private void didFailWhile(@Nullable String shipKey, String msgWhile, Exception e, boolean logStackTrace) {
    var msgCause = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    if (logStackTrace)
      LOG.error("Failed while {} because {}", msgWhile, msgCause, e);
    else
      LOG.error("Failed while {} because {}", msgWhile, msgCause);

    notification.publish(
      Strings.isNullOrEmpty(shipKey) ? String.format("%s-Chain[%s] Work Failure", envName, shipKey) : String.format("%s-Chains Work Failure", envName),
      String.format("Failed while %s because %s\n\n%s", msgWhile, msgCause, Text.formatStackTrace(e)));
  }

  /**
   * Update Segment to Working state
   *
   * @param fabricator to update
   * @param fromState  of existing segment
   * @param toState    of new segment
   * @return updated Segment
   * @throws NexusException if record is invalid
   */
  private Segment updateSegmentState(Fabricator fabricator, Segment segment, SegmentState fromState, SegmentState toState) throws NexusException {
    if (fromState != segment.getState())
      throw new NexusException(String.format("Segment[%s] %s requires Segment must be in %s state.", segment.getId(), toState, fromState));
    var seg = fabricator.getSegment();
    seg.setState(toState);
    fabricator.putSegment(seg);
    LOG.debug("[segId={}] Segment transitioned to state {} OK", segment.getId(), toState);
    return fabricator.getSegment();
  }


  /**
   * Whether this Segment is before a given threshold, first by end-at if available, else begin-at
   *
   * @param eraseBefore threshold to filter before
   * @return true if segment is before threshold
   */
  protected boolean isBefore(Segment segment, Instant eraseBefore) {
    if (Values.isSet(segment.getEndAt())) return Instant.parse(segment.getEndAt()).isBefore(eraseBefore);
    if (Values.isSet(segment.getBeginAt())) return Instant.parse(segment.getBeginAt()).isBefore(eraseBefore);
    return false;
  }

  /**
   * Get the IDs of all Segments that we ought to erase
   *
   * @return list of IDs of Segments we ought to erase
   */
  private Collection<UUID> getSegmentIdsToErase() throws NexusException {
    Instant eraseBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    Collection<UUID> segmentIds = Lists.newArrayList();
    for (UUID chainId : store.getAllChains().stream()
      .flatMap(Entities::flatMapIds).toList())
      store.getAllSegments(chainId)
        .stream()
        .filter(segment -> isBefore(segment, eraseBefore))
        .flatMap(Entities::flatMapIds)
        .forEach(segmentIds::add);
    return segmentIds;
  }

  @Override
  public boolean isHealthy() {
    return allChainsFabricatedAhead
      && !State.Fail.equals(state)
      && nextCycleMillis > System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

  /**
   * chain shows current fabrication latency https://www.pivotaltracker.com/story/show/177072936
   *
   * @param chain fabricating
   */
  private Instant computeFabricatedAheadAt(Chain chain) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException {
    return Chains.computeFabricatedAheadAt(chain, segmentManager.readMany(ImmutableList.of(chain.getId())));
  }

  /**
   * Maintain a single preview template by id
   * If we find no reason to perform work, we return false.
   * Returning false ultimately gracefully terminates the instance
   * https://www.pivotaltracker.com/story/show/185119448
   *
   * @return true if all is well, false if something has failed
   */
  private boolean maintainPreviewTemplate() {
    Optional<TemplatePlayback> templatePlayback = readPreviewTemplatePlayback();
    if (templatePlayback.isEmpty()) {
      LOG.debug("No preview template playback found");
      selfDestructPreviewTemplate();
      return false;
    }

    Optional<Template> template = readPreviewTemplate(templatePlayback.get());
    if (template.isEmpty()) {
      LOG.debug("No preview template found");
      selfDestructPreviewTemplate();
      return false;
    }

    // Keep track of all fabricating chains
    // probably a relic of prior architecture but at this time it stays to change as little as possible
    // https://www.pivotaltracker.com/story/show/183576743
    Collection<Chain> allFab;
    try {
      allFab = chainManager.readAllFabricating();
    } catch (ManagerFatalException | ManagerPrivilegeException e) {
      LOG.error("Failed to retrieve all fabrication Chain(s) because {}", e.getMessage());
      selfDestructPreviewTemplate();
      return false;
    }

    try {
      Set<String> chainShipKeys = allFab.stream().map(Chain::getShipKey).collect(Collectors.toSet());
      if (!Strings.isNullOrEmpty(template.get().getShipKey()) && !chainShipKeys.contains(template.get().getShipKey()))
        createChainForTemplate(template.get())
          .orElseThrow(() -> new ManagerFatalException(String.format("Failed to create chain for Template[%s]", Templates.getIdentifier(template.get()))));
    } catch (ManagerFatalException e) {
      LOG.error("Failed to start Chain(s) for playing Template(s) because {}", e.getMessage());
      selfDestructPreviewTemplate();
      return false;
    }

    return true;
  }

  /**
   * Self-destruct the Preview Template
   */
  private void selfDestructPreviewTemplate() {
    if (Objects.isNull(fabricationPreviewTemplatePlaybackId)) return;
    try {
      LOG.info("Will self-destruct stale TemplatePlayback[{}]", fabricationPreviewTemplatePlaybackId);
      active = false;
      var templatePlayback = new TemplatePlayback();
      templatePlayback.setId(fabricationPreviewTemplatePlaybackId);
      previewNexusAdmin.stopPreviewNexus(templatePlayback);
    } catch (ServiceException e) {
      LOG.error("Failed to self-destruct stale TemplatePlayback[{}] because {}", fabricationPreviewTemplatePlaybackId, e.getMessage());
    }
  }

  /**
   * Read preview Template from Hub
   *
   * @param playback TemplatePlayback for which to get Template
   * @return preview Template
   */
  private Optional<Template> readPreviewTemplate(TemplatePlayback playback) {
    try {
      return hubClient.readPreviewTemplate(playback.getTemplateId());
    } catch (HubClientException e) {
      LOG.error("Failed to read preview Template[{}] from Hub because {}", playback.getTemplateId(), e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Read preview TemplatePlayback from Hub
   *
   * @return preview TemplatePlayback
   */
  private Optional<TemplatePlayback> readPreviewTemplatePlayback() {
    if (Objects.isNull(fabricationPreviewTemplatePlaybackId)) return Optional.empty();
    try {
      return hubClient.readPreviewTemplatePlayback(fabricationPreviewTemplatePlaybackId);
    } catch (HubClientException e) {
      LOG.error("Failed to read preview TemplatePlayback[{}] from Hub because {}", fabricationPreviewTemplatePlaybackId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Bootstrap a chain from JSON chain bootstrap data,
   * first rehydrating store from last shipped JSON matching this ship key.
   * <p>
   * Nexus with bootstrap chain rehydrates store on startup from shipped JSON files https://www.pivotaltracker.com/story/show/178718006
   */
  private Optional<Chain> createChainForTemplate(Template template) {
    var chain = rehydrateTemplate(template);

    // If rehydration was successful, return success
    if (chain.isPresent()) return chain;

    // If the template already exists, destroy it
    chainManager.destroyIfExistsForShipKey(template.getShipKey());

    // Only if rehydration was unsuccessful
    try {
      LOG.info("Will bootstrap Template[{}]", Templates.getIdentifier(template));
      return Optional.of(chainManager.bootstrap(template.getType(), Chains.fromTemplate(template)));

    } catch (ManagerFatalException | ManagerPrivilegeException | ManagerValidationException |
             ManagerExistenceException e) {
      LOG.error("Failed to bootstrap Template[{}] because {}", Templates.getIdentifier(template), e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Attempt to rehydrate the store from a bootstrap, and return true if successful, so we can skip other stuff
   *
   * @param template from which to rehydrate
   * @return chain if the rehydration was successful
   */
  private Optional<Chain> rehydrateTemplate(Template template) {
    if (!isRehydrationEnabled) return Optional.empty();
    var success = new AtomicBoolean(true);
    Collection<Object> entities = com.google.api.client.util.Lists.newArrayList();
    JsonapiPayload chainPayload;
    Chain chain;

    String key = Chains.getShipKey(Chains.getFullKey(template.getShipKey()), EXTENSION_JSON);

    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, key)))
    ) {
      if (!Objects.equals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode())) {
        LOG.error("Failed to get previously fabricated chain for Template[{}] because {} {}", template.getShipKey(), response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        return Optional.empty();
      }

      LOG.debug("will check for last shipped data");
      chainPayload = jsonProvider.getMapper().readValue(response.getEntity().getContent(), JsonapiPayload.class);
      chain = jsonapiPayloadFactory.toOne(chainPayload);
      entities.add(entityFactory.clone(chain));
    } catch (JsonapiException | ClassCastException | IOException | EntityException e) {
      LOG.error("Failed to rehydrate previously fabricated chain for Template[{}] because {}", template.getShipKey(), e.getMessage());
      return Optional.empty();
    }

    try {
      LOG.info("Will load Chain[{}] for ship key \"{}\"", chain.getId(), template.getShipKey());
      chainPayload.getIncluded().stream()
        .filter(po -> po.isType(TemplateBinding.class))
        .forEach(templateBinding -> {
          try {
            entities.add(entityFactory.clone(jsonapiPayloadFactory.toOne(templateBinding)));
          } catch (JsonapiException | EntityException | ClassCastException e) {
            success.set(false);
            LOG.error("Could not deserialize TemplateBinding from shipped Chain JSON because {}", e.getMessage());
          }
        });

      Instant ignoreBefore = Instant.now().minusSeconds(ignoreSegmentsOlderThanSeconds);
      //noinspection DuplicatedCode
      chainPayload.getIncluded().parallelStream()
        .filter(po -> po.isType(Segment.class))
        .flatMap(po -> {
          try {
            return Stream.of((Segment) jsonapiPayloadFactory.toOne(po));
          } catch (JsonapiException | ClassCastException e) {
            LOG.error("Could not deserialize Segment from shipped Chain JSON because {}", e.getMessage());
            success.set(false);
            return Stream.empty();
          }
        })
        .filter(seg -> SegmentState.DUBBED.equals(seg.getState()))
        .filter(seg -> Instant.parse(seg.getEndAt()).isAfter(ignoreBefore))
        .forEach(segment -> {
          var segmentShipKey = Segments.getStorageFilename(segment.getStorageKey(), EXTENSION_JSON);
          try (
            CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, segmentShipKey)))
          ) {
            if (!Objects.equals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode())) {
              LOG.error("Failed to get segment for Template[{}] because {} {}", template.getShipKey(), response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
              success.set(false);
              return;
            }
            var segmentPayload = jsonProvider.getMapper().readValue(response.getEntity().getContent(), JsonapiPayload.class);
            AtomicInteger childCount = new AtomicInteger();
            entities.add(entityFactory.clone(segment));
            segmentPayload.getIncluded()
              .forEach(po -> {
                try {
                  var entity = jsonapiPayloadFactory.toOne(po);
                  entities.add(entity);
                  childCount.getAndIncrement();
                } catch (Exception e) {
                  LOG.error("Could not deserialize Segment from shipped Chain JSON because {}", e.getMessage());
                  success.set(false);
                }
              });
            LOG.info("Read Segment[{}] and {} child entities", Segments.getIdentifier(segment), childCount);

          } catch (Exception e) {
            LOG.error("Could not load Segment[{}] because {}", Segments.getIdentifier(segment), e.getMessage());
            success.set(false);
          }
        });

      // Quit if anything failed up to here
      if (!success.get()) return Optional.empty();

      // Nexus with bootstrap won't rehydrate stale Chain
      // https://www.pivotaltracker.com/story/show/178727631
      var aheadSeconds =
        Math.floor(Values.computeRelativeSeconds(Chains.computeFabricatedAheadAt(chain,
          entities.stream()
            .filter(e -> Entities.isType(e, Segment.class))
            .map(e -> (Segment) e)
            .collect(Collectors.toList()))));

      if (aheadSeconds < rehydrateFabricatedAheadThreshold) {
        LOG.info("Will not rehydrate Chain[{}] fabricated ahead {}s (not > {}s)",
          Chains.getIdentifier(chain), aheadSeconds, rehydrateFabricatedAheadThreshold);
        chainManager.destroy(chain.getId());
        return Optional.empty();
      }

      // Okay to rehydrate
      store.putAll(entities);
      LOG.info("Rehydrated {} entities OK. Chain[{}] is fabricated ahead {}s",
        entities.size(), Chains.getIdentifier(chain), aheadSeconds);
      return Optional.of(chain);

    } catch (NexusException | ManagerFatalException | ManagerPrivilegeException | ManagerExistenceException e) {
      LOG.error("Failed to rehydrate store!", e);
      return Optional.empty();
    }
  }

  enum Mode {
    Lab,
    Yard,
  }

  enum State {
    Init,
    Loading,
    Active,
    Fail,
  }
}
