// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>

#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/fabricator/TemplateUtils.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"
#include "xjmusic/work/CraftWork.h"
#include "spdlog/spdlog.h"
#include "xjmusic/fabricator/FabricationException.h"
#include "xjmusic/craft/MacroMainCraft.h"
#include "xjmusic/craft/BeatCraft.h"
#include "xjmusic/craft/DetailCraft.h"
#include "xjmusic/craft/TransitionCraft.h"
#include "xjmusic/craft/BackgroundCraft.h"

using namespace XJ;


CraftWork::CraftWork(
    FabricatorFactory *fabricatorFactory,
    SegmentEntityStore *store,
    ContentEntityStore *content,
    const long persistenceWindowSeconds,
    const long craftAheadSeconds) {
  this->fabricatorFactory = fabricatorFactory;
  this->store = store;

  craftAheadMicros = craftAheadSeconds * ValueUtils::MICROS_PER_SECOND;
  persistenceWindowMicros = persistenceWindowSeconds * ValueUtils::MICROS_PER_SECOND;
  this->content = content;

  // Telemetry: # Segments Erased
  if (content->getTemplates().empty())
    throw std::runtime_error("Cannot initialize CraftWork without Templates");

  const auto tmpl = *content->getTemplates().begin();
  chain = createChainForTemplate(tmpl);

  templateConfig = TemplateConfig(chain->templateConfig);

  running = true;
}

void CraftWork::finish() {
  if (!running) return;
  running = false;
  spdlog::info("Finished");
}

const Chain *CraftWork::getChain() const {
  if (chain == nullptr) {
    throw std::runtime_error("Cannot get null pointer to Chain");
  }
  return chain;
}

TemplateConfig CraftWork::getTemplateConfig() {
  return templateConfig;
}

std::vector<const Segment *> CraftWork::getSegmentsIfReady(const unsigned long long fromChainMicros, const unsigned long long toChainMicros) const {
  auto currentSegments = store->readAllSegmentsSpanning(fromChainMicros, toChainMicros);
  if (currentSegments.empty()) {
    return {};
  }
  auto previousSegment = store->readSegment(currentSegments.at(0)->id - 1);
  auto nextSegment = store->readSegment(currentSegments.at(currentSegments.size() - 1)->id + 1);
  std::vector<const Segment *> segments;
  if (previousSegment.has_value() && Segment::State::Crafted == previousSegment.value()->state)
    segments.emplace_back(previousSegment.value());
  for (auto segment: currentSegments)
    if (Segment::State::Crafted == segment->state)
      segments.emplace_back(segment);
  if (nextSegment.has_value() && Segment::State::Crafted == nextSegment.value()->state)
    segments.emplace_back(nextSegment.value());
  return segments;
}

std::optional<const Segment *> CraftWork::getSegmentAtChainMicros(const unsigned long long chainMicros) const {
  // require current segment in crafted state
  const auto currentSegment = store->readSegmentAtChainMicros(chainMicros);
  if (!currentSegment.has_value() || currentSegment.value()->state != Segment::State::Crafted) {
    return std::nullopt;
  }
  return currentSegment;
}

std::optional<const Segment *> CraftWork::getSegmentAtOffset(const int offset) const {
  // require current segment in crafted state
  const auto currentSegment = store->readSegment(offset);
  if (!currentSegment.has_value() || currentSegment.value()->state != Segment::State::Crafted) {
    return std::nullopt;
  }
  return currentSegment;
}

std::set<const SegmentChoiceArrangementPick *> CraftWork::getPicks(const std::vector<const Segment *> &segments) const {
  return store->readAllSegmentChoiceArrangementPicks(segments);
}

const Instrument *CraftWork::getInstrument(const InstrumentAudio *audio) const {
  const auto instrument = content->getInstrument(audio->instrumentId);
  if (!instrument.has_value()) {
    throw std::runtime_error("Failed to get Instrument[" + audio->instrumentId + "]");
  }
  return instrument.value();
}

const InstrumentAudio *CraftWork::getInstrumentAudio(const SegmentChoiceArrangementPick *pick) const {
  const auto audio = content->getInstrumentAudio(pick->instrumentAudioId);
  if (!audio.has_value()) {
    throw std::runtime_error("Failed to get InstrumentAudio[" + pick->instrumentAudioId + "]");
  }
  return audio.value();
}

