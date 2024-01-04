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
import io.xj.nexus.NexusException;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.FabricationFatalException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.ChainUtils;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.ManagerValidationException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentUtils;
import io.xj.nexus.persistence.TemplateUtils;
import io.xj.nexus.telemetry.Telemetry;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

public class CraftWorkImpl implements CraftWork {
  private static final Logger LOG = LoggerFactory.getLogger(CraftWorkImpl.class);
  private static final String TIMER_SECTION_CRAFT = "Craft";
  private static final String TIMER_SECTION_CRAFT_CACHE = "CraftCache";
  private static final String TIMER_SECTION_CRAFT_CLEANUP = "CraftCleanup";
  private final Telemetry telemetry;
  private final CraftFactory craftFactory;
  private final FabricatorFactory fabricatorFactory;
  private final HubContent sourceMaterial;
  private final NexusEntityStore store;
  private final SegmentManager segmentManager;
  private final AudioCache audioCache;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final double outputFrameRate;
  private final int outputChannels;

  private final long craftAheadMicros;
  private final TemplateConfig templateConfig;
  private final Chain chain;
  private final long mixerLengthMicros;
  private final long persistenceWindowMicros;

  // Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub
  // Workstation has live performance modulation https://www.pivotaltracker.com/story/show/186003440
  private final AtomicReference<CraftState> craftState = new AtomicReference<>(CraftState.INITIAL);
  private final AtomicReference<Program> nextMacroProgram = new AtomicReference<>();
  private final AtomicReference<Segment> lastDubbedSegment = new AtomicReference<>();

  public CraftWorkImpl(
    Telemetry telemetry,
    CraftFactory craftFactory,
    FabricatorFactory fabricatorFactory,
    SegmentManager segmentManager,
    NexusEntityStore store,
    AudioCache audioCache,
    HubContent sourceMaterial,
    long persistenceWindowSeconds,
    long craftAheadSeconds,
    long mixerLengthSeconds,
    double outputFrameRate,
    int outputChannels
  ) {
    this.telemetry = telemetry;
    this.craftFactory = craftFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.audioCache = audioCache;
    this.outputChannels = outputChannels;
    this.outputFrameRate = outputFrameRate;
    this.segmentManager = segmentManager;
    this.sourceMaterial = sourceMaterial;
    this.store = store;

    craftAheadMicros = craftAheadSeconds * MICROS_PER_SECOND;
    mixerLengthMicros = mixerLengthSeconds * MICROS_PER_SECOND;
    persistenceWindowMicros = persistenceWindowSeconds * MICROS_PER_SECOND;

    // Telemetry: # Segments Erased
    chain = createChainForTemplate(sourceMaterial.getTemplate())
      .orElseThrow(() -> new RuntimeException("Failed to create chain"));

    try {
      templateConfig = Objects.nonNull(chain) ? (new TemplateConfig(chain.getTemplateConfig())) : null;
    } catch (ValueException e) {
      throw new RuntimeException("Could not start craft without template config", e);
    }

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
  public TemplateConfig getTemplateConfig() {
    return templateConfig;
  }

  @Override
  public List<Segment> getSegmentsIfReady(Long fromChainMicros, Long toChainMicros) {
    // require chain
    var chain = getChain();
    if (chain.isEmpty()) {
      return List.of();
    }

    var currentSegments = segmentManager.readAllSpanning(fromChainMicros, toChainMicros);
    if (currentSegments.isEmpty()) {
      return List.of();
    }
    var previousSegment = segmentManager.readOneById(currentSegments.get(0).getId() - 1);
    var nextSegment = segmentManager.readOneById(currentSegments.get(currentSegments.size() - 1).getId() + 1);
    return Stream.concat(Stream.concat(previousSegment.stream(), currentSegments.stream()), nextSegment.stream())
      .filter(segment -> SegmentState.CRAFTED.equals(segment.getState()))
      .toList();
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
  public Instrument getInstrument(InstrumentAudio audio) {
    return sourceMaterial.getInstrument(audio.getInstrumentId())
      .orElseThrow(() -> new RuntimeException("Failed to get Instrument[" + audio.getInstrumentId() + "]"));
  }

  @Override
  public InstrumentAudio getInstrumentAudio(SegmentChoiceArrangementPick pick) {
    return sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId())
      .orElseThrow(() -> new RuntimeException("Failed to get InstrumentAudio[" + pick.getInstrumentAudioId() + "]"));
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
      LOG.warn("Unable to get crafted-to chain micros because {}", e.getMessage());
      return Optional.empty();
    }
  }

