// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.InputMode;
import io.xj.nexus.NexusException;
import io.xj.nexus.OutputMode;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.FabricationFatalException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.*;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.hub.util.ValueUtils.MILLIS_PER_SECOND;
import static io.xj.nexus.work.WorkTelemetry.TIMER_SECTION_STANDBY;

public class CraftWorkImpl implements CraftWork {
  static final Logger LOG = LoggerFactory.getLogger(CraftWorkImpl.class);
  public static final String TIMER_SECTION_CRAFT = "Craft";
  private final WorkTelemetry telemetry;
  final CraftFactory craftFactory;
  final EntityFactory entityFactory;
  final FabricatorFactory fabricatorFactory;
  final FileStoreProvider fileStore;
  HubContent sourceMaterial;
  final NexusEntityStore store;
  final SegmentManager segmentManager;


  @Value("${craft.janitor.enabled}")
  boolean janitorEnabled;

  @Value("${craft.medic.enabled}")
  boolean medicEnabled;

  @Value("${craft.chain.threshold.fabricated.behind.seconds}")
  int chainThresholdFabricatedBehindSeconds;

  @Value("${craft.erase.segments.older.than.seconds}")
  int eraseSegmentsOlderThanSeconds;

  @Value("${craft.ignore.segments.older.than.seconds}")
  int ignoreSegmentsOlderThanSeconds;

  @Value("${craft.ingest.cycle.seconds}")
  int ingestCycleSeconds;

  @Value("${craft.janitor.cycle.seconds}")
  int janitorCycleSeconds;

  @Value("${craft.async.poll.seconds}")
  int asyncPollSeconds;

  @Value("${craft.medic.cycle.seconds}")
  int medicCycleSeconds;

  @Value("${craft.health.cycle.staleness.threshold.seconds}")
  long healthCycleStalenessThresholdSeconds;

  @Value("${ship.bucket}")
  String shipBucket;

  final OutputMode outputMode;
  final AtomicBoolean running = new AtomicBoolean(true);
  boolean chainFabricatedAhead = true;
  long nextJanitorMillis = System.currentTimeMillis();
  long nextMedicMillis = System.currentTimeMillis();
  final InputMode inputMode;
  final String tempFilePathPrefix;
  final double outputFrameRate;
  final int outputChannels;
  final String audioBaseUrl;
  final String shipBaseUrl;

  @Nullable
  private TemplateConfig templateConfig;

  @Nullable
  private final Chain chain;

  public CraftWorkImpl(
    WorkTelemetry telemetry,
    CraftFactory craftFactory,
    EntityFactory entityFactory,
    FabricatorFactory fabricatorFactory,
    SegmentManager segmentManager, FileStoreProvider fileStore,
    NexusEntityStore store, HubContent sourceMaterial,
    InputMode inputMode,
    OutputMode outputMode,
    String audioBaseUrl,
    String shipBaseUrl,
    String tempFilePathPrefix,
    double outputFrameRate,
    int outputChannels
  ) {
    this.telemetry = telemetry;
    this.audioBaseUrl = audioBaseUrl;
    this.craftFactory = craftFactory;
    this.entityFactory = entityFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.outputChannels = outputChannels;
    this.outputFrameRate = outputFrameRate;
    this.segmentManager = segmentManager;
    this.shipBaseUrl = shipBaseUrl;
    this.sourceMaterial = sourceMaterial;
    this.store = store;
    this.tempFilePathPrefix = tempFilePathPrefix;

    this.inputMode = inputMode;
    this.outputMode = outputMode;

    // Telemetry: # Segments Erased
    this.fileStore = fileStore;

    chain = createChainForTemplate(sourceMaterial.getTemplate())
      .orElseThrow(() -> new RuntimeException("Failed to create chain"));

    running.set(true);
  }

  @Override
  public void finish() {
    if (!running.get()) return;
    running.set(false);
    LOG.info("Finished");
  }

  @Override
  public Optional<Chain> getChain() {
    return Optional.ofNullable(chain);
  }

