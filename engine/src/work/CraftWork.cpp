// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#include "xjmusic/work/CraftWork.h"
#include "xjmusic/util/ValueUtils.h"
#include "xjmusic/fabricator/TemplateUtils.h"
#include "xjmusic/fabricator/ChainUtils.h"

using namespace XJ;


CraftWork::CraftWork(
    CraftFactory *craftFactory,
    FabricatorFactory *fabricatorFactory,
    SegmentEntityStore *store,
    ContentEntityStore *content,
    long persistenceWindowSeconds,
    long craftAheadSeconds
) {
  this->craftFactory = craftFactory;
  this->fabricatorFactory = fabricatorFactory;
  this->outputChannels = outputChannels;
  this->outputFrameRate = outputFrameRate;
  this->store = store;

  craftAheadMicros = craftAheadSeconds * ValueUtils::MICROS_PER_SECOND;
  persistenceWindowMicros = persistenceWindowSeconds * ValueUtils::MICROS_PER_SECOND;
  this->content = content;

  // Telemetry: # Segments Erased
  if (content->getTemplates().empty())
    throw std::runtime_error("Cannot initialize CraftWork without Templates");

  auto tmpl = *content->getTemplates().begin();
  chain = createChainForTemplate(tmpl);

    templateConfig = TemplateConfig(chain->templateConfig);

  running= true;
}

void CraftWork::finish() {
  if (!running.get()) return;
  running.set(false);
  spdlog::info("Finished");
}

std::optional<Chain> CraftWork::getChain() {
  return std::optional(chain);
}

TemplateConfig CraftWork::getTemplateConfig() {
  return templateConfig;
}

std::vector<Segment> CraftWork::getSegmentsIfReady(Long fromChainMicros, Long toChainMicros) {
  // require chain
  auto chain = getChain();
  if (chain.isEmpty()) {
    return std::vector.of();
  }

  auto currentSegments = store->readAllSegmentsSpanning(fromChainMicros, toChainMicros);
  if (currentSegments.isEmpty()) {
    return std::vector.of();
  }
  auto previousSegment = store->readSegment(currentSegments.get(0).getId() - 1);
  auto nextSegment = store->readSegment(currentSegments.get(currentSegments.size() - 1).getId() + 1);
  return Stream.concat(Stream.concat(previousSegment.stream(), currentSegments.stream()), nextSegment.stream())
      .filter(segment->SegmentState.CRAFTED.equals(segment.getState()))
      .toList();
}

std::optional<Segment> CraftWork::getSegmentAtChainMicros(long chainMicros) {
  // require chain
  auto chain = getChain();
  if (chain.isEmpty()) {
    return std::nullopt;
  }

  // require current segment in crafted state
  auto currentSegment = store->readSegmentAtChainMicros(chainMicros);
  if (currentSegment.isEmpty() || currentSegment.get().getState() != SegmentState.CRAFTED) {
    return std::nullopt;
  }
  return currentSegment;
}

std::optional<Segment> CraftWork::getSegmentAtOffset(int offset) {
  // require chain
  auto chain = getChain();
  if (chain.isEmpty()) {
    return std::nullopt;
  }

  // require current segment in crafted state
  auto currentSegment = store->readSegment(offset);
  if (currentSegment.isEmpty() || currentSegment.get().getState() != SegmentState.CRAFTED) {
    return std::nullopt;
  }
  return currentSegment;
}

std::vector<SegmentChoiceArrangementPick> CraftWork::getPicks(std::vector<Segment> segments) {
  return store->readPicks(segments);
}

Instrument CraftWork::getInstrument(InstrumentAudio audio) {
  return content->getInstrument(audio.getInstrumentId())
      .orElseThrow(()->
  new RuntimeException("Failed to get Instrument[" + audio.getInstrumentId() + "]"));
}

InstrumentAudio CraftWork::getInstrumentAudio(SegmentChoiceArrangementPick pick) {
  return content->getInstrumentAudio(pick.getInstrumentAudioId())
      .orElseThrow(()->
  new RuntimeException("Failed to get InstrumentAudio[" + pick.getInstrumentAudioId() + "]"));
}

