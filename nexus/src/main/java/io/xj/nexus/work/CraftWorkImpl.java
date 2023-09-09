// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityUtils;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.lib.telemetry.TelemetryMeasureCount;
import io.xj.lib.telemetry.TelemetryMeasureGauge;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.InputMode;
import io.xj.nexus.NexusException;
import io.xj.nexus.OutputMode;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.FabricationFatalException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubClientException;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.*;
import jakarta.annotation.Nullable;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.hub.util.ValueUtils.MILLIS_PER_SECOND;

public class CraftWorkImpl implements CraftWork {
  static final Logger LOG = LoggerFactory.getLogger(CraftWorkImpl.class);
  static final String EXTENSION_JSON = "json";
  static final long INTERNAL_CYCLE_SLEEP_MILLIS = 50;
  final CraftFactory craftFactory;
  final EntityFactory entityFactory;
  final FabricatorFactory fabricatorFactory;
  final HttpClientProvider httpClientProvider;
  final HubClient hubClient;
  final HubClientAccess access = HubClientAccess.internal();
  final JsonProvider jsonProvider;
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final FileStoreProvider fileStore;
  HubContent chainSourceMaterial;
  Long chainNextIngestMillis = System.currentTimeMillis();
  final TelemetryMeasureGauge METRIC_FABRICATED_AHEAD_SECONDS;
  final TelemetryMeasureCount METRIC_SEGMENT_CREATED;
  final TelemetryMeasureCount METRIC_SEGMENT_GC;
  final NexusEntityStore store;
  final NotificationProvider notification;
  final SegmentManager segmentManager;
  final TelemetryProvider telemetryProvider;
  @Value("${ship.base.url}")
  String shipBaseUrl;
  @Value("${craft.janitor.enabled}")
  boolean janitorEnabled;
  @Value("${craft.medic.enabled}")
  boolean medicEnabled;
  @Value("${craft.chain.threshold.fabricated.behind.seconds}")
  int chainThresholdFabricatedBehindSeconds;
  @Value("${craft.cycle.millis}")
  int cycleMillis;
  @Value("${craft.erase.segments.older.than.seconds}")
  int eraseSegmentsOlderThanSeconds;
  @Value("${craft.ignore.segments.older.than.seconds}")
  int ignoreSegmentsOlderThanSeconds;
  @Value("${craft.ingest.cycle.seconds}")
  int ingestCycleSeconds;
  @Value("${craft.janitor.cycle.seconds}")
  int janitorCycleSeconds;
  @Value("${craft.sync.poll.seconds}")
  int syncPollSeconds;
  @Value("${craft.async.poll.seconds}")
  int asyncPollSeconds;
  @Value("${craft.medic.cycle.seconds}")
  int medicCycleSeconds;
  @Value("${rehydration.ahead.threshold}")
  int rehydrateFabricatedAheadThreshold;
  @Value("${craft.health.cycle.staleness.threshold.seconds}")
  long healthCycleStalenessThresholdSeconds;
  @Value("${rehydration.enabled}")
  boolean isRehydrationEnabled;
  @Value("${fabrication.preview.template.playback.id}")
  @Nullable
  UUID fabricationPreviewTemplatePlaybackId;
  @Value("${ship.bucket}")
  String shipBucket;
  final OutputMode outputMode;
  long labPollNextSystemMillis;
  long yardPollNextSystemMillis;
  WorkState state = WorkState.Initializing;
  MultiStopwatch timer;
  final AtomicBoolean running = new AtomicBoolean(true);
  boolean chainFabricatedAhead = true;
  long nextCycleMillis = System.currentTimeMillis();
  long nextJanitorMillis = System.currentTimeMillis();
  long nextMedicMillis = System.currentTimeMillis();
  long atChainMicros = 0;
  final InputMode inputMode;
  final String inputTemplateKey;
  final boolean isJsonOutputEnabled;
  final String tempFilePathPrefix;
  final Integer jsonExpiresInSeconds;
  final int bufferAheadSeconds;
  final int bufferBeforeSeconds;
  final double outputFrameRate;
  final int outputChannels;

  @Nullable
  UUID chainId;
  @Nullable
  TemplateConfig templateConfig;