  @Override
  public Optional<TemplateConfig> getTemplateConfig() {
    if (Objects.isNull(templateConfig)) {
      try {
        templateConfig = Objects.nonNull(chain) ? (new TemplateConfig(chain.getTemplateConfig())) : null;
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

    return segmentManager.readAll();
  }

  @Override
  public List<Segment> getSegmentsIfReady(Long planFromChainMicros, Long planToChainMicros) {
    // require chain
    var chain = getChain();
    if (chain.isEmpty()) {
      return List.of();
    }

    // require current segment with end-at time and crafted state
    var currentSegments = segmentManager.readAllSpanning(planFromChainMicros, planToChainMicros);
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
    if (Objects.nonNull(firstSegment.getDurationMicros()) && firstSegment.getBeginAtChainMicros() + firstSegment.getDurationMicros() < planToChainMicros) { // todo make sure we don't need the removed bit: + craftAheadSeconds * MICROS_PER_SECOND
      nextSegment = segmentManager.readOneById(currentSegments.get(0).getId() + 1);
      if (nextSegment.isEmpty() || Objects.isNull(nextSegment.get().getDurationMicros()) || !SegmentState.CRAFTED.equals(nextSegment.get().getState())) {
        return List.of();
      }
    }

    // if the beginning of the current segment is after the threshold, require previous segment
    Optional<Segment> previousSegment = Optional.empty();
    if (Objects.nonNull(firstSegment.getDurationMicros()) && firstSegment.getBeginAtChainMicros() + firstSegment.getDurationMicros() < planToChainMicros && currentSegments.get(0).getId() > 0) {  // todo make sure we don't need the removed bit: + craftAheadSeconds * MICROS_PER_SECOND
      previousSegment = segmentManager.readOneById(currentSegments.get(0).getId() - 1);
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
    var currentSegment = segmentManager.readOneAtChainMicros(chainMicros);
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
    var currentSegment = segmentManager.readOneById(offset);
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
    return sourceMaterial.getInstrument(audio.getInstrumentId());
  }

  @Override
  public Optional<InstrumentAudio> getInstrumentAudio(SegmentChoiceArrangementPick pick) {
    return sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId());
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
  public boolean isFinished() {
    return !running.get();
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
      return sourceMaterial.getProgram(mainChoice.get().getProgramId());
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
      return sourceMaterial.getProgram(macroChoice.get().getProgramId());
    } catch (NexusException e) {
      LOG.warn("Unable to get main program for segment[{}] because {}", segment.getId(), e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public HubContent getSourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public Optional<Long> getCraftedToChainMicros() {
    try {
      return store.getAllSegments().stream().filter(segment -> SegmentState.CRAFTED.equals(segment.getState())).max(Comparator.comparing(Segment::getId)).map(SegmentUtils::getEndAtChainMicros);
    } catch (NexusException e) {
      LOG.error("Unable to get crafted-to chain micros because {}", e.getMessage());
      return Optional.empty();
    }
  }

  /**
   This is the internal cycle that's run indefinitely
   */
  public void runCycle(long toChainMicros) {
    if (!running.get()) return;

    try {
      telemetry.markTimerSection(TIMER_SECTION_CRAFT);
      fabricateChain(chain, toChainMicros);
      if (medicEnabled) doMedic(toChainMicros);
      if (janitorEnabled) doJanitor();
      telemetry.markTimerSection(TIMER_SECTION_STANDBY);

    } catch (Exception e) {
      didFailWhile("running a work cycle", e, true);
    }
  }

  /**
   Engineer wants platform heartbeat to check for any stale production chains in fabricate state, https://www.pivotaltracker.com/story/show/158897383
   and if found, send back a failure health check it in order to ensure the Chain remains in an operable state.
   <p>
   Medic relies on precomputed  telemetry of fabrication latency https://www.pivotaltracker.com/story/show/177021797
   */
  void doMedic(long toChainMicros) {
    if (System.currentTimeMillis() < nextMedicMillis) return;
    nextMedicMillis = System.currentTimeMillis() + (medicCycleSeconds * MILLIS_PER_SECOND);

    var chain = getChain();
    if (chain.isEmpty()) {
      return;
    }

    try {
      var lastCraftedSegment = segmentManager.readLastCraftedSegment();
      if (lastCraftedSegment.isEmpty()) {
        chainFabricatedAhead = false;
        return;
      }
      var aheadSeconds = (SegmentUtils.getEndAtChainMicros(lastCraftedSegment.get()) - toChainMicros) / MICROS_PER_SECOND;
      if (aheadSeconds < chainThresholdFabricatedBehindSeconds) {
        LOG.debug("Fabrication is stalled, ahead {}s", aheadSeconds);
        chainFabricatedAhead = false;
        return;
      }

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      throw new RuntimeException(e);
    }

    chainFabricatedAhead = true;
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  protected void doJanitor() {
    if (System.currentTimeMillis() < nextJanitorMillis) return;
    nextJanitorMillis = System.currentTimeMillis() + (janitorCycleSeconds * MILLIS_PER_SECOND);
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void fabricateChain(Chain target, long toChainMicros) throws FabricationFatalException {
    try {
      // currently fabricated AT (vs target fabricated TO)
      var atChainMicros = ChainUtils.computeFabricatedToChainMicros(segmentManager.readAll());

      double aheadSeconds = (double) ((atChainMicros - toChainMicros) / MICROS_PER_SECOND);

      var templateConfig = getTemplateConfig();
      if (templateConfig.isEmpty()) return;
      if (aheadSeconds > 0) return;

      Optional<Segment> nextSegment = buildNextSegment(target);
      if (nextSegment.isEmpty()) return;

      Segment segment = segmentManager.create(nextSegment.get());
      LOG.debug("Created Segment {}", segment);

      Fabricator fabricator;
      LOG.debug("[segId={}] will prepare fabricator", segment.getId());
      fabricator = fabricatorFactory.fabricate(sourceMaterial, segment, outputFrameRate, outputChannels);

      LOG.debug("[segId={}] will do craft work", segment.getId());
      segment = doCraftWork(fabricator, segment);

      // Update the chain fabricated-ahead seconds before shipping data
      atChainMicros = Objects.nonNull(segment.getDurationMicros()) ? atChainMicros + segment.getDurationMicros() : atChainMicros;
      aheadSeconds = (float) (atChainMicros - toChainMicros) / MICROS_PER_SECOND;

      finishWork(fabricator, segment);

      LOG.debug("Fabricated Segment[offset={}] {}s long (ahead {}s)",
        segment.getId(),
        String.format("%.1f", (float) (Objects.requireNonNull(segment.getDurationMicros()) / MICROS_PER_SECOND)),
        String.format("%.1f", aheadSeconds)
      );

    } catch (
      ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException | ManagerFatalException |
      NexusException | ValueException e
    ) {
      didFailWhile("fabricating", e, false);
    }
  }

  Optional<Segment> buildNextSegment(Chain target) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
    // Get the last segment in the chain
    // If the chain had no last segment, it must be empty; return a template for its first segment
    var maybeLastSegmentInChain = segmentManager.readLastSegment();
    if (maybeLastSegmentInChain.isEmpty()) {
      var seg = new Segment();
      seg.setId(0);
      seg.setChainId(target.getId());
      seg.setBeginAtChainMicros(0L);
      seg.setDelta(0);
      seg.setType(SegmentType.PENDING);
      seg.setState(SegmentState.PLANNED);
      return Optional.of(seg);
    }
    var seg = getSegment(target, maybeLastSegmentInChain.get());
    return Optional.of(seg);
  }

  /**
   Build the template of the segment that follows the last known one

   @param target                  chain
   @param maybeLastSegmentInChain last segment in chain
   @return segment
   */
  private static Segment getSegment(Chain target, Segment maybeLastSegmentInChain) {
    var seg = new Segment();
    seg.setId(maybeLastSegmentInChain.getId() + 1);
    seg.setChainId(target.getId());
    seg.setBeginAtChainMicros(maybeLastSegmentInChain.getBeginAtChainMicros() + Objects.requireNonNull(maybeLastSegmentInChain.getDurationMicros()));
    seg.setDelta(maybeLastSegmentInChain.getDelta());
    seg.setType(SegmentType.PENDING);
    seg.setState(SegmentState.PLANNED);
    return seg;
  }

  /**
   Finish work on Segment

   @param fabricator to craft
   @param segment    fabricating
   @throws NexusException on failure
   */
  void finishWork(Fabricator fabricator, Segment segment) throws NexusException {
    updateSegmentState(fabricator, segment, SegmentState.CRAFTING, SegmentState.CRAFTED);
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

   @param msgWhile      phrased like "Doing work"
   @param e             exception (optional)
   @param logStackTrace whether to show the whole stack trace in logs
   */
  void didFailWhile(String msgWhile, Exception e, boolean logStackTrace) {
    var msgCause = StringUtils.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    if (logStackTrace)
      LOG.error("Failed while {} because {}", msgWhile, msgCause, e);
    else
      LOG.error("Failed while {} because {}", msgWhile, msgCause);

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
    fabricator.putSegment(seg);
    LOG.debug("[segId={}] Segment transitioned to state {} OK", segment.getId(), toState);
    return fabricator.getSegment();
  }

  /**
   Bootstrap a chain from JSON chain bootstrap data.
   */
  Optional<Chain> createChainForTemplate(Template template) {
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
}
