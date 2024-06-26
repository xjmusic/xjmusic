// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.


#include "xjmusic/segment/SegmentEntityStore.h"
#include "xjmusic/fabricator/FabricationException.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/ValueUtils.h"

using namespace XJ;


#define SEGMENT_STORE_CORE_METHODS(ENTITY, ENTITIES, STORE)                                   \
  ENTITY* SegmentEntityStore::put(const ENTITY &entity) {                                     \
    if (STORE.find(entity.segmentId) == STORE.end()) {                                        \
      STORE[entity.segmentId] = std::map<UUID, ENTITY>();                                     \
    }                                                                                         \
    STORE[entity.segmentId][entity.id] = entity;                                              \
    return &STORE[entity.segmentId][entity.id];                                               \
  }                                                                                           \
  std::optional<ENTITY *> SegmentEntityStore::read##ENTITY(int segmentId, const UUID &id) {   \
    if (STORE.find(segmentId) == STORE.end()) {                                               \
      return std::nullopt;                                                                    \
    }                                                                                         \
    if (STORE[segmentId].find(id) == STORE[segmentId].end()) {                                \
      return std::nullopt;                                                                    \
    }                                                                                         \
    return {&STORE[segmentId][id]};                                                           \
  }                                                                                           \
  std::set<ENTITY *> SegmentEntityStore::readAll##ENTITIES(int segmentId) {                   \
    std::set<ENTITY *> result;                                                                \
    if (STORE.find(segmentId) == STORE.end()) {                                               \
      return result;                                                                          \
    }                                                                                         \
    for (auto &choice: STORE[segmentId]) {                                                    \
      result.emplace(&choice.second);                                                         \
    }                                                                                         \
    return result;                                                                            \
  }                                                                                           \
  std::set<ENTITY *> SegmentEntityStore::readAll##ENTITIES(const std::set<int> &segmentIds) { \
    std::set<ENTITY *> result;                                                                \
    for (auto &segmentId: segmentIds) {                                                       \
      if (STORE.find(segmentId) == STORE.end()) {                                             \
        continue;                                                                             \
      }                                                                                       \
      for (auto &choice: STORE[segmentId]) {                                                  \
        result.emplace(&choice.second);                                                       \
      }                                                                                       \
    }                                                                                         \
    return result;                                                                            \
  }                                                                                           \
  void SegmentEntityStore::delete##ENTITY(int segmentId, const UUID &id) {                    \
    if (STORE.find(segmentId) == STORE.end()) {                                               \
      return;                                                                                 \
    }                                                                                         \
    STORE[segmentId].erase(id);                                                               \
  }

SEGMENT_STORE_CORE_METHODS(SegmentChoice, SegmentChoices, segmentChoices)

SEGMENT_STORE_CORE_METHODS(SegmentChoiceArrangement, SegmentChoiceArrangements, segmentChoiceArrangements)

SEGMENT_STORE_CORE_METHODS(SegmentChoiceArrangementPick, SegmentChoiceArrangementPicks, segmentChoiceArrangementPicks)

SEGMENT_STORE_CORE_METHODS(SegmentChord, SegmentChords, segmentChords)

SEGMENT_STORE_CORE_METHODS(SegmentChordVoicing, SegmentChordVoicings, segmentChordVoicings)

SEGMENT_STORE_CORE_METHODS(SegmentMeme, SegmentMemes, segmentMemes)

SEGMENT_STORE_CORE_METHODS(SegmentMessage, SegmentMessages, segmentMessages)

SEGMENT_STORE_CORE_METHODS(SegmentMeta, SegmentMetas, segmentMetas)


Chain *SegmentEntityStore::put(const Chain &chain) {
  const auto cc = new Chain(chain);
  this->chain = *cc;
  return cc;
}

Segment *SegmentEntityStore::put(const Segment &segment) {
  const auto sc = new Segment(segment);
  this->segments[segment.id] = *sc;
  return sc;
}

std::optional<Segment *> SegmentEntityStore::readSegmentAtChainMicros(const long chainMicros) {
  for (auto &[_, segment]: segments) {
    if (SegmentUtils::isSpanning(&segment, chainMicros, chainMicros)) {
      return {&segment};
    }
  }
  return std::nullopt;
}

