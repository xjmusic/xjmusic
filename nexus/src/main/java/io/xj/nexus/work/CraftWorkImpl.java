// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.Template;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.audio.AudioCache;
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
import io.xj.nexus.persistence.SegmentUtils;
import io.xj.nexus.persistence.TemplateUtils;
import io.xj.nexus.telemetry.Telemetry;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
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

import static io.xj.hub.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;

public class CraftWorkImpl implements CraftWork {
  private static final Logger LOG = LoggerFactory.getLogger(CraftWorkImpl.class);
  private static final String TIMER_SECTION_CRAFT = "Craft";
  private static final String TIMER_SECTION_CRAFT_CACHE = "CraftCache";
  private static final String TIMER_SECTION_CRAFT_CLEANUP = "CraftCleanup";
  private final Telemetry telemetry;
  private final CraftFactory craftFactory;
  private final FabricatorFactory fabricatorFactory;
  private final NexusEntityStore store;
  private final AudioCache audioCache;
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final double outputFrameRate;
  private final int outputChannels;

  private final long craftAheadMicros;
  private final TemplateConfig templateConfig;
  private final Chain chain;
  private final long mixerLengthMicros;
  private final long persistenceWindowMicros;
  private final HubContent content;
  private final AtomicBoolean nextCycleRewrite = new AtomicBoolean(false);
  private final AtomicReference<Program> nextCycleOverrideMacroProgram = new AtomicReference<>();
  private final AtomicReference<Collection<String>> nextCycleOverrideMemes = new AtomicReference<>();
  private final AtomicBoolean didOverride = new AtomicBoolean(false);

