// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.


#include "xjmusic/util/ValueUtils.h"
#include "xjmusic/util/StringUtils.h"
#include "xjmusic/fabricator/FabricationEntityStore.h"
#include "xjmusic/fabricator/FabricationException.h"
#include "xjmusic/fabricator/SegmentUtils.h"

using namespace XJ;

Chain FabricationEntityStore::put(Chain c) {
  this->chain = c;
  return c;
}

Segment FabricationEntityStore::put(Segment segment) {
  validate(segment);
  this->segments[segment.id] = segment;
  return segment;
}

SegmentChoice FabricationEntityStore::put(const SegmentChoice &choice) {
  if (segmentChoices.find(choice.segmentId) == segmentChoices.end()) {
    segmentChoices[choice.segmentId] = std::map<UUID, SegmentChoice>();
  }
  segmentChoices[choice.segmentId][choice.id] = choice;
  return choice;
}

SegmentChoiceArrangement FabricationEntityStore::put(const SegmentChoiceArrangement &arrangement) {
  if (segmentChoiceArrangements.find(arrangement.segmentId) == segmentChoiceArrangements.end()) {
    segmentChoiceArrangements[arrangement.segmentId] = std::map<UUID, SegmentChoiceArrangement>();
  }
  segmentChoiceArrangements[arrangement.segmentId][arrangement.id] = arrangement;
  return arrangement;
}

SegmentChoiceArrangementPick FabricationEntityStore::put(const SegmentChoiceArrangementPick &pick) {
  if (segmentChoiceArrangementPicks.find(pick.segmentId) == segmentChoiceArrangementPicks.end()) {
    segmentChoiceArrangementPicks[pick.segmentId] = std::map<UUID, SegmentChoiceArrangementPick>();
  }
  segmentChoiceArrangementPicks[pick.segmentId][pick.id] = pick;
  return pick;
}

SegmentChord FabricationEntityStore::put(const SegmentChord &chord) {
  if (segmentChords.find(chord.segmentId) == segmentChords.end()) {
    segmentChords[chord.segmentId] = std::map<UUID, SegmentChord>();
  }
  segmentChords[chord.segmentId][chord.id] = chord;
  return chord;
}

SegmentChordVoicing FabricationEntityStore::put(const SegmentChordVoicing &voicing) {
  if (segmentChordVoicings.find(voicing.segmentId) == segmentChordVoicings.end()) {
    segmentChordVoicings[voicing.segmentId] = std::map<UUID, SegmentChordVoicing>();
  }
  segmentChordVoicings[voicing.segmentId][voicing.id] = voicing;
  return voicing;
}

SegmentMeme FabricationEntityStore::put(const SegmentMeme &meme) {
  validate(meme);
  if (segmentMemes.find(meme.segmentId) == segmentMemes.end()) {
    segmentMemes[meme.segmentId] = std::map<UUID, SegmentMeme>();
  }
  segmentMemes[meme.segmentId][meme.id] = meme;
  return meme;
}

SegmentMessage FabricationEntityStore::put(const SegmentMessage &message) {
  if (segmentMessages.find(message.segmentId) == segmentMessages.end()) {
    segmentMessages[message.segmentId] = std::map<UUID, SegmentMessage>();
  }
  segmentMessages[message.segmentId][message.id] = message;
  return message;
}

SegmentMeta FabricationEntityStore::put(const SegmentMeta &meta) {
  if (segmentMetas.find(meta.segmentId) == segmentMetas.end()) {
    segmentMetas[meta.segmentId] = std::map<UUID, SegmentMeta>();
  }
  segmentMetas[meta.segmentId][meta.id] = meta;
  return meta;
}

std::optional<Chain> FabricationEntityStore::readChain() {
  return chain;
}

std::optional<Segment> FabricationEntityStore::readSegment(int id) {
  if (segments.find(id) != segments.end()) return {segments[id]};
  return std::nullopt;
}