std::optional<Chain *> SegmentEntityStore::readChain() {
  return chain.has_value() ? &chain.value() : nullptr;
}

std::optional<Segment *> SegmentEntityStore::readSegment(const int segmentId) {
  if (segments.find(segmentId) != segments.end()) return {&segments[segmentId]};
  return std::nullopt;
}

std::vector<Segment *> SegmentEntityStore::readAllSegments() {
  std::vector<Segment *> result;
  for (auto &[_, segment]: segments) {
    result.emplace_back(&segment);
  }
  return result;
}


std::vector<Segment> SegmentEntityStore::readSegmentsFromToOffset(const int fromOffset, const int toOffset) {
  std::vector<Segment> result;
  for (auto &[_, segment]: segments) {
    if (segment.id >= fromOffset && segment.id <= toOffset) {
      result.emplace_back(segment);
    }
  }
  return result;
}


std::set<SegmentEntity *> SegmentEntityStore::readAllSegmentEntities(const std::set<int> &segmentIds) {
  std::set<SegmentEntity *> result;
  for (auto &segmentId: segmentIds) {
    for (auto &choice: readAllSegmentChoices(segmentId)) {
      result.emplace(choice);
    }
    for (auto &arrangement: readAllSegmentChoiceArrangements(segmentId)) {
      result.emplace(arrangement);
    }
    for (auto &pick: readAllSegmentChoiceArrangementPicks(segmentId)) {
      result.emplace(pick);
    }
    for (auto &chord: readAllSegmentChords(segmentId)) {
      result.emplace(chord);
    }
    for (auto &voicing: readAllSegmentChordVoicings(segmentId)) {
      result.emplace(voicing);
    }
    for (auto &meme: readAllSegmentMemes(segmentId)) {
      result.emplace(meme);
    }
    for (auto &message: readAllSegmentMessages(segmentId)) {
      result.emplace(message);
    }
    for (auto &meta: readAllSegmentMetas(segmentId)) {
      result.emplace(meta);
    }
  }
  return result;
}


std::vector<Segment> SegmentEntityStore::readAllSegmentsSpanning(const long fromChainMicros, const long toChainMicros) {
  std::vector<Segment> result;
  for (auto &[id, segment]: segments) {
    if (SegmentUtils::isSpanning(&segment, fromChainMicros, toChainMicros)) {
      result.emplace_back(segment);
    }
  }
  return result;
}


int SegmentEntityStore::readLastSegmentId() const {
  if (segments.empty()) {
    return 0;
  }
  return segments.rbegin()->first;
}


std::optional<Segment> SegmentEntityStore::readSegmentLast() {
  if (segments.empty()) {
    return std::nullopt;
  }
  return {segments.rbegin()->second};
}


std::optional<SegmentChoice *> SegmentEntityStore::readChoice(const int segmentId, const Program::Type programType) {
  if (segmentChoices.find(segmentId) == segmentChoices.end()) {
    return std::nullopt;
  }
  for (auto &[_, choice]: segmentChoices[segmentId]) {
    if (choice.programType == programType) {
      return {&choice};
    }
  }
  return std::nullopt;
}


std::string SegmentEntityStore::readChoiceHash(const Segment &segment) {
  const std::set<SegmentEntity *> entities = readAllSegmentEntities({segment.id});
  std::vector<std::string> ids;

  for (const auto &entity: entities) {
    ids.push_back(entity->id);
  }

  std::sort(ids.begin(), ids.end());
  return StringUtils::join(ids, "_");
}


int SegmentEntityStore::getSegmentCount() const {
  return static_cast<int>(segments.size());
}


bool SegmentEntityStore::isEmpty() const {
  return segments.empty();
}


