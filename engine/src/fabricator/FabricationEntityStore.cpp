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
  this->segments[segment.id] = segment;
  return segment;
}

SegmentChoice FabricationEntityStore::put(const SegmentChoice &choice) {
  if (segmentChoices.find(choice.segmentId) == segmentChoices.end()) {
    segmentChoices[choice.segmentId] = std::map<UUID, SegmentChoice>();
  }
  segmentChoices[choice.segmentId][choice.id] = choice;
}

SegmentChoiceArrangement FabricationEntityStore::put(const SegmentChoiceArrangement &arrangement) {
  if (segmentChoiceArrangements.find(arrangement.segmentId) == segmentChoiceArrangements.end()) {
    segmentChoiceArrangements[arrangement.segmentId] = std::map<UUID, SegmentChoiceArrangement>();
  }
  segmentChoiceArrangements[arrangement.segmentId][arrangement.id] = arrangement;
}

SegmentChoiceArrangementPick FabricationEntityStore::put(const SegmentChoiceArrangementPick &pick) {
  if (segmentChoiceArrangementPicks.find(pick.segmentId) == segmentChoiceArrangementPicks.end()) {
    segmentChoiceArrangementPicks[pick.segmentId] = std::map<UUID, SegmentChoiceArrangementPick>();
  }
  segmentChoiceArrangementPicks[pick.segmentId][pick.id] = pick;
}

SegmentChord FabricationEntityStore::put(const SegmentChord &chord) {
  if (segmentChords.find(chord.segmentId) == segmentChords.end()) {
    segmentChords[chord.segmentId] = std::map<UUID, SegmentChord>();
  }
  segmentChords[chord.segmentId][chord.id] = chord;
}

SegmentChordVoicing FabricationEntityStore::put(const SegmentChordVoicing &voicing) {
  if (segmentChordVoicings.find(voicing.segmentId) == segmentChordVoicings.end()) {
    segmentChordVoicings[voicing.segmentId] = std::map<UUID, SegmentChordVoicing>();
  }
  segmentChordVoicings[voicing.segmentId][voicing.id] = voicing;
}

SegmentMeme FabricationEntityStore::put(const SegmentMeme &meme) {
  if (segmentMemes.find(meme.segmentId) == segmentMemes.end()) {
    segmentMemes[meme.segmentId] = std::map<UUID, SegmentMeme>();
  }
  segmentMemes[meme.segmentId][meme.id] = meme;
}

SegmentMessage FabricationEntityStore::put(const SegmentMessage &message) {
  if (segmentMessages.find(message.segmentId) == segmentMessages.end()) {
    segmentMessages[message.segmentId] = std::map<UUID, SegmentMessage>();
  }
  segmentMessages[message.segmentId][message.id] = message;
}

SegmentMeta FabricationEntityStore::put(const SegmentMeta &meta) {
  if (segmentMetas.find(meta.segmentId) == segmentMetas.end()) {
    segmentMetas[meta.segmentId] = std::map<UUID, SegmentMeta>();
  }
  segmentMetas[meta.segmentId][meta.id] = meta;
}

std::optional<Chain> FabricationEntityStore::readChain() {
  return chain;
}

std::optional<Segment> FabricationEntityStore::readSegment(int id) {
  if (segments.find(id) != segments.end()) return {segments[id]};
  return std::nullopt;
}