bool CraftWork::isMuted(SegmentChoiceArrangementPick pick) {
  try {
    var
        segment = store->readSegment(pick.getSegmentId())
        .orElseThrow(()->
    new FabricationException("Failed to get Segment[" + pick.getSegmentId() + "]"));
    var
    arrangement = store->read(segment.getId(), SegmentChoiceArrangement.
    class, pick.getSegmentChoiceArrangementId());
    if (arrangement.isEmpty()) {
      return false;
    }
    var
    choice = store->read(segment.getId(), SegmentChoice.
    class, arrangement.get().getSegmentChoiceId());
    return choice.isPresent() ? choice.get().getMute() : false;

  } catch (FabricationException e) {
    spdlog::warn("Unable to determine if SegmentChoiceArrangementPick[{}] is muted because {}", pick.getId(),
                 e.getMessage());
    return false;
  }
}

bool CraftWork::isFinished() {
  return !running.get();
}

std::optional<Program> CraftWork::getMainProgram(Segment segment) {
  auto chain = getChain();
  if (chain.isEmpty()) {
    return std::nullopt;
  }
  var
  mainChoice = store->readAll(segment.getId(), SegmentChoice.
  class).stream().filter(choice->ProgramType.Main.equals(choice.getProgramType())).findFirst();
  if (mainChoice.isEmpty()) {
    return std::nullopt;
  }
  return content->getProgram(mainChoice.get().getProgramId());

}

std::optional<Program> CraftWork::getMacroProgram(Segment segment) {
  auto chain = getChain();
  if (chain.isEmpty()) {
    return std::nullopt;
  }
  var
  macroChoice = store->readAll(segment.getId(), SegmentChoice.
  class).stream().filter(choice->ProgramType.Macro.equals(choice.getProgramType())).findFirst();
  if (macroChoice.isEmpty()) {
    return std::nullopt;
  }
  return content->getProgram(macroChoice.get().getProgramId());
}

ContentEntityStore CraftWork::getSourceMaterial() {
  return content;
}

std::optional<Long> CraftWork::getCraftedToChainMicros() {
  try {
    return store->readAllSegments().stream().filter(segment->SegmentState.CRAFTED.equals(segment.getState())).max(
        Comparator.comparing(Segment::getId)).map(SegmentUtils::getEndAtChainMicros);
  } catch (FabricationException e) {
    spdlog::warn("Unable to get crafted-to chain micros because {}", e.getMessage());
    return std::nullopt;
  }
}

/**
 This is the internal cycle that's run indefinitely
 */
void CraftWork::runCycle(long shippedToChainMicros, long dubbedToChainMicros) {
  if (!running.get()) return;

  try {
    long startedAtMillis = System.currentTimeMillis();
    doFabrication(dubbedToChainMicros, shippedToChainMicros + craftAheadMicros);
    telemetry.record(TIMER_SECTION_CRAFT, System.currentTimeMillis() - startedAtMillis);

    startedAtMillis = System.currentTimeMillis();
    doAudioCacheMaintenance(Math.min(shippedToChainMicros, dubbedToChainMicros),
                            Math.max(shippedToChainMicros, dubbedToChainMicros) + mixerLengthMicros);
    telemetry.record(TIMER_SECTION_CRAFT_CACHE, System.currentTimeMillis() - startedAtMillis);

    startedAtMillis = System.currentTimeMillis();
    doSegmentCleanup(shippedToChainMicros);
    telemetry.record(TIMER_SECTION_CRAFT_CLEANUP, System.currentTimeMillis() - startedAtMillis);

  } catch (FabricationFatalException e) {
    spdlog::warn("Failed to fabricate because {}", e.getMessage());

  } catch (Exception e) {
    didFailWhile("running craft work", e);
  }
}

bool CraftWork::isReady() {
  return !nextCycleRewrite.get();
}

void CraftWork::doOverrideMacro(Program macroProgram) {
  spdlog::info("Next craft cycle, will override macro with {}", macroProgram.getName());
  nextCycleOverrideMacroProgram.set(macroProgram);
  doNextCycleRewriteUnlessInitialSegment();
}