void SegmentEntityStore::updateSegment(Segment &segment) {
  // validate and cache to-state
  validate(segment);
  const Segment::State toState = segment.state;

  // fetch existing segment; further logic is based on its current state
  const std::optional<Segment *> existingOpt = readSegment(segment.id);
  if (existingOpt.has_value()) {
    const Segment *existing = existingOpt.value();

    // logic based on existing Segment State
    protectSegmentStateTransition(existing->state, toState);

    // fail if attempt to [#128] change chainId of a segment
    const std::optional updateChainId = segment.chainId;
    if (updateChainId.has_value() && updateChainId.value() != existing->chainId) {
      throw FabricationException("cannot modify chainId of a Segment");
    }

    // Never change id
    segment.id = existing->id;
  }

  // Updated at is always now
  segment.updatedAt = EntityUtils::currentTimeMillis();

  // save segment
  put(segment);
}


void SegmentEntityStore::deleteChain() {
  chain = std::nullopt;
}


void SegmentEntityStore::deleteSegment(const int id) {
  segments.erase(id);
  segmentChoices.erase(id);
  segmentChoiceArrangements.erase(id);
  segmentChoiceArrangementPicks.erase(id);
  segmentChords.erase(id);
  segmentChordVoicings.erase(id);
  segmentMemes.erase(id);
  segmentMessages.erase(id);
  segmentMetas.erase(id);
}


void SegmentEntityStore::protectSegmentStateTransition(const Segment::State fromState, const Segment::State toState) {
  switch (fromState) {
    case Segment::State::Planned:
      onlyAllowSegmentStateTransitions(toState, {
                                                    Segment::State::Planned,
                                                    Segment::State::Crafting,
                                                });
      break;
    case Segment::State::Crafting:
      onlyAllowSegmentStateTransitions(toState, {
                                                    Segment::State::Crafting,
                                                    Segment::State::Crafted,
                                                    Segment::State::Failed,
                                                    Segment::State::Planned,
                                                });
      break;
    case Segment::State::Crafted:
      onlyAllowSegmentStateTransitions(toState, {
                                                    Segment::State::Crafted,
                                                    Segment::State::Crafting,
                                                });
      break;
    case Segment::State::Failed:
      onlyAllowSegmentStateTransitions(toState, {
                                                    Segment::State::Failed,
                                                });
      break;
    default:
      onlyAllowSegmentStateTransitions(toState, {Segment::State::Planned});
      break;
  }
}


void SegmentEntityStore::onlyAllowSegmentStateTransitions(
    const Segment::State toState,
    const std::set<Segment::State> &allowedStates) {
  std::vector<std::string> allowedStateNames;
  allowedStateNames.reserve(allowedStates.size());
  for (Segment::State search: allowedStates) {
    allowedStateNames.emplace_back(Segment::toString(search));
    if (search == toState) {
      return;
    }
  }
  throw FabricationException(
      "transition to " + Segment::toString(toState) +
      " not in allowed (" + StringUtils::join(allowedStateNames, ",") +
      ")");
}


void SegmentEntityStore::validate(SegmentMeme entity) {
  entity.name = StringUtils::toMeme(entity.name);
}


void SegmentEntityStore::validate(Segment entity) {
  entity.updatedAt = EntityUtils::currentTimeMillis();
}


void SegmentEntityStore::deleteSegmentsBefore(const int lastSegmentId) {
  std::set<int> idsToDelete;
  for (auto &[segmentId, segment]: segments) {
    if (segmentId < lastSegmentId) {
      idsToDelete.insert(segmentId);
    }
  }
  for (auto &id: idsToDelete) {
    deleteSegment(id);
  }
}


void SegmentEntityStore::deleteSegmentsAfter(const int lastSegmentId) {
  std::set<int> idsToDelete;
  for (auto &[segmentId, segment]: segments) {
    if (segmentId > lastSegmentId) {
      idsToDelete.insert(segmentId);
    }
  }
  for (auto &id: idsToDelete) {
    deleteSegment(id);
  }
}


void SegmentEntityStore::clear() {
  segments.clear();
  segmentChoices.clear();
  segmentChoiceArrangements.clear();
  segmentChoiceArrangementPicks.clear();
  segmentChords.clear();
  segmentChordVoicings.clear();
  segmentMemes.clear();
  segmentMessages.clear();
  segmentMetas.clear();
  chain = std::nullopt;
}