bool CraftWork::isMuted(const SegmentChoiceArrangementPick *pick) const {
  try {
    const auto segment = store->readSegment(pick->segmentId);
    if (!segment.has_value()) {
      throw std::runtime_error("Failed to get Segment[" + std::to_string(pick->segmentId) + "]");
    }
    const auto arrangement = store->readSegmentChoiceArrangement(pick->segmentId, pick->segmentChoiceArrangementId);
    if (!arrangement.has_value())
      throw std::runtime_error("Failed to get SegmentChoiceArrangement[" + pick->segmentChoiceArrangementId + "]");

    const auto choice = store->readSegmentChoice(pick->segmentId, arrangement.value()->segmentChoiceId);
    return choice.has_value() ? choice.value()->mute : false;

  } catch (std::exception &e) {
    spdlog::warn("Unable to determine if SegmentChoiceArrangementPick[{}] is muted because {}", pick->id, e.what());
    return false;
  }
}

bool CraftWork::isFinished() const {
  return !running;
}

std::optional<const Program *> CraftWork::getMainProgram(const Segment *segment) const {
  if (chain == nullptr) {
    return std::nullopt;
  }
  const auto
      choices = store->readAllSegmentChoices(segment->id);
  for (const auto choice: choices) {
    if (Program::Type::Main == choice->programType) {
      return content->getProgram(choice->programId);
    }
  }
  return std::nullopt;
}

std::optional<const Program *> CraftWork::getMacroProgram(const Segment &segment) const {
  if (chain == nullptr) {
    return std::nullopt;
  }
  const auto
      choices = store->readAllSegmentChoices(segment.id);
  for (const auto choice: choices) {
    if (Program::Type::Macro == choice->programType) {
      return content->getProgram(choice->programId);
    }
  }
  return std::nullopt;
}

ContentEntityStore *CraftWork::getSourceMaterial() const {
  return content;
}

void CraftWork::runCycle(const long atChainMicros) {
  if (!running) return;

  try {
    doFabrication(atChainMicros + craftAheadMicros);
    doSegmentCleanup(atChainMicros);

  } catch (std::exception &e) {
    didFailWhile("running craft work", e);
  }
}

bool CraftWork::isReady() const {
  return !nextCycleRewrite;
}

void CraftWork::doOverrideMacro(const Program *macroProgram) {
  spdlog::info("Next craft cycle, will override macro with {}", macroProgram->name);
  nextCycleOverrideMacroProgram = {macroProgram};
  doNextCycleRewriteUnlessInitialSegment();
}

void CraftWork::doOverrideMemes(std::set<std::string> memes) {
  spdlog::info("Next craft cycle, will override memes with {}",
               CsvUtils::toProperCsvAnd(std::vector(memes.begin(), memes.end())));
  nextCycleOverrideMemes = memes;
  doNextCycleRewriteUnlessInitialSegment();
}

bool CraftWork::getAndResetDidOverride() {
  const auto previous = didOverride;
  didOverride = false;
  return previous;
}

void CraftWork::doNextCycleRewriteUnlessInitialSegment() {
  if (0 < store->getSegmentCount())
    nextCycleRewrite = true;
}

void CraftWork::doFabrication(const long craftToChainMicros) {
  if (nextCycleRewrite) {
    doFabricationRewrite(craftToChainMicros, nextCycleOverrideMacroProgram, nextCycleOverrideMemes);
    nextCycleRewrite = false;
  } else {
    doFabricationDefault(craftToChainMicros, nextCycleOverrideMacroProgram, nextCycleOverrideMemes);
  }
}

void CraftWork::doFabricationDefault(
    const unsigned long long toChainMicros,
    const std::optional<const Program *> overrideMacroProgram,
    const std::set<std::string> &overrideMemes) {
  try {
    // currently fabricated AT (vs target fabricated TO)
    const long atChainMicros = ChainUtils::computeFabricatedToChainMicros(store->readAllSegments());
    const float aheadSeconds = atChainMicros > toChainMicros
                                   ? (static_cast<float>(atChainMicros - toChainMicros) / ValueUtils::MICROS_PER_SECOND)
                                   : 0;
    if (aheadSeconds > 0) return;

    // Build next segment in chain
    // Get the last segment in the chain
    // If the chain had no last segment, it must be empty; return a template for its first segment
    Segment segment;
    const auto existing = store->readSegmentLast();
    if (!existing.has_value()) {
      segment = buildSegmentInitial();
    } else if (!existing.value()->durationMicros.has_value()) {
      spdlog::debug("Last segment in chain has no duration, cannot fabricate next segment");
      return;
    } else {
      segment = buildSegmentFollowing(existing.value());
    }
    doFabricationWork(store->put(segment), std::nullopt, overrideMacroProgram, overrideMemes);

  } catch (
      std::exception e) {
    didFailWhile("fabricating", e);
  }
}