void CraftWork::doOverrideMemes(Collection <String> memes) {
  spdlog::info("Next craft cycle, will override memes with {}",
               StringUtils.toProperCsvAnd(memes.stream().sorted().toList()));
  nextCycleOverrideMemes.set(memes);
  doNextCycleRewriteUnlessInitialSegment();
}

bool CraftWork::getAndResetDidOverride() {
  return didOverride.getAndSet(false);
}

/**
 If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
 the first segment should be governed by that selection
 https://github.com/xjmusic/xjmusic/issues/201
 */
void CraftWork::doNextCycleRewriteUnlessInitialSegment() {
  if (0 < store->getSegmentCount())
    nextCycleRewrite.set(true);
}

/**
 Fabricate the chain based on craft state
 <p>
 Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub live performance modulation https://github.com/xjmusic/xjmusic/issues/197

 @param dubbedToChainMicros already dubbed to here
 @param craftToChainMicros  target to craft until
 @throws FabricationFatalException if the chain cannot be fabricated
 */
void CraftWork::doFabrication(long dubbedToChainMicros, long craftToChainMicros) {
  if (nextCycleRewrite) {
    doFabricationRewrite(dubbedToChainMicros, nextCycleOverrideMacroProgram, nextCycleOverrideMemes);
    nextCycleRewrite = false;
  } else {
    doFabricationDefault(craftToChainMicros, nextCycleOverrideMacroProgram, nextCycleOverrideMemes);
  }
}

/**
 Default behavior is to fabricate the next segment if we are not crafted enough ahead, otherwise skip

 @param toChainMicros to target chain micros
 @throws FabricationFatalException if the chain cannot be fabricated
 */
void CraftWork::doFabricationDefault(
    unsigned long long toChainMicros,
    std::optional<Program *> overrideMacroProgram,
    std::set<std::string> overrideMemes) {
  try {
// currently fabricated AT (vs target fabricated TO)
    long atChainMicros = ChainUtils.computeFabricatedToChainMicros(store->readAllSegments());
    double aheadSeconds = ((double) (atChainMicros - toChainMicros) / MICROS_PER_SECOND);
    if (aheadSeconds > 0) return;

// Build next segment in chain
// Get the last segment in the chain
// If the chain had no last segment, it must be empty; return a template for its first segment
    Segment segment;
    auto existing = store->readSegmentLast();
    if (existing.

        isEmpty()

        ) {
      segment = buildSegmentInitial();
    } else if (Objects.
        isNull(existing
                   .

                       get()

                   .

                       getDurationMicros()

    )) {
      spdlog::debug("Last segment in chain has no duration, cannot fabricate next segment");
      return;
    } else {
      segment = buildSegmentFollowing(existing.get());
    }
    segment = store->put(segment);
    doFabricationWork(segment, null, overrideMacroProgram, overrideMemes
    );

  } catch (
      FabricationException e
  ) {
    didFailWhile("fabricating", e);
  }
}

/**
 Override behavior deletes all future segments and re-fabricates starting with the given parameters
 <p>
 Macro program override
 https://github.com/xjmusic/xjmusic/issues/197
 <p>
 Memes override
 https://github.com/xjmusic/xjmusic/issues/199

 @param dubbedToChainMicros  already dubbed to here
 @param overrideMacroProgram to override fabrication
 @param overrideMemes        to override fabrication
 @throws FabricationFatalException if the chain cannot be fabricated
 */