  /**
   This is the internal cycle that's run indefinitely
   */
  public void runCycle(long shippedToChainMicros, long dubbedToChainMicros) {
    if (!running.get()) return;

    try {
      long startedAtMillis = System.currentTimeMillis();
      fabricateTo(shippedToChainMicros + craftAheadMicros);
      telemetry.record(TIMER_SECTION_CRAFT, System.currentTimeMillis() - startedAtMillis);

      startedAtMillis = System.currentTimeMillis();
      doAudioCacheMaintenance(Math.min(shippedToChainMicros, dubbedToChainMicros), Math.max(shippedToChainMicros, dubbedToChainMicros) + mixerLengthMicros);
      telemetry.record(TIMER_SECTION_CRAFT_CACHE, System.currentTimeMillis() - startedAtMillis);

      startedAtMillis = System.currentTimeMillis();
      doSegmentCleanup(shippedToChainMicros);
      telemetry.record(TIMER_SECTION_CRAFT_CLEANUP, System.currentTimeMillis() - startedAtMillis);
    } catch (FabricationFatalException e) {
      LOG.warn("Failed to fabricate because {}", e.getMessage());

    } catch (Exception e) {
      didFailWhile("running craft work", e);
    }
  }

  @Override
  public boolean isReady() {
    return Objects.equals(craftState.get(), CraftState.READY);
  }

  @Override
  public void gotoMacroProgram(Program macroProgram, long dubbedToChainMicros) {
    craftState.set(CraftState.GOTO_MACRO);
    nextMacroProgram.set(macroProgram);
    lastDubbedSegment.set(getSegmentAtChainMicros(dubbedToChainMicros).orElse(null));
  }

  /**
   Fabricate the chain based on craft state

   @param toChainMicros to target chain micros
   @throws FabricationFatalException if the chain cannot be fabricated
   */
  private void fabricateTo(long toChainMicros) throws FabricationFatalException {
    switch (craftState.get()) {
      case INITIAL, READY -> fabricateDefault(toChainMicros);
      case GOTO_MACRO -> fabricateOverrideMacro();
    }

    // Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub
    // Workstation has live performance modulation https://www.pivotaltracker.com/story/show/186003440
    craftState.set(CraftState.READY);
  }

  /**
   Default behavior is to fabricate the next segment if we are not crafted enough ahead, otherwise skip

   @param toChainMicros to target chain micros
   @throws FabricationFatalException if the chain cannot be fabricated
   */
  public void fabricateDefault(long toChainMicros) throws FabricationFatalException {
    try {
      // currently fabricated AT (vs target fabricated TO)
      long atChainMicros = ChainUtils.computeFabricatedToChainMicros(segmentManager.readAll());
      double aheadSeconds = ((double) (atChainMicros - toChainMicros) / MICROS_PER_SECOND);
      if (aheadSeconds > 0) return;

      // Build next segment in chain
      // Get the last segment in the chain
      // If the chain had no last segment, it must be empty; return a template for its first segment
      Segment segment;
      var existing = segmentManager.readLastSegment();
      if (existing.isEmpty()) {
        segment = buildSegmentInitial();
      } else if (Objects.isNull(existing.get().getDurationMicros())) {
        LOG.debug("Last segment in chain has no duration, cannot fabricate next segment");
        return;
      } else {
        segment = buildSegmentFollowing(existing.get());
      }
      segment = segmentManager.create(segment);
      doCraftWork(segment, null, null);

    } catch (
      ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException | ManagerFatalException |
      NexusException | ValueException e
    ) {
      didFailWhile("fabricating", e);
    }
  }

  /**
   Macro override behavior deletes all future segments and re-fabricates starting with the given macro program

   @throws FabricationFatalException if the chain cannot be fabricated
   */
  private void fabricateOverrideMacro() throws FabricationFatalException {
    try {
      LOG.info("Will delete segments after #{} and craft using macro-program {}",
        lastDubbedSegment.get().getId(), nextMacroProgram.get().getName());
      segmentManager.deleteSegmentsAfter(lastDubbedSegment.get().getId());
      Segment segment = buildSegmentFollowing(lastDubbedSegment.get());
      segment.setType(SegmentType.NEXT_MACRO);
      segment = segmentManager.create(segment);
      lastDubbedSegment.set(null);
      doCraftWork(segment, nextMacroProgram.get(), SegmentType.NEXT_MACRO);
      nextMacroProgram.set(null);

    } catch (
      ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException | ManagerFatalException |
      NexusException | ValueException e
    ) {
      didFailWhile("fabricating", e);
    }
  }

  /**
   Create the initial template segment

   @return initial template segment
   */
  private Segment buildSegmentInitial() {
    Segment segment = new Segment();
    segment.setId(0);
    segment.setChainId(chain.getId());
    segment.setBeginAtChainMicros(0L);
    segment.setDelta(0);
    segment.setType(SegmentType.PENDING);
    segment.setState(SegmentState.PLANNED);
    return segment;
  }

