// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <utility>

#include "xjmusic/fabricator/FabricationFatalException.h"
#include "xjmusic/fabricator/SegmentRetrospective.h"
#include "xjmusic/fabricator/FabricationException.h"

using namespace XJ;

SegmentRetrospective::SegmentRetrospective(SegmentEntityStore *entityStore, const int segmentId, const bool autoload) {
  this->entityStore = entityStore;
  this->segmentId = segmentId;

  // NOTE: the segment retrospective is empty for segments of type Initial, NextMain, and NextMacro--
  // Only Continue-type segments have a retrospective
  // begin by getting the previous segment
  // only can build retrospective if there is at least one previous segment
  // the previous segment is the first one cached here. we may cache even further back segments below if found
  if (!autoload || segmentId <= 0) {
    retroSegments = std::vector<const Segment *>();
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
  segmentChords.emplace(previousSegment.value()->id, entityStore->readOrderedSegmentChords(previousSegment.value()->id));

  // previous segment must have a main choice to continue past here.
  const std::optional<const SegmentChoice *> previousSegmentMainChoice =
      entityStore->readChoice(previousSegment.value()->id, Program::Type::Main);

  if (!previousSegmentMainChoice.has_value() || previousSegmentMainChoice.value()->programType != Program::Type::Main) {
    throw FabricationFatalException("Retrospective sees no main choice!");
  }

  retroSegments = entityStore->readAllSegments();

  for (const auto &s: retroSegments) {
    auto c = entityStore->readChoice(s->id, Program::Type::Main);
    if (c.has_value() && previousSegmentMainChoice.value()->programId == c.value()->programId) {
      previousSegmentIds.emplace(s->id);
      segmentChords.emplace(s->id, entityStore->readOrderedSegmentChords(s->id));
    }
  }
}

std::optional<const SegmentChoice *>
SegmentRetrospective::getPreviousChoiceOfType(const Segment *segment, const Program::Type programType) const {
  const auto c = entityStore->readChoice(segment->id, programType);
  return c.has_value() && programType == c.value()->programType ? c : std::nullopt;
}

std::set<const SegmentChoiceArrangementPick *> SegmentRetrospective::getPicks() const {
  // return new ArrayList<>(retroStore.getAll(SegmentChoiceArrangementPick.class));
  return entityStore->readAllSegmentChoiceArrangementPicks(previousSegmentIds);
}

std::optional<const Segment *> SegmentRetrospective::getPreviousSegment() const {
  return previousSegment;
}

std::optional<const SegmentChoice *> SegmentRetrospective::getPreviousChoiceOfType(const Program::Type programType) const {
  if (!previousSegment.has_value()) return std::nullopt;
  return getPreviousChoiceOfType(previousSegment.value(), programType);
}

std::set<const SegmentChoice *> SegmentRetrospective::getPreviousChoicesOfMode(const Instrument::Mode instrumentMode) const {
  std::set<const SegmentChoice *> result;
  if (!previousSegment.has_value()) return result;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentMode == instrumentMode) {
      result.emplace(choice);
    }
  }
  return result;
}

std::set<const SegmentChoice *>
SegmentRetrospective::getPreviousChoicesOfTypeMode(const Instrument::Type instrumentType, const Instrument::Mode instrumentMode) const {
  std::set<const SegmentChoice *> result;
  if (!previousSegment.has_value()) return result;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentMode == instrumentMode && choice->instrumentType == instrumentType) {
      result.emplace(choice);
    }
  }
  return result;
}

std::optional<const SegmentChoice *> SegmentRetrospective::getPreviousChoiceOfType(const Instrument::Type instrumentType) const {
  if (!previousSegment.has_value()) return std::nullopt;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentType == instrumentType) {
      return choice;
    }
  }
  return std::nullopt;
}

std::vector<const Segment *> SegmentRetrospective::getSegments() const {
  return retroSegments;
}

std::set<const SegmentChoice *> SegmentRetrospective::getChoices() const {
  return entityStore->readAllSegmentChoices(previousSegmentIds);
}

std::set<const SegmentChoice *> SegmentRetrospective::getPreviousChoicesForInstrument(const UUID &instrumentId) const {
  std::set<const SegmentChoice *> result;
  if (!previousSegment.has_value()) return result;
  const auto choices = entityStore->readAllSegmentChoices(previousSegment.value()->id);
  for (const auto &choice: choices) {
    if (choice->instrumentId == instrumentId) {
      result.emplace(choice);
    }
  }
  return result;
}

std::set<const SegmentChoiceArrangement *> SegmentRetrospective::getPreviousArrangementsForInstrument(const UUID &instrumentId) const {
  std::set<const SegmentChoiceArrangement *> result;
  const auto choices = getPreviousChoicesForInstrument(instrumentId);
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

std::set<const SegmentChoiceArrangementPick *> SegmentRetrospective::getPreviousPicksForInstrument(const UUID &instrumentId) const  {
  std::set<const SegmentChoiceArrangementPick *> result;
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

Instrument::Type SegmentRetrospective::getInstrumentType(const SegmentChoiceArrangementPick *pick) const  {
  return getChoice(getArrangement(pick))->instrumentType;
}

std::optional<const SegmentMeta *> SegmentRetrospective::getPreviousMeta(const std::string &key) const  {
  const auto metas = entityStore->readAllSegmentMetas(previousSegmentIds);
  for (const auto &meta: metas) {
    if (meta->key == key) {
      return meta;
    }
  }
  return std::nullopt;
}

const SegmentChoiceArrangement * SegmentRetrospective::getArrangement(const SegmentChoiceArrangementPick *pick) const  {
  const auto arrangements = entityStore->readAllSegmentChoiceArrangements(pick->segmentId);
  for (const auto &arrangement: arrangements) {
    if (arrangement->id == pick->segmentChoiceArrangementId) {
      return arrangement;
    }
  }
  throw FabricationException("Failed to get arrangement for SegmentChoiceArrangementPick[" + pick->id + "]");
}

const SegmentChoice *SegmentRetrospective::getChoice(const SegmentChoiceArrangement *arrangement) const  {
  const auto choices = entityStore->readAllSegmentChoices(arrangement->segmentId);
  for (const auto &choice: choices) {
    if (choice->id == arrangement->segmentChoiceId) {
      return choice;
    }
  }
  throw FabricationException("Failed to get arrangement for SegmentChoiceArrangement[" + arrangement->id + "]");
}

std::vector<const SegmentChord *> SegmentRetrospective::getSegmentChords(const int segmentId) const  {
  if (segmentChords.find(segmentId) == segmentChords.end()) {
    return std::vector<const SegmentChord *>();
  }
  return segmentChords.at(segmentId);
}