void CraftWork::doFabricationRewrite(
    unsigned long long dubbedToChainMicros,
    std::optional<Program *> overrideMacroProgram,
    std::set<std::string> overrideMemes) {
  try {
// Determine the segment we are currently in the middle of dubbing
    auto lastSegment = getSegmentAtChainMicros(dubbedToChainMicros);
    if (!lastSegment.has_value()) {
      spdlog::warn("Will not delete any segments because fabrication is already at the end of the known chain.");
      return;
    }

    // Determine whether the current segment can be cut short
    auto currentMainProgram = getMainProgram(lastSegment.get());
    if (!currentMainProgram.has_value()) {
      spdlog::warn("Will not delete any segments because current segment has no main program.");
      return;
    }
    ProgramConfig mainProgramConfig;
    try {
      mainProgramConfig = ProgramConfig(currentMainProgram.get().getConfig());
    } catch (std::exception e) {
      throw FabricationException("Failed to get main program config");
    }
    auto subBeats = mainProgramConfig.barBeats * mainProgramConfig.cutoffMinimumBars;
    auto dubbedToSegmentMicros = dubbedToChainMicros - lastSegment.value().beginAtChainMicros;
    auto microsPerBeat = (long) (ValueUtils::MICROS_PER_MINUTE / currentMainProgram.get().getTempo());
    auto dubbedToSegmentBeats = dubbedToSegmentMicros / microsPerBeat;
    auto cutoffAfterBeats = subBeats * Math.ceil((double) dubbedToSegmentBeats / subBeats);
    if (cutoffAfterBeats < lastSegment.

            value()

        .

            getTotal()

        ) {
      doCutoffLastSegment(lastSegment
                              .

                                  get(), cutoffAfterBeats

      );
    }

// Delete all segments after the current segment and fabricate the next segment
    spdlog::info("Will delete segments after #{} and re-fabricate.", lastSegment.

            get()

        .

            getId()

    );
    if (Objects.
        nonNull(overrideMacroProgram)
        )
      spdlog::info("Has macro program override {}", overrideMacroProgram.

          getName()

      );
    else if (Objects.
        nonNull(overrideMemes)
        )
      spdlog::info("Has meme override {}", StringUtils.
          toProperCsvAnd(overrideMemes
                             .

                                 stream()

                             .

                                 sorted()

                             .

                                 toList()

      ));
    else {
      spdlog::warn("Neither override memes nor macros are present: unsure what rewrite action to take");
    }
    store->
        deleteSegmentsAfter(lastSegment
                                .

                                    get()

                                .

                                    getId()

    );
    Segment segment = buildSegmentFollowing(lastSegment.get());
    segment.
        setType(SegmentType
                    .NEXT_MACRO);
    segment = store->put(segment);
    doFabricationWork(segment, SegmentType
        .NEXT_MACRO, overrideMacroProgram, overrideMemes);
    didOverride.set(true);

  } catch (
      FabricationException e
  ) {
    didFailWhile("fabricating", e);
  }
}

/**
 Cut the current segment short after the given number of beats

 @param segment          to cut short
 @param cutoffAfterBeats number of beats to cut short after
 */
void CraftWork::doCutoffLastSegment(Segment segment, double cutoffAfterBeats) {
  try {
    auto durationMicros = cutoffAfterBeats * MICROS_PER_MINUTE / segment.getTempo();
    spdlog::info("Will cut current segment short after {} beats.", cutoffAfterBeats);
    segment.setTotal((int) cutoffAfterBeats);
    segment.setDurationMicros((long) (durationMicros));
    store->updateSegment(segment);
    store->readAll(segment.getId(), SegmentChoiceArrangementPick.
    class)
    .forEach((pick)->
    {
      try {
        if (pick.getStartAtSegmentMicros() >= durationMicros)
          store->
        delete (segment.getId(), SegmentChoiceArrangementPick.
        class, pick.getId());
        else if (Objects.nonNull(pick.getLengthMicros()) &&
                 pick.getStartAtSegmentMicros() + pick.getLengthMicros() > durationMicros)
          store->put(pick.lengthMicros((long) (durationMicros - pick.getStartAtSegmentMicros())));
      } catch (FabricationException e) {
        spdlog::error("Failed to cut SegmentChoiceArrangementPick[{}] short to {} beats because {}", pick.getId(),
                      cutoffAfterBeats, e.getMessage());
      }
    });

  } catch (Exception e) {
    throw new FabricationException(
        String.format("Failed to cut Segment[%d] short to %f beats", segment.getId(), cutoffAfterBeats), e);
  }
}

