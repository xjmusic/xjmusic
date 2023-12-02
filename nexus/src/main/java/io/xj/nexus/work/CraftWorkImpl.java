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
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

public class CraftWorkImpl implements CraftWork {
  private static final Logger LOG = LoggerFactory.getLogger(CraftWorkImpl.class);
  private static final String TIMER_SECTION_CRAFT = "Craft";
  private static final String TIMER_SECTION_CRAFT_CACHE = "CraftAudioPrecache";
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

  public CraftWorkImpl(
    Telemetry telemetry,
    CraftFactory craftFactory,
    FabricatorFactory fabricatorFactory,
    SegmentManager segmentManager,
    NexusEntityStore store,
    AudioCache audioCache,
    HubContent sourceMaterial,
    long craftAheadMicros,
    double outputFrameRate,
    int outputChannels
  ) {
    this.telemetry = telemetry;
    this.craftFactory = craftFactory;
    this.craftAheadMicros = craftAheadMicros;
    this.fabricatorFactory = fabricatorFactory;
    this.audioCache = audioCache;
    this.outputChannels = outputChannels;
    this.outputFrameRate = outputFrameRate;
    this.segmentManager = segmentManager;
    this.sourceMaterial = sourceMaterial;
    this.store = store;

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
  public void runCycle(long shippedToChainMicros) {
    if (!running.get()) return;

    try {
      long startedAtMillis = System.currentTimeMillis();
      fabricateChain(chain, shippedToChainMicros + craftAheadMicros);
      telemetry.record(TIMER_SECTION_CRAFT, System.currentTimeMillis() - startedAtMillis);

      startedAtMillis = System.currentTimeMillis();
      doAudioCacheMaintenance(shippedToChainMicros);
      telemetry.record(TIMER_SECTION_CRAFT_CACHE, System.currentTimeMillis() - startedAtMillis);

    } catch (Exception e) {
      didFailWhile("running craft work", e);
    }
  }

  /**
   Engineer wants platform heartbeat to check for any stale production chains in fabricate state, https://www.pivotaltracker.com/story/show/158897383
   and if found, send back a failure health check it in order to ensure the Chain remains in an operable state.
   <p>
   Medic relies on precomputed  telemetry of fabrication latency https://www.pivotaltracker.com/story/show/177021797
   */
  void doAudioCacheMaintenance(long shippedToChainMicros) throws NexusException {
    // Poke the audio cache to load all known-to-be-upcoming audio to cache; this is a no-op for already-cache audio
    var segments = getSegmentsIfReady(shippedToChainMicros, shippedToChainMicros);
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
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void fabricateChain(Chain target, long toChainMicros) throws FabricationFatalException {
    try {
      // currently fabricated AT (vs target fabricated TO)
      var atChainMicros = ChainUtils.computeFabricatedToChainMicros(segmentManager.readAll());

      double aheadSeconds = ((double) (atChainMicros - toChainMicros) / MICROS_PER_SECOND);

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
      didFailWhile("fabricating", e);
    }
  }

  /**
   Build the next segment in the chain

   @param target chain
   @return segment
   @throws ManagerFatalException     if the segment cannot be created
   @throws ManagerExistenceException if the segment cannot be created
   @throws ManagerPrivilegeException if the segment cannot be created
   */
  private Optional<Segment> buildNextSegment(Chain target) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
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
    if (Objects.isNull(maybeLastSegmentInChain.get().getDurationMicros())) {
      return Optional.empty();
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
      LOG.warn("Failed to bootstrap Template[{}] because {}", TemplateUtils.getIdentifier(template), e.getMessage());
      return Optional.empty();
    }
  }
}