std::optional<SegmentChoice> FabricationEntityStore::readSegmentChoice(int segmentId, const UUID &id) {
  if (segmentChoices.find(segmentId) == segmentChoices.end()) {
    return std::nullopt;
  }
  if (segmentChoices[segmentId].find(id) == segmentChoices[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChoices[segmentId][id]};
}

std::optional<SegmentChoiceArrangement>
FabricationEntityStore::readSegmentChoiceArrangement(int segmentId, const UUID &id) {
  if (segmentChoiceArrangements.find(segmentId) == segmentChoiceArrangements.end()) {
    return std::nullopt;
  }
  if (segmentChoiceArrangements[segmentId].find(id) == segmentChoiceArrangements[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChoiceArrangements[segmentId][id]};
}

std::optional<SegmentChoiceArrangementPick>
FabricationEntityStore::readSegmentChoiceArrangementPick(int segmentId, const UUID &id) {
  if (segmentChoiceArrangementPicks.find(segmentId) == segmentChoiceArrangementPicks.end()) {
    return std::nullopt;
  }
  if (segmentChoiceArrangementPicks[segmentId].find(id) == segmentChoiceArrangementPicks[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChoiceArrangementPicks[segmentId][id]};
}

std::optional<SegmentChord> FabricationEntityStore::readSegmentChord(int segmentId, const UUID &id) {
  if (segmentChords.find(segmentId) == segmentChords.end()) {
    return std::nullopt;
  }
  if (segmentChords[segmentId].find(id) == segmentChords[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChords[segmentId][id]};
}

std::optional<SegmentChordVoicing> FabricationEntityStore::readSegmentChordVoicing(int segmentId, const UUID &id) {
  if (segmentChordVoicings.find(segmentId) == segmentChordVoicings.end()) {
    return std::nullopt;
  }
  if (segmentChordVoicings[segmentId].find(id) == segmentChordVoicings[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChordVoicings[segmentId][id]};
}

std::optional<SegmentMeme> FabricationEntityStore::readSegmentMeme(int segmentId, const UUID &id) {
  if (segmentMemes.find(segmentId) == segmentMemes.end()) {
    return std::nullopt;
  }
  if (segmentMemes[segmentId].find(id) == segmentMemes[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentMemes[segmentId][id]};
}

std::optional<SegmentMessage> FabricationEntityStore::readSegmentMessage(int segmentId, const UUID &id) {
  if (segmentMessages.find(segmentId) == segmentMessages.end()) {
    return std::nullopt;
  }
  if (segmentMessages[segmentId].find(id) == segmentMessages[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentMessages[segmentId][id]};
}

std::optional<SegmentMeta> FabricationEntityStore::readSegmentMeta(int segmentId, const UUID &id) {
  if (segmentMetas.find(segmentId) == segmentMetas.end()) {
    return std::nullopt;
  }
  if (segmentMetas[segmentId].find(id) == segmentMetas[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentMetas[segmentId][id]};
}

std::set<SegmentChoice> FabricationEntityStore::readAllSegmentChoices(int segmentId) {
  std::set<SegmentChoice> result;
  if (segmentChoices.find(segmentId) == segmentChoices.end()) {
    return result;
  }
  for (auto &choice: segmentChoices[segmentId]) {
    result.emplace(choice.second);
  }
  return result;
}

std::set<SegmentChoiceArrangement> FabricationEntityStore::readAllSegmentChoiceArrangements(int segmentId) {
  std::set<SegmentChoiceArrangement> result;
  if (segmentChoiceArrangements.find(segmentId) == segmentChoiceArrangements.end()) {
    return result;
  }
  for (auto &arrangement: segmentChoiceArrangements[segmentId]) {
    result.emplace(arrangement.second);
  }
  return result;
}

std::set<SegmentChoiceArrangementPick> FabricationEntityStore::readAllSegmentChoiceArrangementPicks(int segmentId) {
  std::set<SegmentChoiceArrangementPick> result;
  if (segmentChoiceArrangementPicks.find(segmentId) == segmentChoiceArrangementPicks.end()) {
    return result;
  }
  for (auto &pick: segmentChoiceArrangementPicks[segmentId]) {
    result.emplace(pick.second);
  }
  return result;
}

std::set<SegmentChord> FabricationEntityStore::readAllSegmentChords(int segmentId) {
  std::set<SegmentChord> result;
  if (segmentChords.find(segmentId) == segmentChords.end()) {
    return result;
  }
  for (auto &chord: segmentChords[segmentId]) {
    result.emplace(chord.second);
  }
  return result;
}

std::set<SegmentChordVoicing> FabricationEntityStore::readAllSegmentChordVoicings(int segmentId) {
  std::set<SegmentChordVoicing> result;
  if (segmentChordVoicings.find(segmentId) == segmentChordVoicings.end()) {
    return result;
  }
  for (auto &voicing: segmentChordVoicings[segmentId]) {
    result.emplace(voicing.second);
  }
  return result;
}

std::set<SegmentMeme> FabricationEntityStore::readAllSegmentMemes(int segmentId) {
  std::set<SegmentMeme> result;
  if (segmentMemes.find(segmentId) == segmentMemes.end()) {
    return result;
  }
  for (auto &meme: segmentMemes[segmentId]) {
    result.emplace(meme.second);
  }
  return result;
}

std::set<SegmentMessage> FabricationEntityStore::readAllSegmentMessages(int segmentId) {
  std::set<SegmentMessage> result;
  if (segmentMessages.find(segmentId) == segmentMessages.end()) {
    return result;
  }
  for (auto &message: segmentMessages[segmentId]) {
    result.emplace(message.second);
  }
  return result;
}

std::set<SegmentMeta> FabricationEntityStore::readAllSegmentMetas(int segmentId) {
  std::set<SegmentMeta> result;
  if (segmentMetas.find(segmentId) == segmentMetas.end()) {
    return result;
  }
  for (auto &meta: segmentMetas[segmentId]) {
    result.emplace(meta.second);
  }
  return result;
}

std::set<SegmentChoice> FabricationEntityStore::readAllSegmentChoices(const std::set<int> &segmentIds) {
  std::set<SegmentChoice> result;
  for (auto &segmentId: segmentIds) {
    if (segmentChoices.find(segmentId) == segmentChoices.end()) {
      continue;
    }
    for (auto &choice: segmentChoices[segmentId]) {
      result.emplace(choice.second);
    }
  }
  return result;
}

std::set<SegmentChoiceArrangement>
FabricationEntityStore::readAllSegmentChoiceArrangements(const std::set<int> &segmentIds) {
  std::set<SegmentChoiceArrangement> result;
  for (auto &segmentId: segmentIds) {
    if (segmentChoiceArrangements.find(segmentId) == segmentChoiceArrangements.end()) {
      continue;
    }
    for (auto &arrangement: segmentChoiceArrangements[segmentId]) {
      result.emplace(arrangement.second);
    }
  }
  return result;
}

std::set<SegmentChoiceArrangementPick>
FabricationEntityStore::readAllSegmentChoiceArrangementPicks(const std::set<int> &segmentIds) {
  std::set<SegmentChoiceArrangementPick> result;
  for (auto &segmentId: segmentIds) {
    if (segmentChoiceArrangementPicks.find(segmentId) == segmentChoiceArrangementPicks.end()) {
      continue;
    }
    for (auto &pick: segmentChoiceArrangementPicks[segmentId]) {
      result.emplace(pick.second);
    }
  }
  return result;
}

std::set<SegmentChord> FabricationEntityStore::readAllSegmentChords(const std::set<int> &segmentIds) {
  std::set<SegmentChord> result;
  for (auto &segmentId: segmentIds) {
    if (segmentChords.find(segmentId) == segmentChords.end()) {
      continue;
    }
    for (auto &chord: segmentChords[segmentId]) {
      result.emplace(chord.second);
    }
  }
  return result;
}

std::set<SegmentChordVoicing> FabricationEntityStore::readAllSegmentChordVoicings(const std::set<int> &segmentIds) {
  std::set<SegmentChordVoicing> result;
  for (auto &segmentId: segmentIds) {
    if (segmentChordVoicings.find(segmentId) == segmentChordVoicings.end()) {
      continue;
    }
    for (auto &voicing: segmentChordVoicings[segmentId]) {
      result.emplace(voicing.second);
    }
  }
  return result;
}

std::set<SegmentMeme> FabricationEntityStore::readAllSegmentMemes(const std::set<int> &segmentIds) {
  std::set<SegmentMeme> result;
  for (auto &segmentId: segmentIds) {
    if (segmentMemes.find(segmentId) == segmentMemes.end()) {
      continue;
    }
    for (auto &meme: segmentMemes[segmentId]) {
      result.emplace(meme.second);
    }
  }
  return result;
}

std::set<SegmentMessage> FabricationEntityStore::readAllSegmentMessages(const std::set<int> &segmentIds) {
  std::set<SegmentMessage> result;
  for (auto &segmentId: segmentIds) {
    if (segmentMessages.find(segmentId) == segmentMessages.end()) {
      continue;
    }
    for (auto &message: segmentMessages[segmentId]) {
      result.emplace(message.second);
    }
  }
  return result;
}

std::set<SegmentMeta> FabricationEntityStore::readAllSegmentMetas(const std::set<int> &segmentIds) {
  std::set<SegmentMeta> result;
  for (auto &segmentId: segmentIds) {
    if (segmentMetas.find(segmentId) == segmentMetas.end()) {
      continue;
    }
    for (auto &meta: segmentMetas[segmentId]) {
      result.emplace(meta.second);
    }
  }
  return result;
}

std::optional<Segment> FabricationEntityStore::readSegmentAtChainMicros(long chainMicros) {
  for (auto &segment: segments) {
    if (SegmentUtils::isSpanning(segment.second, chainMicros, chainMicros)) {
      return {segment.second};
    }
  }
  return std::nullopt;
}

std::vector<Segment> FabricationEntityStore::readAllSegments() {
  std::vector<Segment> result;
  for (auto &segment: segments) {
    result.emplace_back(segment.second);
  }
  return result;
}

std::vector<Segment> FabricationEntityStore::readSegmentsFromToOffset(int fromOffset, int toOffset) {
  std::vector<Segment> result;
  for (auto &segment: segments) {
    if (segment.second.id >= fromOffset && segment.second.id <= toOffset) {
      result.emplace_back(segment.second);
    }
  }
  return result;
}

std::set<SegmentEntity> FabricationEntityStore::readAllSegmentEntities(const std::set<int> &segmentIds) {
  std::set<SegmentEntity> result;
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

std::vector<Segment> FabricationEntityStore::readAllSegmentsSpanning(long fromChainMicros, long toChainMicros) {
  std::vector<Segment> result;
  for (auto &segment: segments) {
    if (SegmentUtils::isSpanning(segment.second, fromChainMicros, toChainMicros)) {
      result.emplace_back(segment.second);
    }
  }
  return result;
}

int FabricationEntityStore::readLastSegmentId() {
  if (segments.empty()) {
    return 0;
  }
  return segments.rbegin()->first;
}

std::optional<Segment> FabricationEntityStore::readSegmentLast() {
  if (segments.empty()) {
    return std::nullopt;
  }
  return {segments.rbegin()->second};
}


std::optional<SegmentChoice> FabricationEntityStore::readChoice(int segmentId, Program::Type programType) {
  if (segmentChoices.find(segmentId) == segmentChoices.end()) {
    return std::nullopt;
  }
  for (auto &choice: segmentChoices[segmentId]) {
    if (choice.second.programType == programType) {
      return {choice.second};
    }
  }
  return std::nullopt;
}

std::string FabricationEntityStore::readChoiceHash(const XJ::Segment& segment) {
  std::set<SegmentEntity> entities = readAllSegmentEntities({segment.id});
  std::vector<std::string> ids;

  for (const auto &entity: entities) {
    ids.push_back(entity.id);
  }

  std::sort(ids.begin(), ids.end());
  return StringUtils::join(ids, "_");
}


int FabricationEntityStore::getSegmentCount() {
  return static_cast<int>(segments.size());
}

bool FabricationEntityStore::isEmpty() {
  return segments.empty();
}

void FabricationEntityStore::updateSegment(Segment &segment) {
// validate and cache to-state
  validate(segment);
  Segment::State toState = segment.state;

  // fetch existing segment; further logic is based on its current state
  std::optional<Segment> existingOpt = readSegment(segment.id);
  if (!existingOpt.has_value()) {
    throw FabricationException("Segment #" + std::to_string(segment.id) + " does not exist");
  }
  Segment existing = existingOpt.value();

  // logic based on existing Segment State
  protectSegmentStateTransition(existing.state, toState);

  // fail if attempt to [#128] change chainId of a segment
  std::optional<UUID> updateChainId = segment.chainId;
  if (updateChainId.has_value() && updateChainId.value() != existing.chainId) {
    throw FabricationException("cannot modify chainId of a Segment");
  }

  // Never change id
  segment.id = existing.id;

  // Updated at is always now
  segment.updatedAt = Entity::currentTimeMillis();

  // save segment
  put(segment);
}

void FabricationEntityStore::deleteChain() {
  chain = std::nullopt;
}

void FabricationEntityStore::deleteSegment(int id) {
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

void FabricationEntityStore::deleteSegmentChoice(int segmentId, const UUID &id) {
  if (segmentChoices.find(segmentId) == segmentChoices.end()) {
    return;
  }
  segmentChoices[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChoiceArrangement(int segmentId, const UUID &id) {
  if (segmentChoiceArrangements.find(segmentId) == segmentChoiceArrangements.end()) {
    return;
  }
  segmentChoiceArrangements[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChoiceArrangementPick(int segmentId, const UUID &id) {
  if (segmentChoiceArrangementPicks.find(segmentId) == segmentChoiceArrangementPicks.end()) {
    return;
  }
  segmentChoiceArrangementPicks[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChord(int segmentId, const UUID &id) {
  if (segmentChords.find(segmentId) == segmentChords.end()) {
    return;
  }
  segmentChords[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChordVoicing(int segmentId, const UUID &id) {
  if (segmentChordVoicings.find(segmentId) == segmentChordVoicings.end()) {
    return;
  }
  segmentChordVoicings[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentMeme(int segmentId, const UUID &id) {
  if (segmentMemes.find(segmentId) == segmentMemes.end()) {
    return;
  }
  segmentMemes[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentMessage(int segmentId, const UUID &id) {
  if (segmentMessages.find(segmentId) == segmentMessages.end()) {
    return;
  }
  segmentMessages[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentMeta(int segmentId, const UUID &id) {
  if (segmentMetas.find(segmentId) == segmentMetas.end()) {
    return;
  }
  segmentMetas[segmentId].erase(id);
}

void FabricationEntityStore::protectSegmentStateTransition(Segment::State fromState, Segment::State toState) {
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
      onlyAllowSegmentStateTransitions(toState, {
          Segment::State::Planned
      });
      break;
  }
}

void FabricationEntityStore::onlyAllowSegmentStateTransitions(
    Segment::State toState,
    const std::set<Segment::State> &allowedStates
) {
  std::vector<std::string> allowedStateNames;
  allowedStateNames.reserve(allowedStates.size());
  for (Segment::State search: allowedStates) {
    allowedStateNames.emplace_back(Segment::toString(search));
    if (search == toState) {
      return;
    }
  }
  throw FabricationException(
      "transition to " + Segment::toString(toState) + " not in allowed (" + StringUtils::join(allowedStateNames, ",") +
      ")");
}

void FabricationEntityStore::validate(SegmentMeme entity) {
  entity.name = StringUtils::toMeme(entity.name);
}


void FabricationEntityStore::validate(Segment entity) {
  entity.updatedAt = Entity::currentTimeMillis();
}

void FabricationEntityStore::deleteSegmentsBefore(int lastSegmentId) {
  std::set<int> idsToDelete;
  for (auto &segment: segments) {
    if (segment.first < lastSegmentId) {
      idsToDelete.insert(segment.first);
    }
  }
  for (auto &id: idsToDelete) {
    deleteSegment(id);
  }
}

void FabricationEntityStore::deleteSegmentsAfter(int lastSegmentId) {
  std::set<int> idsToDelete;
  for (auto &segment: segments) {
    if (segment.first > lastSegmentId) {
      idsToDelete.insert(segment.first);
    }
  }
  for (auto &id: idsToDelete) {
    deleteSegment(id);
  }
}

void FabricationEntityStore::clear() {
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