  public CraftWorkImpl(
    CraftFactory craftFactory,
    EntityFactory entityFactory,
    FabricatorFactory fabricatorFactory,
    FileStoreProvider fileStore,
    HttpClientProvider httpClientProvider,
    HubClient hubClient,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider,
    NexusEntityStore store,
    NotificationProvider notification,
    SegmentManager segmentManager,
    TelemetryProvider telemetryProvider,
    InputMode inputMode,
    OutputMode outputMode,
    String inputTemplateKey,
    boolean isJsonOutputEnabled,
    String tempFilePathPrefix,
    int jsonExpiresInSeconds,
    int bufferAheadSeconds,
    int bufferBeforeSeconds,
    double outputFrameRate,
    int outputChannels
  ) {
    this.craftFactory = craftFactory;
    this.entityFactory = entityFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.httpClientProvider = httpClientProvider;
    this.hubClient = hubClient;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.notification = notification;
    this.segmentManager = segmentManager;
    this.store = store;
    this.telemetryProvider = telemetryProvider;
    this.tempFilePathPrefix = tempFilePathPrefix;
    this.inputTemplateKey = inputTemplateKey;
    this.isJsonOutputEnabled = isJsonOutputEnabled;
    this.jsonExpiresInSeconds = jsonExpiresInSeconds;
    this.bufferAheadSeconds = bufferAheadSeconds;
    this.bufferBeforeSeconds = bufferBeforeSeconds;
    this.outputFrameRate = outputFrameRate;
    this.outputChannels = outputChannels;

    labPollNextSystemMillis = System.currentTimeMillis();
    yardPollNextSystemMillis = System.currentTimeMillis();
    this.inputMode = inputMode;
    this.outputMode = outputMode;

    // Telemetry: # Segments Erased
    METRIC_FABRICATED_AHEAD_SECONDS = telemetryProvider.gauge("fabricated_ahead_seconds", "Fabricated Ahead Seconds", "s");
    METRIC_SEGMENT_CREATED = telemetryProvider.count("segment_created", "Segment Created", "");
    METRIC_SEGMENT_GC = telemetryProvider.count("segment_gc", "Segment Garbage Collected", "");
    this.fileStore = fileStore;
  }

  @Override
  public void start() {
    timer = MultiStopwatch.start();
    LOG.info("Will start Nexus");
    while (running.get()) {
      try {
        runCycle();
      } catch (InterruptedException e) {
        LOG.warn("Nexus interrupted!", e);
        running.set(false);
      }
    }
  }

  @Override
  public void finish() {
    if (!running.get()) return;
    running.set(false);
    LOG.info("Finished");
  }

  @Override
  public boolean isHealthy() {
    return chainFabricatedAhead
      && !WorkState.Failed.equals(state)
      && nextCycleMillis > System.currentTimeMillis() - healthCycleStalenessThresholdSeconds * MILLIS_PER_SECOND;
  }

  @Override
  public void setAtChainMicros(long chainMicros) {
    atChainMicros = chainMicros;
  }