std::optional<SegmentChoice> FabricationEntityStore::readSegmentChoice(int segmentId, const UUID& id) {
  if (segmentChoices.find(segmentId) == segmentChoices.end()) {
    return std::nullopt;
  }
  if (segmentChoices[segmentId].find(id) == segmentChoices[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChoices[segmentId][id]};
}

std::optional<SegmentChoiceArrangement> FabricationEntityStore::readSegmentChoiceArrangement(int segmentId, const UUID& id) {
  if (segmentChoiceArrangements.find(segmentId) == segmentChoiceArrangements.end()) {
    return std::nullopt;
  }
  if (segmentChoiceArrangements[segmentId].find(id) == segmentChoiceArrangements[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChoiceArrangements[segmentId][id]};
}

std::optional<SegmentChoiceArrangementPick> FabricationEntityStore::readSegmentChoiceArrangementPick(int segmentId, const UUID& id) {
  if (segmentChoiceArrangementPicks.find(segmentId) == segmentChoiceArrangementPicks.end()) {
    return std::nullopt;
  }
  if (segmentChoiceArrangementPicks[segmentId].find(id) == segmentChoiceArrangementPicks[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChoiceArrangementPicks[segmentId][id]};
}

std::optional<SegmentChord> FabricationEntityStore::readSegmentChord(int segmentId, const UUID& id) {
  if (segmentChords.find(segmentId) == segmentChords.end()) {
    return std::nullopt;
  }
  if (segmentChords[segmentId].find(id) == segmentChords[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChords[segmentId][id]};
}

std::optional<SegmentChordVoicing> FabricationEntityStore::readSegmentChordVoicing(int segmentId, const UUID& id) {
  if (segmentChordVoicings.find(segmentId) == segmentChordVoicings.end()) {
    return std::nullopt;
  }
  if (segmentChordVoicings[segmentId].find(id) == segmentChordVoicings[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentChordVoicings[segmentId][id]};
}

std::optional<SegmentMeme> FabricationEntityStore::readSegmentMeme(int segmentId, const UUID& id) {
  if (segmentMemes.find(segmentId) == segmentMemes.end()) {
    return std::nullopt;
  }
  if (segmentMemes[segmentId].find(id) == segmentMemes[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentMemes[segmentId][id]};
}

std::optional<SegmentMessage> FabricationEntityStore::readSegmentMessage(int segmentId, const UUID& id) {
  if (segmentMessages.find(segmentId) == segmentMessages.end()) {
    return std::nullopt;
  }
  if (segmentMessages[segmentId].find(id) == segmentMessages[segmentId].end()) {
    return std::nullopt;
  }
  return {segmentMessages[segmentId][id]};
}

std::optional<SegmentMeta> FabricationEntityStore::readSegmentMeta(int segmentId, const UUID& id) {
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

std::set<SegmentChoice> FabricationEntityStore::readAllSegmentChoices(const std::set<int>& segmentIds) {
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

std::set<SegmentChoiceArrangement> FabricationEntityStore::readAllSegmentChoiceArrangements(const std::set<int>& segmentIds) {
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

std::set<SegmentChoiceArrangementPick> FabricationEntityStore::readAllSegmentChoiceArrangementPicks(const std::set<int>& segmentIds) {
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

std::set<SegmentChord> FabricationEntityStore::readAllSegmentChords(const std::set<int>& segmentIds) {
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

std::set<SegmentChordVoicing> FabricationEntityStore::readAllSegmentChordVoicings(const std::set<int>& segmentIds) {
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

std::set<SegmentMeme> FabricationEntityStore::readAllSegmentMemes(const std::set<int>& segmentIds) {
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

std::set<SegmentMessage> FabricationEntityStore::readAllSegmentMessages(const std::set<int>& segmentIds) {
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

std::set<SegmentMeta> FabricationEntityStore::readAllSegmentMetas(const std::set<int>& segmentIds) {
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

void FabricationEntityStore::deleteChain() {
  chain = std::nullopt;
}

void FabricationEntityStore::deleteSegment(int id) {
  segments.erase(id);
}

void FabricationEntityStore::deleteSegmentChoice(int segmentId, UUID id) {
  if (segmentChoices.find(segmentId) == segmentChoices.end()) {
    return;
  }
  segmentChoices[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChoiceArrangement(int segmentId, UUID id) {
  if (segmentChoiceArrangements.find(segmentId) == segmentChoiceArrangements.end()) {
    return;
  }
  segmentChoiceArrangements[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChoiceArrangementPick(int segmentId, UUID id) {
  if (segmentChoiceArrangementPicks.find(segmentId) == segmentChoiceArrangementPicks.end()) {
    return;
  }
  segmentChoiceArrangementPicks[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChord(int segmentId, UUID id) {
  if (segmentChords.find(segmentId) == segmentChords.end()) {
    return;
  }
  segmentChords[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentChordVoicing(int segmentId, UUID id) {
  if (segmentChordVoicings.find(segmentId) == segmentChordVoicings.end()) {
    return;
  }
  segmentChordVoicings[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentMeme(int segmentId, UUID id) {
  if (segmentMemes.find(segmentId) == segmentMemes.end()) {
    return;
  }
  segmentMemes[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentMessage(int segmentId, UUID id) {
  if (segmentMessages.find(segmentId) == segmentMessages.end()) {
    return;
  }
  segmentMessages[segmentId].erase(id);
}

void FabricationEntityStore::deleteSegmentMeta(int segmentId, UUID id) {
  if (segmentMetas.find(segmentId) == segmentMetas.end()) {
    return;
  }
  segmentMetas[segmentId].erase(id);
}

/*
  public FabricationEntityStoreImpl(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
  }

  @Override
  public <N> N put(N entity) throws FabricationException {
    validate(entity);

    if (entity instanceof Chain) {
      chain = (Chain) entity;
      return entity;
    }

    if (entity instanceof Segment) {
      segments.put(((Segment) entity).id, (Segment) entity);
      return entity;
    }

    // fail to store entity without id
    UUID id;
    try {
      id = EntityUtils.getId(entity);
    } catch (EntityException e) {
      throw new FabricationException(std::string.format("Can't get id of %s-type entity",
        entity.getClass().getSimpleName()));
    }

    // fail to store entity with unset id
    if (!ValueUtils::isSet(id))
      throw new FabricationException(std::string.format("Can't store %s with null id",
        entity.getClass().getSimpleName()));

    else if (entity instanceof SegmentMeme ||
      entity instanceof SegmentChord ||
      entity instanceof SegmentChordVoicing ||
      entity instanceof SegmentChoice ||
      entity instanceof SegmentMessage ||
      entity instanceof SegmentMeta ||
      entity instanceof SegmentChoiceArrangement ||
      entity instanceof SegmentChoiceArrangementPick)
      try {
        var segmentIdValue = EntityUtils.get(entity, SEGMENT_ID_ATTRIBUTE)
          .orElseThrow(() -> new FabricationException(std::string.format("Can't store %s without Segment ID!",
            entity.getClass().getSimpleName())));
        int segmentId = Integer.parseInt(std::string.valueOf(segmentIdValue));
        if (!entities.containsKey(segmentId))
          entities.put(segmentId, new ConcurrentHashMap<>());
        entities.get(segmentId).putIfAbsent(entity.getClass(), new ConcurrentHashMap<>());
        entities.get(segmentId).get(entity.getClass()).put(id, entity);
      } catch (EntityException e) {
        throw new FabricationException(e);
      }
    else return entity;

    return entity;
  }

  @Override
  public std::optional<Chain> readChain() {
    return std::optional.ofNullable(chain);
  }

  @Override
  public std::optional<Segment> readSegment(int id) {
    return segments.containsKey(id) ? std::optional.of(segments.get(id)) : std::optional.empty();
  }

  @Override
  public std::optional<Segment> readSegmentLast() {
    return readAllSegments()
      .stream()
      .max(Comparator.comparing(Segment::getId));
  }

  @Override
  public std::optional<Segment> readSegmentAtChainMicros(long chainMicros) {
    var segments = readAllSegments()
      .stream()
      .filter(s -> SegmentUtils.isSpanning(s, chainMicros, chainMicros))
      .sorted(Comparator.comparing(Segment::getId))
      .toList();
    return segments.isEmpty() ? std::optional.empty() : std::optional.of(segments.get(segments.size() - 1));
  }

  @Override
  public <N> std::optional<N> read(int segmentId, Class<N> type, UUID id) throws FabricationException {
    try {
      if (!entities.containsKey(segmentId)
        || !entities.get(segmentId).containsKey(type)
        || !entities.get(segmentId).get(type).containsKey(id)) return std::optional.empty();
      //noinspection unchecked
      return (std::optional<N>) std::optional.ofNullable(entities.get(segmentId).get(type).get(id));

    } catch (Exception e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public <N> std::vector<N> readAll(int segmentId, Class<N> type) {
    if (!entities.containsKey(segmentId)
      || !entities.get(segmentId).containsKey(type))
      return std::vector.of();
    //noinspection unchecked
    return (std::vector<N>) entities.get(segmentId).get(type).values().stream()
      .filter(entity -> type.equals(entity.getClass()))
      .collect(Collectors.toList());
  }

  @Override
  public <N, B> std::vector<N> readAll(int segmentId, Class<N> type, Class<B> belongsToType, std::vector<UUID> belongsToIds) {
    if (!entities.containsKey(segmentId)
      || !entities.get(segmentId).containsKey(type))
      return std::vector.of();
    //noinspection unchecked
    return (std::vector<N>) entities.get(segmentId).get(type).values().stream()
      .filter(entity -> EntityUtils.isChild(entity, belongsToType, belongsToIds))
      .collect(Collectors.toList());
  }

  @Override
  public std::vector<Segment> readAllSegments() {
    return segments.values().stream()
      .sorted(Comparator.comparingInt(Segment::getId))
      .collect(Collectors.toList());
  }

  @Override
  public std::vector<Segment> readSegmentsFromToOffset(int fromOffset, int toOffset) {
    return readAllSegments()
      .stream()
      .filter(s -> s.id >= fromOffset && s.id <= toOffset)
      .toList();
  }

  @Override
  public std::vector<Segment> readAllSegmentsSpanning(Long fromChainMicros, Long toChainMicros) {
    return readAllSegments()
      .stream()
      .filter(s -> SegmentUtils.isSpanning(s, fromChainMicros, toChainMicros))
      .toList();
  }

  @Override
  public int readLastSegmentId() {
    return segments.keySet().stream()
      .max(Integer::compareTo)
      .orElse(0);
  }

  @Override
  public std::vector<SegmentChoiceArrangementPick> readPicks(std::vector<Segment> segments) {
    std::vector<SegmentChoiceArrangementPick> picks = new ArrayList<>();
    for (Segment segment : segments) {
      picks.addAll(readAll(segment.id, SegmentChoiceArrangementPick.class));
    }
    return picks;
  }

  @Override
  public <N> std::vector<N> readManySubEntities(std::vector<Integer> segmentIds, Boolean includePicks) {
    std::vector<Object> entities = new ArrayList<>();
    for (Integer sId : segmentIds) {
      entities.addAll(readAll(sId, SegmentChoice.class));
      entities.addAll(readAll(sId, SegmentChoiceArrangement.class));
      entities.addAll(readAll(sId, SegmentChord.class));
      entities.addAll(readAll(sId, SegmentChordVoicing.class));
      entities.addAll(readAll(sId, SegmentMeme.class));
      entities.addAll(readAll(sId, SegmentMessage.class));
      entities.addAll(readAll(sId, SegmentMeta.class));
      if (includePicks)
        entities.addAll(readAll(sId, SegmentChoiceArrangementPick.class));
    }
    //noinspection unchecked
    return (std::vector<N>) entities;
  }

  @Override
  public <N> std::vector<N> readManySubEntitiesOfType(int segmentId, Class<N> type) {
    return readAll(segmentId, type);
  }

  @Override
  public <N> std::vector<N> readManySubEntitiesOfType(std::vector<Integer> segmentIds, Class<N> type) {
    return segmentIds.stream().flatMap(segmentId -> readManySubEntitiesOfType(segmentId, type).stream()).toList();
  }

  @Override
  public std::optional<SegmentChoice> readChoice(int segmentId, Program::Type programType) {
    return readAll(segmentId, SegmentChoice.class)
      .stream()
      .filter(sc -> programType.equals(sc.programType))
      .findAny();
  }

  @Override
  public std::string readChoiceHash(Segment segment) {
    return
      readManySubEntities(Set.of(segment.id), false)
        .stream()
        .flatMap((entity) -> {
          try {
            return Stream.of(EntityUtils.getId(entity));
          } catch (EntityException e) {
            return Stream.empty();
          }
        })
        .map(UUID::toString)
        .sorted()
        .collect(Collectors.joining("_"));

  }

  @Override
  public void updateSegment(Segment segment) throws FabricationException {
    // validate and cache to-state
    validate(segment);
    Segment::State toState = segment.state;

    // fetch existing segment; further logic is based on its current state
    Segment existing = readSegment(segment.id)
      .orElseThrow(() -> new FabricationException("Segment #" + segment.id + " does not exist"));
    if (Objects.isNull(existing)) throw new FabricationException("Segment #" + segment.id + " does not exist");

    // logic based on existing Segment State
    protectSegmentStateTransition(existing.state, toState);

    // fail if attempt to [#128] change chainId of a segment
    Object updateChainId = segment.getChainId();
    if (ValueUtils::isSet(updateChainId) && !Objects.equals(updateChainId, existing.getChainId()))
      throw new FabricationException("cannot change chainId create a segment");

    // Never change id
    segment.setId(segment.id);

    // Updated at is always now
    segment.setUpdatedNow();

    // save segment
    put(segment);

  }

  @Override
  public <N> void delete(int segmentId, Class<N> type, UUID id) {
    if (entities.containsKey(segmentId) && entities.get(segmentId).containsKey(type))
      entities.get(segmentId).get(type).remove(id);
  }

  @Override
  public <N> void clear(Integer segmentId, Class<N> type) {
    for (N entity : readAll(segmentId, type)) {
      try {
        delete(segmentId, type, EntityUtils.getId(entity));
      } catch (EntityException e) {
        LOG.error("Failed to delete {} in Segment[{}]", type.name, segmentId, e);
      }
    }
  }

  @Override
  public void clear() {
    entities.clear();
    segments.clear();
    chain = null;
    LOG.debug("Did delete all records in store");
  }

  @Override
  public void deleteSegmentsBefore(int lastSegmentId) {
    for (var segmentId : segments.keySet().stream()
      .filter(segmentId -> segmentId < lastSegmentId)
      .toList()) {
      segments.remove(segmentId);
      entities.remove(segmentId);
    }
  }

  @Override
  public void deleteSegment(int segmentId) {
    segments.remove(segmentId);
    entities.remove(segmentId);
  }

  @Override
  public void deleteSegmentsAfter(int lastSegmentId) {
    for (var segmentId : segments.keySet().stream()
      .filter(segmentId -> segmentId > lastSegmentId)
      .toList())
      deleteSegment(segmentId);
  }

  @Override
  public Integer getSegmentCount() {
    return segments.size();
  }

  @Override
  public Boolean isEmpty() {
    return segments.isEmpty();
  }
*/

void FabricationEntityStore::protectSegmentStateTransition(Segment::State fromState, Segment::State toState) {
  switch (fromState) {
    case Segment::Planned:
      onlyAllowSegmentStateTransitions(toState, {
          Segment::State::Planned,
          Segment::State::Crafting,
      });
    case Segment::Crafting:
      onlyAllowSegmentStateTransitions(toState, {
          Segment::State::Crafting,
          Segment::State::Crafted,
          Segment::State::Failed,
          Segment::State::Planned,
      });
    case Segment::Crafted:
      onlyAllowSegmentStateTransitions(toState, {
          Segment::State::Crafted,
          Segment::State::Crafting,
      });
    case Segment::Failed:
      onlyAllowSegmentStateTransitions(toState, {
          Segment::State::Failed,
      });
    default:
      onlyAllowSegmentStateTransitions(toState, {
          Segment::State::Planned
      });
  }
}

void FabricationEntityStore::onlyAllowSegmentStateTransitions(Segment::State toState,
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
      "transition to " + Segment::toString(toState) + " not in allowed (" + StringUtils::join(allowedStateNames, ",") +
      ")");
}

void FabricationEntityStore::validate(SegmentMeme entity) {
  entity.name = StringUtils::toMeme(entity.name);
}


void FabricationEntityStore::validate(Segment entity) {
  entity.updatedAt = Entity::currentTimeMillis();
}