void CraftWork::doFabricationRewrite(
    const unsigned long long dubbedToChainMicros,
    std::optional<const Program *> overrideMacroProgram,
    std::set<std::string> overrideMemes) {
  try {
    // Determine the segment we are currently in the middle of dubbing
    const auto lastSegment = getSegmentAtChainMicros(dubbedToChainMicros);
    if (!lastSegment.has_value()) {
      spdlog::warn("Will not delete any segments because fabrication is already at the end of the known chain->");
      return;
    }

    // Determine whether the current segment can be cut short
    const auto currentMainProgram = getMainProgram(lastSegment.value());
    if (!currentMainProgram.has_value()) {
      spdlog::warn("Will not delete any segments because current segment has no main program.");
      return;
    }
    ProgramConfig mainProgramConfig;
    try {
      mainProgramConfig = ProgramConfig(currentMainProgram.value()->config);
    } catch (std::exception &e) {
      throw FabricationException("Failed to get main program config");
    }
    const auto subBeats = mainProgramConfig.barBeats * mainProgramConfig.cutoffMinimumBars;
    const auto dubbedToSegmentMicros = dubbedToChainMicros - lastSegment.value()->beginAtChainMicros;
    const auto microsPerBeat = static_cast<long>(ValueUtils::MICROS_PER_MINUTE / currentMainProgram.value()->tempo);
    const auto dubbedToSegmentBeats = dubbedToSegmentMicros / microsPerBeat;
    const auto cutoffAfterBeats = subBeats * ceil(static_cast<double>(dubbedToSegmentBeats) / subBeats);
    if (cutoffAfterBeats < lastSegment.value()->total) {
      doCutoffLastSegment(lastSegment.value(), cutoffAfterBeats);
    }

    // Delete all segments after the current segment and fabricate the next segment
    spdlog::info("Will delete segments after #{} and re-fabricate.", lastSegment.value()->id);
    if (overrideMacroProgram.has_value())
      spdlog::info("Has macro program override {}", overrideMacroProgram.value()->name);
    else if (!overrideMemes.empty())
      spdlog::info("Has meme override {}", CsvUtils::toProperCsvAnd(std::vector(overrideMemes.begin(), overrideMemes.end())));
    else {
      spdlog::warn("Neither override memes nor macros are present: unsure what rewrite action to take");
    }
    store->deleteSegmentsAfter(lastSegment.value()->id);
    Segment followingSegment = buildSegmentFollowing(lastSegment.value());
    followingSegment.type = Segment::Type::NextMacro;
    doFabricationWork(store->put(followingSegment), Segment::Type::NextMacro, overrideMacroProgram, overrideMemes);
    didOverride = true;

  } catch (
      std::exception e) {
    didFailWhile("fabricating", e);
  }
}

void CraftWork::doCutoffLastSegment(const Segment *inputSegment, float cutoffAfterBeats) const {
  try {
    const long durationMicros = cutoffAfterBeats * ValueUtils::MICROS_PER_MINUTE / inputSegment->tempo;
    spdlog::info("Will cut current segment short after {} beats.", cutoffAfterBeats);
    Segment updateSegment = *inputSegment;
    updateSegment.total = static_cast<int>(cutoffAfterBeats);
    updateSegment.durationMicros = static_cast<long>(durationMicros);
    store->updateSegment(updateSegment);
    for (const auto pick: store->readAllSegmentChoiceArrangementPicks(inputSegment->id)) {
      try {
        if (pick->startAtSegmentMicros >= durationMicros)
          store->deleteSegmentChoiceArrangementPick(inputSegment->id, pick->id);
        else if (0 < pick->lengthMicros &&
                 pick->startAtSegmentMicros + pick->lengthMicros > durationMicros) {
          SegmentChoiceArrangementPick updatePick = *pick;
          updatePick.lengthMicros = static_cast<long>(durationMicros - pick->startAtSegmentMicros);
          store->put(updatePick);
        }
      } catch (std::exception &e) {
        spdlog::error("Failed to cut SegmentChoiceArrangementPick[{}] short to {} beats because {}", pick->id,
                      cutoffAfterBeats, e.what());
      }
    }

  } catch (std::exception &e) {
    throw FabricationException(
        "Failed to cut Segment[" + std::to_string(inputSegment->id) + "] short to " + std::to_string(cutoffAfterBeats) + " beats: " + e.what());
  }
}