  @Override
  public Optional<Chain> getChain() {
    try {
      return store.getChain();
    } catch (NexusException e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<TemplateConfig> getTemplateConfig() {
    if (Objects.isNull(templateConfig)) {
      try {
        var chain = getChain();
        templateConfig = chain.isPresent() ? (new TemplateConfig(chain.get().getTemplateConfig())) : null;
      } catch (ValueException e) {
        LOG.debug("Unable to retrieve template config because {}", e.getMessage());
      }
    }
    return Optional.ofNullable(templateConfig);
  }

  @Override
  public List<Segment> getAllSegments() {
    // require chain
    var chain = getChain();
    if (chain.isEmpty()) {
      return List.of();
    }

    return segmentManager.readAll(chain.get().getId());
  }

  @Override
  public List<Segment> getSegmentsIfReady(Long planFromChainMicros, Long planToChainMicros) {
    // require chain
    var chain = getChain();
    if (chain.isEmpty()) {
      return List.of();
    }

    // require current segment with end-at time and crafted state
    var currentSegments = segmentManager.readAllSpanning(chain.get().getId(), planFromChainMicros, planToChainMicros);
    if (currentSegments.isEmpty() || currentSegments.stream().anyMatch(segment -> !SegmentState.CRAFTED.equals(segment.getState()))) {
      return List.of();
    }

    // If we are already spanning two segments, return them
    if (1 < currentSegments.size()) {
      return currentSegments;
    }
    var firstSegment = Objects.requireNonNull(currentSegments.get(0));

    // if the end of the current segment is before the threshold, require next segment
    Optional<Segment> nextSegment = Optional.empty();
    if (Objects.nonNull(firstSegment.getDurationMicros()) && firstSegment.getBeginAtChainMicros() + firstSegment.getDurationMicros() < planToChainMicros + bufferAheadSeconds * MICROS_PER_SECOND) {
      nextSegment = segmentManager.readOneAtChainOffset(chain.get().getId(), currentSegments.get(0).getId() + 1);
      if (nextSegment.isEmpty() || Objects.isNull(nextSegment.get().getDurationMicros()) || !SegmentState.CRAFTED.equals(nextSegment.get().getState())) {
        return List.of();
      }
    }

    // if the beginning of the current segment is after the threshold, require previous segment
    Optional<Segment> previousSegment = Optional.empty();
    if (Objects.nonNull(firstSegment.getDurationMicros()) && firstSegment.getBeginAtChainMicros() + firstSegment.getDurationMicros() < planToChainMicros + bufferAheadSeconds * MICROS_PER_SECOND && currentSegments.get(0).getId() > 0) {
      previousSegment = segmentManager.readOneAtChainOffset(chain.get().getId(), currentSegments.get(0).getId() - 1);
      if (previousSegment.isEmpty()) {
        return List.of();
      }
    }

    return Stream.of(currentSegments.stream().findFirst(), nextSegment, previousSegment)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
  }

  @Override
  public Optional<Segment> getSegmentAtChainMicros(long chainMicros) {
    // require chain
    var chain = getChain();
    if (chain.isEmpty()) {
      return Optional.empty();
    }

    // require current segment in crafted state
    var currentSegment = segmentManager.readOneAtChainMicros(chain.get().getId(), chainMicros);
    if (currentSegment.isEmpty() || currentSegment.get().getState() != SegmentState.CRAFTED) {
      return Optional.empty();
    }
    return currentSegment;
  }

  @Override
  public Optional<Segment> getSegmentAtOffset(int offset) {
    // require chain
    var chain = getChain();
    if (chain.isEmpty()) {
      return Optional.empty();
    }

    // require current segment in crafted state
    var currentSegment = segmentManager.readOneAtChainOffset(chain.get().getId(), offset);
    if (currentSegment.isEmpty() || currentSegment.get().getState() != SegmentState.CRAFTED) {
      return Optional.empty();
    }
    return currentSegment;
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPicks(List<Segment> segments) throws NexusException {
    return store.getPicks(segments);
  }

  @Override
  public Optional<Instrument> getInstrument(InstrumentAudio audio) {
    return chainSourceMaterial.getInstrument(audio.getInstrumentId());
  }

  @Override
  public Optional<InstrumentAudio> getInstrumentAudio(SegmentChoiceArrangementPick pick) {
    return chainSourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId());
  }

  @Override
  public String getInputTemplateKey() {
    return inputTemplateKey;
  }

  @Override
  public boolean isMuted(SegmentChoiceArrangementPick pick) {
    try {
      var segment = segmentManager.readOne(pick.getSegmentId());
      var arrangement = store.get(segment.getId(), SegmentChoiceArrangement.class, pick.getSegmentChoiceArrangementId());
      if (arrangement.isEmpty()) {
        return false;
      }
      var choice = store.get(segment.getId(), SegmentChoice.class, arrangement.get().getSegmentChoiceId());
      return choice.isPresent() ? choice.get().getMute() : false;
    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException | NexusException e) {
      LOG.warn("Unable to determine if SegmentChoiceArrangementPick[{}] is muted because {}", pick.getId(), e.getMessage());
      return false;
    }
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public Optional<Program> getMainProgram(Segment segment) {
    try {
      var chain = getChain();
      if (chain.isEmpty()) {
        return Optional.empty();
      }
      var mainChoice = store.getAll(segment.getId(), SegmentChoice.class).stream().filter(choice -> ProgramType.Main.equals(choice.getProgramType())).findFirst();
      if (mainChoice.isEmpty()) {
        return Optional.empty();
      }
      return chainSourceMaterial.getProgram(mainChoice.get().getProgramId());
    } catch (NexusException e) {
      LOG.warn("Unable to get main program for segment[{}] because {}", segment.getId(), e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<Program> getMacroProgram(Segment segment) {
    try {
      var chain = getChain();
      if (chain.isEmpty()) {
        return Optional.empty();
      }
      var macroChoice = store.getAll(segment.getId(), SegmentChoice.class).stream().filter(choice -> ProgramType.Macro.equals(choice.getProgramType())).findFirst();
      if (macroChoice.isEmpty()) {
        return Optional.empty();
      }
      return chainSourceMaterial.getProgram(macroChoice.get().getProgramId());
    } catch (NexusException e) {
      LOG.warn("Unable to get main program for segment[{}] because {}", segment.getId(), e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public boolean isFailed() {
    return state == WorkState.Failed;
  }

  @Override
  public HubContent getSourceMaterial() {
    return chainSourceMaterial;
  }

  /**
   This is the internal cycle that's run indefinitely
   */
  void runCycle() throws InterruptedException {
    if (System.currentTimeMillis() < nextCycleMillis) {
      Thread.sleep(INTERNAL_CYCLE_SLEEP_MILLIS);
      return;
    }

    nextCycleMillis = System.currentTimeMillis() + cycleMillis;

    // Action based on state and mode
    try {
      switch (state) {
        case Initializing -> {
          if (InputMode.PREVIEW.equals(inputMode)) {
            state = WorkState.Working;
          } else {
            loadYard();
          }
        }
        case Loading -> {
          // no op
        }
        case Working -> {
          if (InputMode.PREVIEW.equals(inputMode)) {
            runPreview();
          } else {
            runYard();
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
    LOG.debug("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
    nextCycleMillis = System.currentTimeMillis() + cycleMillis;
  }

  /**
   Run all work when this Nexus is a sidecar to a hub, as in the Lab
   */
  void runPreview() {
    if (System.currentTimeMillis() > labPollNextSystemMillis) {
      state = WorkState.Loading;
      labPollNextSystemMillis = System.currentTimeMillis() + syncPollSeconds * MILLIS_PER_SECOND;
      if (maintainPreviewTemplate())
        state = WorkState.Working;
      else
        state = WorkState.Failed;
    }

    // Fabricate active chain
    var chain = getChain();
    if (chain.isEmpty()) {
      return;
    }
    ingestMaterialIfNecessary(chain.get());
    try {
      fabricateChain(chain.get());
    } catch (FabricationFatalException e) {
      didFailWhile(chain.get().getShipKey(), "fabricating", e, false);
    }
  }

  /**
   Load static content to run nexus fabrication
   <p>
   Nexus production fabrication from static source (without Hub) https://www.pivotaltracker.com/story/show/177020318
   */
  void loadYard() {
    try {
      chainSourceMaterial = hubClient.load(inputTemplateKey);
      chainSourceMaterial.setTemplateShipKey(inputTemplateKey);
      chainId = createChainForTemplate(chainSourceMaterial.getTemplate())
        .orElseThrow(() -> new HubClientException(String.format("Failed to create chain for Template[%s]", inputTemplateKey)))
        .getId();

      LOG.debug("Ingested {} entities of source material", chainSourceMaterial.size());
      state = WorkState.Working;

    } catch (Exception e) {
      didFailWhile(inputTemplateKey, "ingesting published source material", e, false);
    }
  }

  /**
   Run all work when this Nexus is in production, as in the Nexus
   */
  void runYard() {
    if (System.currentTimeMillis() > yardPollNextSystemMillis) {
      yardPollNextSystemMillis = System.currentTimeMillis() + asyncPollSeconds * MILLIS_PER_SECOND;
    }

    try {
      var chain = getChain();
      if (chain.isEmpty()) {
        return;
      }
      fabricateChain(chain.get());
    } catch (FabricationFatalException e) {
      didFailWhile(inputTemplateKey, "fabricating", e, false);
      state = WorkState.Failed;
    }
  }

  /**
   Ingest Content from Hub
   */
  void ingestMaterialIfNecessary(Chain chain) {
    if (System.currentTimeMillis() < chainNextIngestMillis) return;
    chainNextIngestMillis = System.currentTimeMillis() + ingestCycleSeconds * MILLIS_PER_SECOND;
    timer.section("Ingest");

    try {
      // read the source material
      chainSourceMaterial = hubClient.ingest(access, chain.getTemplateId());
      LOG.info("Ingested {} entities of source material for Chain[{}]", chainSourceMaterial.size(), ChainUtils.getIdentifier(chain));

    } catch (HubClientException e) {
      didFailWhile(chain.getShipKey(), "ingesting source material from Hub", e, false);
    }
  }

  /**
   Engineer wants platform heartbeat to check for any stale production chains in fabricate state, https://www.pivotaltracker.com/story/show/158897383
   and if found, send back a failure health check it in order to ensure the Chain remains in an operable state.
   <p>
   Medic relies on precomputed  telemetry of fabrication latency https://www.pivotaltracker.com/story/show/177021797
   */
  void doMedic() {
    if (System.currentTimeMillis() < nextMedicMillis) return;
    nextMedicMillis = System.currentTimeMillis() + (medicCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Medic");

    var chain = getChain();
    if (chain.isEmpty()) {
      return;
    }

    try {
      var lastCraftedSegment = segmentManager.readLastCraftedSegment(HubClientAccess.internal(), chainId);
      if (lastCraftedSegment.isEmpty()) {
        chainFabricatedAhead = false;
        return;
      }
      var aheadSeconds = (SegmentUtils.getEndAtChainMicros(lastCraftedSegment.get()) - atChainMicros) / MICROS_PER_SECOND;
      if (aheadSeconds < chainThresholdFabricatedBehindSeconds) {
        LOG.debug("Fabrication is stalled, ahead {}s", aheadSeconds);
        chainFabricatedAhead = false;
        return;
      }

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      throw new RuntimeException(e);
    }

    chainFabricatedAhead = true;
    LOG.debug("Total elapsed time: {}", timer.totalsToString());

  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  protected void doJanitor() {
    if (System.currentTimeMillis() < nextJanitorMillis) return;
    nextJanitorMillis = System.currentTimeMillis() + (janitorCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Janitor");

    // Seek segments to erase
    Collection<Integer> gcSegIds;
    try {
      gcSegIds = getSegmentIdsToErase();
    } catch (NexusException e) {
      didFailWhile(null, "checking for segments to erase", e, true);
      return;
    }

    // Erase segments if necessary
    if (gcSegIds.isEmpty())
      LOG.debug("Found no segments to erase");
    else
      LOG.debug("Will garbage collect {} segments", gcSegIds.size());

    for (Integer segmentId : gcSegIds) {
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
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void fabricateChain(Chain target) throws FabricationFatalException {
    try {
      timer.section("ComputeAhead");
      var fabricatedToChainMicros = ChainUtils.computeFabricatedToChainMicros(segmentManager.readMany(List.of(target.getId())));

      double aheadSeconds = (double) ((fabricatedToChainMicros - atChainMicros) / MICROS_PER_SECOND);
      telemetryProvider.put(METRIC_FABRICATED_AHEAD_SECONDS, aheadSeconds);

      var templateConfig = getTemplateConfig();
      if (templateConfig.isEmpty()) return;
      if (aheadSeconds > bufferAheadSeconds) return;

      timer.section("BuildNext");
      Optional<Segment> nextSegment = buildNextSegment(target);
      if (nextSegment.isEmpty()) return;

      Segment segment = segmentManager.create(nextSegment.get());
      LOG.debug("Created Segment {}", segment);
      telemetryProvider.put(METRIC_SEGMENT_CREATED, 1L);

      Fabricator fabricator;
      timer.section("Prepare");
      LOG.debug("[segId={}] will prepare fabricator", segment.getId());
      fabricator = fabricatorFactory.fabricate(chainSourceMaterial, segment, bufferAheadSeconds, bufferBeforeSeconds, outputFrameRate, outputChannels);

      timer.section("Craft");
      LOG.debug("[segId={}] will do craft work", segment.getId());
      segment = doCraftWork(fabricator, segment);

      // Update the chain fabricated-ahead seconds before shipping data
      fabricatedToChainMicros = Objects.nonNull(segment.getDurationMicros()) ? fabricatedToChainMicros + segment.getDurationMicros() : fabricatedToChainMicros;
      aheadSeconds = (float) (fabricatedToChainMicros - atChainMicros) / MICROS_PER_SECOND;
      telemetryProvider.put(METRIC_FABRICATED_AHEAD_SECONDS, aheadSeconds);

      finishWork(fabricator, segment);

      LOG.info("Fabricated Segment[offset={}] {}s long (ahead {}s)",
        segment.getId(),
        String.format("%.1f", (float) (Objects.requireNonNull(segment.getDurationMicros()) / MICROS_PER_SECOND)),
        String.format("%.1f", aheadSeconds)
      );

    } catch (
      ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException | ManagerFatalException |
      NexusException | ValueException | HubClientException e
    ) {
      var body = String.format("Failed to create Segment of Chain[%s] (%s) because %s\n\n%s",
        ChainUtils.getIdentifier(target),
        target.getType(),
        e.getMessage(),
        StringUtils.formatStackTrace(e));

      notification.publish(String.format("%s-Chain[%s] Failure",
        target.getType(),
        ChainUtils.getIdentifier(target)), body
      );

      LOG.error("Failed to created Segment in Chain[{}] reason={}", ChainUtils.getIdentifier(target), e.getMessage());
    }
  }

  Optional<Segment> buildNextSegment(Chain target) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
    // Get the last segment in the chain
    // If the chain had no last segment, it must be empty; return a template for its first segment
    var maybeLastSegmentInChain = segmentManager.readLastSegment(target.getId());
    if (maybeLastSegmentInChain.isEmpty()) {
      var seg = new Segment();
      seg.setId(123);
      seg.setChainId(target.getId());
      seg.setBeginAtChainMicros(0L);
      seg.setDelta(0);
      seg.setType(SegmentType.PENDING);
      seg.setState(SegmentState.PLANNED);
      return Optional.of(seg);
    }
    var lastSegmentInChain = maybeLastSegmentInChain.get();

    // Build the template of the segment that follows the last known one
    var seg = new Segment();
    seg.setId(456);
    seg.setChainId(target.getId());
    seg.setBeginAtChainMicros(lastSegmentInChain.getBeginAtChainMicros() + Objects.requireNonNull(lastSegmentInChain.getDurationMicros()));
    seg.setDelta(lastSegmentInChain.getDelta());
    seg.setType(SegmentType.PENDING);
    seg.setState(SegmentState.PLANNED);
    return Optional.of(seg);
  }

  /**
   Finish work on Segment

   @param fabricator to craft
   @param segment    fabricating
   @throws NexusException on failure
   */
  void finishWork(Fabricator fabricator, Segment segment) throws NexusException {
    updateSegmentState(fabricator, segment, SegmentState.CRAFTING, SegmentState.CRAFTED);
    doWriteJsonOutputs(fabricator);
    LOG.debug("[segId={}] Worked for {} seconds", segment.getId(), String.format("%.2f", (float) fabricator.getElapsedMicros() / MICROS_PER_SECOND));
  }

  /**
   Craft a Segment, or fail

   @param fabricator to craft
   @param segment    fabricating
   @throws NexusException on configuration failure
   @throws NexusException on craft failure
   */
  Segment doCraftWork(Fabricator fabricator, Segment segment) throws NexusException {
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
   Log and send notification of error that job failed while (message)

   @param templateKey   (optional) ship key
   @param msgWhile      phrased like "Doing work"
   @param e             exception (optional)
   @param logStackTrace whether to show the whole stack trace in logs
   */
  void didFailWhile(@Nullable String templateKey, String msgWhile, Exception e, boolean logStackTrace) {
    var msgCause = StringUtils.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    if (logStackTrace)
      LOG.error("Failed while {} because {}", msgWhile, msgCause, e);
    else
      LOG.error("Failed while {} because {}", msgWhile, msgCause);

    notification.publish(
      StringUtils.isNullOrEmpty(templateKey) ? String.format("Chain[%s] Work Failure", templateKey) : "Chains Work Failure",
      String.format("Failed while %s because %s\n\n%s", msgWhile, msgCause, StringUtils.formatStackTrace(e)));

    state = WorkState.Failed;
    running.set(false);
    finish();
  }

  /**
   Update Segment to Working state

   @param fabricator to update
   @param fromState  of existing segment
   @param toState    of new segment
   @return updated Segment
   @throws NexusException if record is invalid
   */
  Segment updateSegmentState(Fabricator fabricator, Segment segment, SegmentState fromState, SegmentState toState) throws NexusException {
    if (fromState != segment.getState())
      throw new NexusException(String.format("Segment[%s] %s requires Segment must be in %s state.", segment.getId(), toState, fromState));
    var seg = fabricator.getSegment();
    seg.setState(toState);
    seg.setUpdatedNow();
    fabricator.putSegment(seg);
    LOG.debug("[segId={}] Segment transitioned to state {} OK", segment.getId(), toState);
    return fabricator.getSegment();
  }


  /**
   Whether this Segment is before a given threshold, first by end-at if available, else begin-at

   @param eraseBeforeChainMicros threshold to filter before
   @return true if segment is before threshold
   */
  protected boolean isBefore(Segment segment, Long eraseBeforeChainMicros) {
    if (Objects.nonNull(segment.getDurationMicros()))
      return segment.getBeginAtChainMicros() + segment.getDurationMicros() < eraseBeforeChainMicros;
    if (Objects.nonNull(segment.getBeginAtChainMicros()))
      return segment.getBeginAtChainMicros() < eraseBeforeChainMicros;
    return false;
  }

  /**
   Get the IDs of all Segments that we ought to erase

   @return list of IDs of Segments we ought to erase
   */
  Collection<Integer> getSegmentIdsToErase() throws NexusException {
    Long eraseBeforeChainMicros = atChainMicros - eraseSegmentsOlderThanSeconds * MICROS_PER_SECOND;
    return store.getAllSegments()
      .stream()
      .filter(segment -> isBefore(segment, eraseBeforeChainMicros))
      .map(Segment::getId)
      .toList();
  }

  /**
   Maintain a single preview template by id
   If we find no reason to perform work, we return false.
   Returning false ultimately gracefully terminates the instance
   https://www.pivotaltracker.com/story/show/185119448

   @return true if all is well, false if something has failed
   */
  boolean maintainPreviewTemplate() {
    Optional<TemplatePlayback> templatePlayback = readPreviewTemplatePlayback();
    if (templatePlayback.isEmpty()) {
      LOG.debug("No preview template playback found");
      return false;
    }

    Optional<Template> template = readPreviewTemplate(templatePlayback.get());
    if (template.isEmpty()) {
      LOG.debug("No preview template found");
      return false;
    }

    try {
      if (Objects.isNull(chainId)) {
        chainId = createChainForTemplate(template.get())
          .orElseThrow(() -> new ManagerFatalException(String.format("Failed to create chain for Template[%s]", TemplateUtils.getIdentifier(template.get()))))
          .getId();
      }
    } catch (ManagerFatalException e) {
      LOG.error("Failed to start Chain(s) for playing Template(s) because {}", e.getMessage());
      return false;
    }

    return true;
  }

  /**
   Read preview Template from Hub

   @param playback TemplatePlayback for which to get Template
   @return preview Template
   */
  Optional<Template> readPreviewTemplate(TemplatePlayback playback) {
    try {
      return hubClient.readPreviewTemplate(playback.getTemplateId());
    } catch (HubClientException e) {
      LOG.error("Failed to read preview Template[{}] from Hub because {}", playback.getTemplateId(), e.getMessage());
      return Optional.empty();
    }
  }

  /**
   Read preview TemplatePlayback from Hub

   @return preview TemplatePlayback
   */
  Optional<TemplatePlayback> readPreviewTemplatePlayback() {
    if (Objects.isNull(fabricationPreviewTemplatePlaybackId)) return Optional.empty();
    try {
      return hubClient.readPreviewTemplatePlayback(fabricationPreviewTemplatePlaybackId);
    } catch (HubClientException e) {
      LOG.error("Failed to read preview TemplatePlayback[{}] from Hub because {}", fabricationPreviewTemplatePlaybackId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   Bootstrap a chain from JSON chain bootstrap data,
   first rehydrating store from last shipped JSON matching this ship key.
   <p>
   Nexus with bootstrap chain rehydrates store on startup from shipped JSON files https://www.pivotaltracker.com/story/show/178718006
   */
  Optional<Chain> createChainForTemplate(Template template) {
    var rehydrated = rehydrateTemplate(template);
    if (rehydrated.isPresent()) return rehydrated;

    // Only if rehydration was unsuccessful
    try {
      LOG.info("Will bootstrap Template[{}]", TemplateUtils.getIdentifier(template));
      var entity = ChainUtils.fromTemplate(template);
      entity.setState(ChainState.FABRICATE);
      entity.setId(UUID.randomUUID());
      return Optional.ofNullable(store.put(entity));

    } catch (NexusException e) {
      LOG.error("Failed to bootstrap Template[{}] because {}", TemplateUtils.getIdentifier(template), e.getMessage());
      return Optional.empty();
    }
  }

  /**
   Attempt to rehydrate the store from a bootstrap, and return true if successful, so we can skip other stuff

   @param template from which to rehydrate
   @return chain if the rehydration was successful
   */
  Optional<Chain> rehydrateTemplate(Template template) {
    if (!isRehydrationEnabled) return Optional.empty();
    if (outputMode.isLocal()) return Optional.empty();
    var success = new AtomicBoolean(true);
    Collection<Object> entities = new ArrayList<>();
    JsonapiPayload chainPayload;
    Chain chain;

    String key = ChainUtils.getShipKey(ChainUtils.getFullKey(template.getShipKey()), EXTENSION_JSON);

    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, key)))
    ) {
      if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode())) {
        LOG.debug("Failed to get previously fabricated chain for Template[{}] because {} {}", template.getShipKey(), response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
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

      Long ignoreBeforeChainMicros = atChainMicros - ignoreSegmentsOlderThanSeconds * MICROS_PER_SECOND;
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
        .filter(seg -> SegmentState.CRAFTED.equals(seg.getState()))
        .filter(seg -> seg.getBeginAtChainMicros() > ignoreBeforeChainMicros)
        .forEach(segment -> {
          var segmentShipKey = SegmentUtils.getStorageFilename(segment, EXTENSION_JSON);
          try (
            CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, segmentShipKey)))
          ) {
            if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode())) {
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
            LOG.info("Read Segment[{}] and {} child entities", SegmentUtils.getIdentifier(segment), childCount);

          } catch (Exception e) {
            LOG.error("Could not load Segment[{}] because {}", SegmentUtils.getIdentifier(segment), e.getMessage());
            success.set(false);
          }
        });

      // Quit if anything failed up to here
      if (!success.get()) return Optional.empty();

      // Nexus with bootstrap won't rehydrate stale Chain
      // https://www.pivotaltracker.com/story/show/178727631
      var aheadSeconds =
        Math.floor(ChainUtils.computeFabricatedToChainMicros(
          entities.stream()
            .filter(e -> EntityUtils.isType(e, Segment.class))
            .map(e -> (Segment) e)
            .collect(Collectors.toList())) - atChainMicros) / MICROS_PER_SECOND;

      if (aheadSeconds < rehydrateFabricatedAheadThreshold) {
        LOG.info("Will not rehydrate Chain[{}] fabricated ahead {}s (not > {}s)",
          ChainUtils.getIdentifier(chain), aheadSeconds, rehydrateFabricatedAheadThreshold);
        return Optional.empty();
      }

      // Okay to rehydrate
      store.putAll(entities);
      LOG.info("Rehydrated {} entities OK. Chain[{}] is fabricated ahead {}s (> {}s)",
        entities.size(), ChainUtils.getIdentifier(chain), aheadSeconds, rehydrateFabricatedAheadThreshold);
      return Optional.of(chain);

    } catch (NexusException e) {
      LOG.error("Failed to rehydrate store!", e);
      return Optional.empty();
    }
  }

  /**
   MasterDub implements Mixer module to write JSON outputs

   @throws NexusException on error
   */
  void doWriteJsonOutputs(Fabricator fabricator) throws NexusException {
    if (!isJsonOutputEnabled) return;
    writeJsonFile(
      fabricator.getSegmentJson(),
      SegmentUtils.getStorageFilename(fabricator.getSegment(), EXTENSION_JSON));
    writeJsonFile(
      fabricator.getChainFullJson(),
      ChainUtils.getShipKey(ChainUtils.getFullKey(ChainUtils.computeBaseKey(fabricator.getChain())), EXTENSION_JSON));
    writeJsonFile(
      fabricator.getChainJson(atChainMicros),
      ChainUtils.getShipKey(ChainUtils.computeBaseKey(fabricator.getChain()), EXTENSION_JSON));
  }

  /**
   Write json content to file

   @param json     content
   @param filename to write
   */
  void writeJsonFile(String json, String filename) {
    var bytes = json.getBytes();
    if (outputMode == OutputMode.HLS) {
      try {
        fileStore.putS3ObjectFromString(new String(bytes), shipBucket, filename, ContentType.APPLICATION_JSON.toString(), jsonExpiresInSeconds);
        LOG.info("Uploaded {} bytes to {}", bytes.length, filename);
      } catch (Exception e) {
        LOG.error("Error writing {} bytes to {}", bytes.length, filename, e);
      }
    } else {
      var path = computeTempFilePath(filename);
      try {
        Files.write(Paths.get(path), bytes);
        LOG.info("Wrote {} bytes to {}", bytes.length, path);
      } catch (Exception e) {
        LOG.error("Error writing {} bytes to {}", bytes.length, path, e);
      }
    }
  }

  String computeTempFilePath(String filename) {
    return String.format("%s%s", tempFilePathPrefix, filename);
  }

}