/**
 Craft a Segment, or fail

 @param segment              to craft
 @param overrideSegmentType  to use for crafting
 @param overrideMacroProgram to override fabrication
 @param overrideMemes        to override fabrication
 @ on configuration failure
 @ on craft failure
 */
void CraftWork::doFabricationWork(
    Segment *segment,
    std::optional<Segment::Type>
    overrideSegmentType,
    std::optional<Program *> overrideMacroProgram,
    std::set<std::string> overrideMemes
) {
  spdlog::debug("[segId={}] will prepare fabricator", segment->id);
  Fabricator *fabricator = fabricatorFactory->fabricate(content, segment->id, outputFrameRate, outputChannels,
                                                        overrideSegmentType);

  spdlog::debug("[segId={}] will do craft work", segment->id);
  updateSegmentState(fabricator, segment, Segment::State::Planned, Segment::State::Crafting);
  craftFactory->macroMain(fabricator, overrideMacroProgram, overrideMemes).doWork();

  craftFactory->beat(fabricator).doWork();

  craftFactory->detail(fabricator).doWork();

  craftFactory->transition(fabricator).doWork();
  craftFactory->background(fabricator).doWork();

  spdlog::debug("Fabricated Segment[{}]", segment->id);

  updateSegmentState(fabricator, segment, Segment::State::Crafting, Segment::State::Crafted);
  spdlog::debug("[segId={}] Worked for {} seconds", segment.getId(),
                String.format("%.2f", (float) fabricator.getElapsedMicros() / MICROS_PER_SECOND));
}

/**
 Delete segments before the given shipped-to chain micros

 @param shippedToChainMicros the shipped-to chain micros
 */
void CraftWork::doSegmentCleanup(long shippedToChainMicros) {
  getSegmentAtChainMicros(shippedToChainMicros - persistenceWindowMicros)
      .ifPresent(segment->store->deleteSegmentsBefore(segment.getId()));
}

/**
 Engineer wants platform heartbeat to check for any stale production chains in fabricate state
 and if found, send back a failure health check it in order to ensure the Chain remains in an operable state.
 <p>
 Medic relies on precomputed telemetry of fabrication latency
 */
void CraftWork::doAudioCacheMaintenance(long minChainMicros, long maxChainMicros) {
  // Poke the audio cache to load all known-to-be-upcoming audio to cache; this is a no-op for already-cache audio
  auto segments = getSegmentsIfReady(minChainMicros, maxChainMicros);
  Set <UUID> seen = new HashSet<>();
  std::vector<InstrumentAudio> currentAudios = new ArrayList<>();
  getPicks(segments).stream()
      .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStartAtSegmentMicros))
      .map(this
  ::getInstrumentAudio)
  .forEach(audio->
  {
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
Segment CraftWork::buildSegmentInitial() {
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
 */
Segment CraftWork::buildSegmentFollowing(Segment last) {
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
void CraftWork::didFailWhile(std::string msgWhile, std::exception e) {
  spdlog::error("Failed while {} because {}\n{}", msgWhile, e.what());
  running = false;
  finish();
}

/**
 Update Segment to Working state

 @param fabricator to update
 @param fromState  of existing segment
 @param toState    of new segment
 @ if record is invalid
 */
void
CraftWork::updateSegmentState(Fabricator fabricator, Segment segment, Segment::State fromState, Segment::State toState) {
  if (fromState != segment.getState())
    throw new FabricationException(
        String.format("Segment[%s] %s requires Segment must be in %s state.", segment.getId(), toState, fromState));
  auto seg = fabricator.getSegment();
  seg.setState(toState);
  fabricator.updateSegment(seg);
  spdlog::debug("[segId={}] Segment transitioned to state {} OK", segment.getId(), toState);
}


/**
 Bootstrap a chain from JSON chain bootstrap data.
 */
const Chain * CraftWork::createChainForTemplate(const Template *tmpl) {
    spdlog::info("Will bootstrap Template[{}]", TemplateUtils::getIdentifier(tmpl));
    auto entity = ChainUtils::fromTemplate(tmpl);
    entity.id = EntityUtils::computeUniqueId();
    return store->put(entity);
}
