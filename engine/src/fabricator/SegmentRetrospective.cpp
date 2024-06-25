// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <utility>

#include "xjmusic/fabricator/FabricationFatalException.h"
#include "xjmusic/fabricator/SegmentRetrospective.h"
#include "xjmusic/fabricator/FabricationException.h"

using namespace XJ;

SegmentRetrospective::SegmentRetrospective(SegmentEntityStore *entityStore, const int segmentId) {
  this->entityStore = entityStore;
  this->segmentId = segmentId;
}

void SegmentRetrospective::load() {
  // NOTE: the segment retrospective is empty for segments of type Initial, NextMain, and NextMacro--
  // Only Continue-type segments have a retrospective

  // begin by getting the previous segment
  // only can build retrospective if there is at least one previous segment
  // the previous segment is the first one cached here. we may cache even further back segments below if found
  if (segmentId <= 0) {
    retroSegments = std::vector<Segment *>();
    previousSegmentIds = std::set<int>();
    previousSegment = std::nullopt;
    return;
  }

  // begin by getting the previous segment
  // the previous segment is the first one cached here. we may cache even further back segments below if found
  previousSegment = entityStore->readSegment(segmentId - 1);

  if (!previousSegment.has_value()) {
    throw FabricationFatalException("Retrospective sees no previous segment!");
  }

  // previous segment must have a main choice to continue past here.
  const std::optional<SegmentChoice *> previousSegmentMainChoice =
      entityStore->readChoice(previousSegment.value()->id, Program::Type::Main);

  if (!previousSegmentMainChoice.has_value() || previousSegmentMainChoice.value()->programType != Program::Type::Main) {
    throw FabricationFatalException("Retrospective sees no main choice!");
  }

  retroSegments = entityStore->readAllSegments();

  for (const auto &s: retroSegments) {
    auto c = entityStore->readChoice(s->id, Program::Type::Main);
    if (c.has_value() && previousSegmentMainChoice.value()->programId == c.value()->programId) {
      previousSegmentIds.emplace(s->id);
    }
  }
}

std::optional<SegmentChoice *>
SegmentRetrospective::getPreviousChoiceOfType(const Segment *segment, const Program::Type programType) {
  const auto c = entityStore->readChoice(segment->id, programType);
  return (c.has_value() && programType == c.value()->programType) ? c : std::nullopt;
}

std::set<SegmentChoiceArrangementPick *> SegmentRetrospective::getPicks() {
  // return new ArrayList<>(retroStore.getAll(SegmentChoiceArrangementPick.class));
  return entityStore->readAllSegmentChoiceArrangementPicks(previousSegmentIds);
}

std::optional<Segment *> SegmentRetrospective::getPreviousSegment() {
  return previousSegment;
}

std::optional<SegmentChoice *> SegmentRetrospective::getPreviousChoiceOfType(const Program::Type programType) {
  if (!previousSegment.has_value()) return std::nullopt;
  return getPreviousChoiceOfType(previousSegment.value(), programType);
}

std::set<SegmentChoice *> SegmentRetrospective::getPreviousChoicesOfMode(const Instrument::Mode instrumentMode) {
  std::set<SegmentChoice *> result;
  if (!previousSegment.has_value()) return result;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentMode == instrumentMode) {
      result.emplace(choice);
    }
  }
  return result;
}

std::set<SegmentChoice *>
SegmentRetrospective::getPreviousChoicesOfTypeMode(const Instrument::Type instrumentType, const Instrument::Mode instrumentMode) {
  std::set<SegmentChoice *> result;
  if (!previousSegment.has_value()) return result;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentMode == instrumentMode && choice->instrumentType == instrumentType) {
      result.emplace(choice);
    }
  }
  return result;
}

std::optional<SegmentChoice *> SegmentRetrospective::getPreviousChoiceOfType(const Instrument::Type instrumentType) {
  if (!previousSegment.has_value()) return std::nullopt;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentType == instrumentType) {
      return choice;
    }
  }
  return std::nullopt;
}