void CraftWork::doFabricationWork(
    const Segment *inputSegment,
    const std::optional<Segment::Type> overrideSegmentType,
    const std::optional<const Program *> overrideMacroProgram,
    const std::set<std::string> &overrideMemes) const {
  spdlog::debug("[segId={}] will prepare fabricator", inputSegment->id);
  Fabricator *fabricator = fabricatorFactory->fabricate(content, inputSegment->id, overrideSegmentType);

  spdlog::debug("[segId={}] will do craft work", inputSegment->id);
  const Segment * updatedSegment = updateSegmentState(fabricator, inputSegment, Segment::State::Planned, Segment::State::Crafting);
  MacroMainCraft(fabricator, overrideMacroProgram, overrideMemes).doWork();

  BeatCraft(fabricator).doWork();
  DetailCraft(fabricator).doWork();
  TransitionCraft(fabricator).doWork();
  BackgroundCraft(fabricator).doWork();

  spdlog::debug("Fabricated Segment[{}]", inputSegment->id);

  updateSegmentState(fabricator, updatedSegment, Segment::State::Crafting, Segment::State::Crafted);
}

void CraftWork::doSegmentCleanup(const long shippedToChainMicros) const {
  const auto segment = getSegmentAtChainMicros(shippedToChainMicros - persistenceWindowMicros);
  if (segment.has_value())
    store->deleteSegmentsBefore(segment.value()->id);
}

Segment CraftWork::buildSegmentInitial() const {
   auto segment = Segment();
  segment.id = 0;
  segment.chainId = chain->id;
  segment.beginAtChainMicros = 0L;
  segment.delta = 0;
  segment.type = Segment::Type::Pending;
  segment.state = Segment::State::Planned;
  return segment;
}

Segment CraftWork::buildSegmentFollowing(const Segment *last) const {
  if (!last->durationMicros.has_value()) {
    throw std::runtime_error("Last segment has no duration, cannot fabricate next segment");
  }
   auto segment = Segment();
  segment.id = last->id + 1;
  segment.chainId = chain->id;
  segment.beginAtChainMicros = last->beginAtChainMicros + last->durationMicros.value();
  segment.delta = last->delta;
  segment.type = Segment::Type::Pending;
  segment.state = Segment::State::Planned;
  return segment;
}

void CraftWork::didFailWhile(std::string msgWhile, const std::exception &e) {
  spdlog::error("Failed while {} because {}\n{}", msgWhile, e.what());
  running = false;
  finish();
}

const Segment *
CraftWork::updateSegmentState(Fabricator *fabricator, const Segment *inputSegment, const Segment::State fromState, const Segment::State toState) {
  if (fromState != inputSegment->state)
    throw std::runtime_error("Segment[" + std::to_string(inputSegment->id) + "] " + Segment::toString(toState) + " requires Segment must be in " + Segment::toString(fromState) + " state.");
  Segment updateSegment = *inputSegment;
  updateSegment.state = toState;
  auto updatedSegment = fabricator->updateSegment(updateSegment);
  spdlog::debug("[segId={}] Segment transitioned to state {} OK", inputSegment->id, Segment::toString(toState));
  return updatedSegment;
}

const Chain *CraftWork::createChainForTemplate(const Template *tmpl) const {
  spdlog::info("Will bootstrap Template[{}]", TemplateUtils::getIdentifier(tmpl));
  auto entity = ChainUtils::fromTemplate(tmpl);
  entity.id = EntityUtils::computeUniqueId();
  return store->put(entity);
}

std::set<const SegmentChoice *> CraftWork::getChoices(const Segment *segment) const {
  return store->readAllSegmentChoices(segment->id);
}

std::set<const SegmentChoiceArrangement *> CraftWork::getArrangements(const SegmentChoice *choice) const {
  std::set<const SegmentChoiceArrangement *> results;
  for (auto arrangement : store->readAllSegmentChoiceArrangements( choice->segmentId))
    if (arrangement->segmentChoiceId == choice->id)
      results.emplace(arrangement);
  return results;
}

std::set<const SegmentChoiceArrangementPick *> CraftWork::getPicks(const SegmentChoiceArrangement *arrangement) const {
  std::set<const SegmentChoiceArrangementPick *> results;
  for (auto pick : store->readAllSegmentChoiceArrangementPicks( arrangement->segmentId))
    if (pick->segmentChoiceArrangementId == arrangement->id)
      results.emplace(pick);
  return results;
}