  public CraftWorkImpl(
    Telemetry telemetry,
    CraftFactory craftFactory,
    FabricatorFactory fabricatorFactory,
    NexusEntityStore store,
    AudioCache audioCache,
    long persistenceWindowSeconds,
    long craftAheadSeconds,
    long mixerLengthSeconds,
    double outputFrameRate,
    int outputChannels,
    HubContent content
  ) {
    this.telemetry = telemetry;
    this.craftFactory = craftFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.audioCache = audioCache;
    this.outputChannels = outputChannels;
    this.outputFrameRate = outputFrameRate;
    this.store = store;

    craftAheadMicros = craftAheadSeconds * MICROS_PER_SECOND;
    mixerLengthMicros = mixerLengthSeconds * MICROS_PER_SECOND;
    persistenceWindowMicros = persistenceWindowSeconds * MICROS_PER_SECOND;
    this.content = content;

    // Telemetry: # Segments Erased
    chain = createChainForTemplate(content.getTemplates().stream().findFirst()
      .orElseThrow(() -> new RuntimeException("Failed to obtain template from content")))
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

    var currentSegments = store.readAllSegmentsSpanning(fromChainMicros, toChainMicros);
    if (currentSegments.isEmpty()) {
      return List.of();
    }
    var previousSegment = store.readSegment(currentSegments.get(0).getId() - 1);
    var nextSegment = store.readSegment(currentSegments.get(currentSegments.size() - 1).getId() + 1);
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
    var currentSegment = store.readSegmentAtChainMicros(chainMicros);
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
    var currentSegment = store.readSegment(offset);
    if (currentSegment.isEmpty() || currentSegment.get().getState() != SegmentState.CRAFTED) {
      return Optional.empty();
    }
    return currentSegment;
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPicks(List<Segment> segments) throws NexusException {
    return store.readPicks(segments);
  }

  @Override
  public Instrument getInstrument(InstrumentAudio audio) {
    return content.getInstrument(audio.getInstrumentId())
      .orElseThrow(() -> new RuntimeException("Failed to get Instrument[" + audio.getInstrumentId() + "]"));
  }

  @Override
  public InstrumentAudio getInstrumentAudio(SegmentChoiceArrangementPick pick) {
    return content.getInstrumentAudio(pick.getInstrumentAudioId())
      .orElseThrow(() -> new RuntimeException("Failed to get InstrumentAudio[" + pick.getInstrumentAudioId() + "]"));
  }

  @Override
  public boolean isMuted(SegmentChoiceArrangementPick pick) {
    try {
      var segment = store.readSegment(pick.getSegmentId())
        .orElseThrow(() -> new NexusException("Failed to get Segment[" + pick.getSegmentId() + "]"));
      var arrangement = store.read(segment.getId(), SegmentChoiceArrangement.class, pick.getSegmentChoiceArrangementId());
      if (arrangement.isEmpty()) {
        return false;
      }
      var choice = store.read(segment.getId(), SegmentChoice.class, arrangement.get().getSegmentChoiceId());
      return choice.isPresent() ? choice.get().getMute() : false;

    } catch (NexusException e) {
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
    var chain = getChain();
    if (chain.isEmpty()) {
      return Optional.empty();
    }
    var mainChoice = store.readAll(segment.getId(), SegmentChoice.class).stream().filter(choice -> ProgramType.Main.equals(choice.getProgramType())).findFirst();
    if (mainChoice.isEmpty()) {
      return Optional.empty();
    }
    return content.getProgram(mainChoice.get().getProgramId());

  }

  @Override
  public Optional<Program> getMacroProgram(Segment segment) {
    var chain = getChain();
    if (chain.isEmpty()) {
      return Optional.empty();
    }
    var macroChoice = store.readAll(segment.getId(), SegmentChoice.class).stream().filter(choice -> ProgramType.Macro.equals(choice.getProgramType())).findFirst();
    if (macroChoice.isEmpty()) {
      return Optional.empty();
    }
    return content.getProgram(macroChoice.get().getProgramId());
  }

  @Override
  public HubContent getSourceMaterial() {
    return content;
  }

  @Override
  public Optional<Long> getCraftedToChainMicros() {
    try {
      return store.readAllSegments().stream().filter(segment -> SegmentState.CRAFTED.equals(segment.getState())).max(Comparator.comparing(Segment::getId)).map(SegmentUtils::getEndAtChainMicros);
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
      doFabrication(dubbedToChainMicros, shippedToChainMicros + craftAheadMicros);
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
    return !nextCycleRewrite.get();
  }

  @Override
  public void doOverrideMacro(Program macroProgram) {
    LOG.info("Next craft cycle, will override macro with {}", macroProgram.getName());
    nextCycleOverrideMacroProgram.set(macroProgram);
    doNextCycleRewriteUnlessInitialSegment();
  }

  @Override
  public void doOverrideMemes(Collection<String> memes) {
    LOG.info("Next craft cycle, will override memes with {}", StringUtils.toProperCsvAnd(memes.stream().sorted().toList()));
    nextCycleOverrideMemes.set(memes);
    doNextCycleRewriteUnlessInitialSegment();
  }

  @Override
  public boolean getAndResetDidOverride() {
    return didOverride.getAndSet(false);
  }

  /**
   If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
   the first segment should be governed by that selection
   https://www.pivotaltracker.com/story/show/187381427
   */
  private void doNextCycleRewriteUnlessInitialSegment() {
    if (0 < store.getSegmentCount())
      nextCycleRewrite.set(true);
  }

  /**
   Fabricate the chain based on craft state
   <p>
   Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub live performance modulation https://www.pivotaltracker.com/story/show/186003440

   @param dubbedToChainMicros already dubbed to here
   @param craftToChainMicros  target to craft until
   @throws FabricationFatalException if the chain cannot be fabricated
   */
  private void doFabrication(long dubbedToChainMicros, long craftToChainMicros) throws FabricationFatalException {
    if (nextCycleRewrite.get()) {
      doFabricationRewrite(dubbedToChainMicros, nextCycleOverrideMacroProgram.get(), nextCycleOverrideMemes.get());
      nextCycleRewrite.set(false);
    } else {
      doFabricationDefault(craftToChainMicros, nextCycleOverrideMacroProgram.get(), nextCycleOverrideMemes.get());
    }
  }

  /**
   Default behavior is to fabricate the next segment if we are not crafted enough ahead, otherwise skip

   @param toChainMicros to target chain micros
   @throws FabricationFatalException if the chain cannot be fabricated
   */
  private void doFabricationDefault(
    long toChainMicros,
    @Nullable Program overrideMacroProgram,
    @Nullable Collection<String> overrideMemes
  ) throws FabricationFatalException {
    try {
      // currently fabricated AT (vs target fabricated TO)
      long atChainMicros = ChainUtils.computeFabricatedToChainMicros(store.readAllSegments());
      double aheadSeconds = ((double) (atChainMicros - toChainMicros) / MICROS_PER_SECOND);
      if (aheadSeconds > 0) return;

      // Build next segment in chain
      // Get the last segment in the chain
      // If the chain had no last segment, it must be empty; return a template for its first segment
      Segment segment;
      var existing = store.readSegmentLast();
      if (existing.isEmpty()) {
        segment = buildSegmentInitial();
      } else if (Objects.isNull(existing.get().getDurationMicros())) {
        LOG.debug("Last segment in chain has no duration, cannot fabricate next segment");
        return;
      } else {
        segment = buildSegmentFollowing(existing.get());
      }
      segment = store.put(segment);
      doFabricationWork(segment, null, overrideMacroProgram, overrideMemes);

    } catch (
      ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException | ManagerFatalException |
      NexusException | ValueException e
    ) {
      didFailWhile("fabricating", e);
    }
  }

  /**
   Override behavior deletes all future segments and re-fabricates starting with the given parameters
   <p>
   Macro program override
   https://www.pivotaltracker.com/story/show/186003440
   <p>
   Memes override
   https://www.pivotaltracker.com/story/show/186714075

   @param dubbedToChainMicros  already dubbed to here
   @param overrideMacroProgram to override fabrication
   @param overrideMemes        to override fabrication
   @throws FabricationFatalException if the chain cannot be fabricated
   */
  private void doFabricationRewrite(
    long dubbedToChainMicros,
    @Nullable Program overrideMacroProgram,
    @Nullable Collection<String> overrideMemes
  ) throws FabricationFatalException {
    try {
      // Determine the segment we are currently in the middle of dubbing
      var lastSegment = getSegmentAtChainMicros(dubbedToChainMicros);
      if (lastSegment.isEmpty()) {
        LOG.warn("Will not delete any segments because fabrication is already at the end of the known chain.");
        return;
      }

      // Determine whether the current segment can be cut short
      var currentMainProgram = getMainProgram(lastSegment.get());
      if (currentMainProgram.isEmpty()) {
        LOG.warn("Will not delete any segments because current segment has no main program.");
        return;
      }
      var mainProgramConfig = new ProgramConfig(currentMainProgram.get().getConfig());
      var subBeats = mainProgramConfig.getBarBeats() * mainProgramConfig.getCutoffMinimumBars();
      var dubbedToSegmentMicros = dubbedToChainMicros - lastSegment.get().getBeginAtChainMicros();
      var microsPerBeat = (long) (MICROS_PER_MINUTE / currentMainProgram.get().getTempo());
      var dubbedToSegmentBeats = dubbedToSegmentMicros / microsPerBeat;
      var cutoffAfterBeats = subBeats * Math.ceil((double) dubbedToSegmentBeats / subBeats);
      if (cutoffAfterBeats < lastSegment.get().getTotal()) {
        doCutoffLastSegment(lastSegment.get(), cutoffAfterBeats);
      }

      // Delete all segments after the current segment and fabricate the next segment
      LOG.info("Will delete segments after #{} and re-fabricate.", lastSegment.get().getId());
      if (Objects.nonNull(overrideMacroProgram))
        LOG.info("Has macro program override {}", overrideMacroProgram.getName());
      else if (Objects.nonNull(overrideMemes))
        LOG.info("Has meme override {}", StringUtils.toProperCsvAnd(overrideMemes.stream().sorted().toList()));
      else {
        LOG.warn("Neither override memes nor macros are present: unsure what rewrite action to take");
      }
      store.deleteSegmentsAfter(lastSegment.get().getId());
      Segment segment = buildSegmentFollowing(lastSegment.get());
      segment.setType(SegmentType.NEXT_MACRO);
      segment = store.put(segment);
      doFabricationWork(segment, SegmentType.NEXT_MACRO, overrideMacroProgram, overrideMemes);
      didOverride.set(true);

    } catch (
      ManagerPrivilegeException | ManagerExistenceException | ManagerValidationException | ManagerFatalException |
      NexusException | ValueException e
    ) {
      didFailWhile("fabricating", e);
    }
  }

  /**
   Cut the current segment short after the given number of beats

   @param segment          to cut short
   @param cutoffAfterBeats number of beats to cut short after
   */
  private void doCutoffLastSegment(Segment segment, double cutoffAfterBeats) throws NexusException {
    try {
      var durationMicros = cutoffAfterBeats * MICROS_PER_MINUTE / segment.getTempo();
      LOG.info("Will cut current segment short after {} beats.", cutoffAfterBeats);
      segment.setTotal((int) cutoffAfterBeats);
      segment.setDurationMicros((long) (durationMicros));
      store.updateSegment(segment);
      store.readAll(segment.getId(), SegmentChoiceArrangementPick.class)
        .forEach((pick) -> {
          try {
            if (pick.getStartAtSegmentMicros() >= durationMicros)
              store.delete(segment.getId(), SegmentChoiceArrangementPick.class, pick.getId());
            else if (Objects.nonNull(pick.getLengthMicros()) && pick.getStartAtSegmentMicros() + pick.getLengthMicros() > durationMicros)
              store.put(pick.lengthMicros((long) (durationMicros - pick.getStartAtSegmentMicros())));
          } catch (NexusException e) {
            LOG.error("Failed to cut SegmentChoiceArrangementPick[{}] short to {} beats because {}", pick.getId(), cutoffAfterBeats, e.getMessage());
          }
        });

    } catch (Exception e) {
      throw new NexusException(String.format("Failed to cut Segment[%d] short to %f beats", segment.getId(), cutoffAfterBeats), e);
    }
  }

  /**
   Craft a Segment, or fail

   @param segment              to craft
   @param overrideSegmentType  to use for crafting
   @param overrideMacroProgram to override fabrication
   @param overrideMemes        to override fabrication
   @throws NexusException on configuration failure
   @throws NexusException on craft failure
   */
  private void doFabricationWork(
    Segment segment,
    @Nullable SegmentType overrideSegmentType,
    @Nullable Program overrideMacroProgram,
    @Nullable Collection<String> overrideMemes
  ) throws NexusException, ManagerFatalException, ValueException, FabricationFatalException {
    LOG.debug("[segId={}] will prepare fabricator", segment.getId());
    Fabricator fabricator = fabricatorFactory.fabricate(content, segment.getId(), outputFrameRate, outputChannels, overrideSegmentType);

    LOG.debug("[segId={}] will do craft work", segment.getId());
    updateSegmentState(fabricator, segment, SegmentState.PLANNED, SegmentState.CRAFTING);
    craftFactory.macroMain(fabricator, overrideMacroProgram, overrideMemes).doWork();
    craftFactory.beat(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
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
      .ifPresent(segment -> store.deleteSegmentsBefore(segment.getId()));
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
   Log and send notification of error that job failed while (message)

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    LOG.error("Failed while {} because {}\n{}", msgWhile, e.getMessage(), StringUtils.formatStackTrace(e));
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
    fabricator.updateSegment(seg);
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