std::vector<Segment *> SegmentRetrospective::getSegments() {
  return retroSegments;
}

std::set<SegmentChoice *> SegmentRetrospective::getChoices() {
  return entityStore->readAllSegmentChoices(previousSegmentIds);
}

std::set<SegmentChoice *> SegmentRetrospective::getPreviousChoicesForInstrument(const UUID &instrumentId) {
  std::set<SegmentChoice *> result;
  if (!previousSegment.has_value()) return result;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentId == instrumentId) {
      result.emplace(choice);
    }
  }
  return result;
}

std::set<SegmentChoiceArrangement *> SegmentRetrospective::getPreviousArrangementsForInstrument(UUID instrumentId) {
  std::set<SegmentChoiceArrangement *> result;
  const auto choices = getPreviousChoicesForInstrument(std::move(instrumentId));
  const auto arrangements = entityStore->readAllSegmentChoiceArrangements(previousSegmentIds);
  for (const auto &choice: choices) {
    for (const auto &arrangement: arrangements) {
      if (arrangement->segmentChoiceId == choice->id) {
        result.emplace(arrangement);
      }
    }
  }
  return result;
}

std::set<SegmentChoiceArrangementPick *> SegmentRetrospective::getPreviousPicksForInstrument(UUID instrumentId) {
  std::set<SegmentChoiceArrangementPick *> result;
  if (!previousSegment.has_value()) return result;
  const auto arrangements = getPreviousArrangementsForInstrument(std::move(instrumentId));
  const auto picks = entityStore->readAllSegmentChoiceArrangementPicks(
      previousSegmentIds);
  for (const auto &arrangement: arrangements) {
    for (const auto &pick: picks) {
      if (pick->segmentChoiceArrangementId == arrangement->id) {
        result.emplace(pick);
      }
    }
  }
  return result;
}

Instrument::Type SegmentRetrospective::getInstrumentType(const SegmentChoiceArrangementPick *pick) {
  return getChoice(getArrangement(pick))->instrumentType;
}

std::optional<SegmentMeta *> SegmentRetrospective::getPreviousMeta(const std::string &key) {
  const auto metas = entityStore->readAllSegmentMetas(previousSegmentIds);
  for (const auto &meta: metas) {
    if (meta->key == key) {
      return meta;
    }
  }
  return std::nullopt;
}

SegmentChoiceArrangement * SegmentRetrospective::getArrangement(const SegmentChoiceArrangementPick *pick) {
  const auto arrangements = entityStore->readAllSegmentChoiceArrangements(pick->segmentId);
  for (const auto &arrangement: arrangements) {
    if (arrangement->id == pick->segmentChoiceArrangementId) {
      return arrangement;
    }
  }
  throw FabricationException("Failed to get arrangement for SegmentChoiceArrangementPick[" + pick->id + "]");
}

SegmentChoice *SegmentRetrospective::getChoice(const SegmentChoiceArrangement *arrangement) {
  const auto choices = entityStore->readAllSegmentChoices(arrangement->segmentId);
  for (const auto &choice: choices) {
    if (choice->id == arrangement->segmentChoiceId) {
      return choice;
    }
  }
  throw FabricationException("Failed to get arrangement for SegmentChoiceArrangement[" + arrangement->id + "]");
}

std::vector<SegmentChord *> SegmentRetrospective::getSegmentChords(const int segmentId) {
  if (segmentChords.find(segmentId) == segmentChords.end()) {
    auto chords = entityStore->readAllSegmentChords(segmentId);
    auto sortedChords = std::vector(chords.begin(), chords.end());
    std::sort(sortedChords.begin(), sortedChords.end(),
              [](const SegmentChord *a, const SegmentChord *b) {
                return a->position < b->position;
              });
    segmentChords[segmentId] = sortedChords;
  }

  return segmentChords[segmentId];
}