  /**
   Create the next segment in the chain, following the last segment

   @param last segment
   @return next segment
   @throws ManagerFatalException      on a fatal error
   @throws ManagerValidationException on a validation error
   @throws ManagerExistenceException  if the segment does not exist
   @throws ManagerPrivilegeException  if access is prohibited
   */
  private Segment buildSegmentFollowing(Segment last) throws ManagerFatalException, ManagerValidationException, ManagerExistenceException, ManagerPrivilegeException {
    Segment segment = new Segment();
    segment.setId(last.getId() + 1);
    segment.setChainId(chain.getId());
    segment.setBeginAtChainMicros(last.getBeginAtChainMicros() + Objects.requireNonNull(last.getDurationMicros()));
    segment.setDelta(last.getDelta());
    segment.setType(SegmentType.PENDING);
    segment.setState(SegmentState.PLANNED);
    return segment;
  }

  /**
   Craft a Segment, or fail

   @param segment              to craft
   @param overrideMacroProgram to use for crafting
   @param overrideSegmentType  to use for crafting
   @throws NexusException on configuration failure
   @throws NexusException on craft failure
   */
  void doCraftWork(Segment segment, @Nullable Program overrideMacroProgram, @Nullable SegmentType overrideSegmentType) throws NexusException, ManagerFatalException, ValueException, FabricationFatalException {
    LOG.debug("[segId={}] will prepare fabricator", segment.getId());
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment, outputFrameRate, outputChannels, overrideSegmentType);

    LOG.debug("[segId={}] will do craft work", segment.getId());
    updateSegmentState(fabricator, segment, SegmentState.PLANNED, SegmentState.CRAFTING);
    craftFactory.macroMain(fabricator, overrideMacroProgram).doWork();
    craftFactory.beat(fabricator).doWork();
    craftFactory.hook(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
    craftFactory.percLoop(fabricator).doWork();
    craftFactory.transition(fabricator).doWork();
    craftFactory.background(fabricator).doWork();
    LOG.debug("Fabricated Segment[{}]", segment.getId());

    updateSegmentState(fabricator, segment, SegmentState.CRAFTING, SegmentState.CRAFTED);
    LOG.debug("[segId={}] Worked for {} seconds", segment.getId(), String.format("%.2f", (float) fabricator.getElapsedMicros() / MICROS_PER_SECOND));
  }

  /**
   Delete segments before the given shipped-to chain micros

   @param shippedToChainMicros the shipped-to chain micros
   */
  private void doSegmentCleanup(long shippedToChainMicros) {
    getSegmentAtChainMicros(shippedToChainMicros - persistenceWindowMicros)
      .ifPresent(segment -> segmentManager.deleteSegmentsBefore(segment.getId()));
  }

  /**
   Engineer wants platform heartbeat to check for any stale production chains in fabricate state, https://www.pivotaltracker.com/story/show/158897383
   and if found, send back a failure health check it in order to ensure the Chain remains in an operable state.
   <p>
   Medic relies on precomputed  telemetry of fabrication latency https://www.pivotaltracker.com/story/show/177021797
   */
  void doAudioCacheMaintenance(long minChainMicros, long maxChainMicros) throws NexusException {
    // Poke the audio cache to load all known-to-be-upcoming audio to cache; this is a no-op for already-cache audio
    var segments = getSegmentsIfReady(minChainMicros, maxChainMicros);
    Set<UUID> seen = new HashSet<>();
    List<InstrumentAudio> currentAudios = new ArrayList<>();
    getPicks(segments).stream()
      .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStartAtSegmentMicros))
      .map(this::getInstrumentAudio)
      .forEach(audio -> {
        if (!seen.contains(audio.getId())) {
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            currentAudios.add(audio);
          }
          seen.add(audio.getId());
        }
      });
    audioCache.loadTheseAndForgetTheRest(currentAudios);
  }

  /**
   Log and send notification of error that job failed while (message)

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    var msgCause = StringUtils.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
    LOG.error("Failed while {} because {}", msgWhile, msgCause, e);
    running.set(false);
    finish();
  }

  /**
   Update Segment to Working state

   @param fabricator to update
   @param fromState  of existing segment
   @param toState    of new segment
   @throws NexusException if record is invalid
   */
  void updateSegmentState(Fabricator fabricator, Segment segment, SegmentState fromState, SegmentState toState) throws NexusException {
    if (fromState != segment.getState())
      throw new NexusException(String.format("Segment[%s] %s requires Segment must be in %s state.", segment.getId(), toState, fromState));
    var seg = fabricator.getSegment();
    seg.setState(toState);
    fabricator.putSegment(seg);
    LOG.debug("[segId={}] Segment transitioned to state {} OK", segment.getId(), toState);
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
      LOG.warn("Failed to bootstrap Template[{}] because {}", TemplateUtils.getIdentifier(template), e.getMessage());
      return Optional.empty();
    }
  }
}
