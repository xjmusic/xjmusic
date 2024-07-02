// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/SegmentUtils.h"

using namespace XJ;


std::optional<const SegmentChoice *>
SegmentUtils::findFirstOfType(const std::set<const SegmentChoice *> &segmentChoices, Program::Type type) {
  const auto it = std::find_if(segmentChoices.begin(), segmentChoices.end(), [type](const SegmentChoice *choice) {
    return choice->programType == type;
  });
  if (it == segmentChoices.end()) {
    return std::nullopt;
  }
  return *it;
}


std::optional<const SegmentChoice *>
SegmentUtils::findFirstOfType(const std::set<const SegmentChoice *> &segmentChoices, Instrument::Type type) {
  const auto it = std::find_if(segmentChoices.begin(), segmentChoices.end(), [type](const SegmentChoice *choice) {
    return choice->instrumentType == type;
  });
  if (it == segmentChoices.end()) {
    return std::nullopt;
  }
  return *it;
}


std::string SegmentUtils::getIdentifier(const Segment *segment) {
  if (segment == nullptr) {
    return "N/A";
  }
  return segment->storageKey.empty() ? std::to_string(segment->id) : segment->storageKey;
}


std::optional<const Segment *> SegmentUtils::getLastCrafted(const std::vector<const Segment *> &segments) {
  if (segments.empty()) {
    return std::nullopt;
  }
  const auto craftedSegments = getCrafted(segments);
  return getLast(craftedSegments);
}


std::optional<const Segment *> SegmentUtils::getLast(const std::vector<const Segment *> &segments) {
  if (segments.empty()) {
    return std::nullopt;
  }
  return *std::max_element(segments.begin(), segments.end(), [](const Segment *a, const Segment *b) {
    return a->id < b->id;
  });
}


std::vector<const Segment *> SegmentUtils::getCrafted(const std::vector<const Segment *> &segments) {
  std::vector<const Segment *> result;
  std::copy_if(segments.begin(), segments.end(), std::back_inserter(result), [](const Segment *segment) {
    return segment->state == Segment::State::Crafted;
  });
  return result;
}


bool SegmentUtils::containsAnyValidNotes(const SegmentChordVoicing *voicing) {
  return Note::containsAnyValidNotes(voicing->notes);
}


bool SegmentUtils::isSpanning(const Segment *segment, const long long fromChainMicros, const long long toChainMicros) {
  if (segment->durationMicros.has_value()) {
    return segment->beginAtChainMicros + segment->durationMicros.value() > fromChainMicros &&
           segment->beginAtChainMicros <= toChainMicros;
  }
  return false;
}


bool SegmentUtils::isIntersecting(const Segment *segment, const long long atChainMicros, const long long thresholdMicros) {
  if (segment->durationMicros.has_value() && atChainMicros) {
    return segment->beginAtChainMicros + segment->durationMicros.value() + thresholdMicros > atChainMicros &&
           segment->beginAtChainMicros - thresholdMicros <= atChainMicros;
  }
  return false;
}


std::string SegmentUtils::getStorageFilename(const Segment * segment, const std::string &extension) {
  return segment->storageKey + "." + extension;
}


std::string SegmentUtils::getStorageFilename(const Segment *segment) {
  return getStorageFilename(segment, "wav");
}


std::string SegmentUtils::describe(const SegmentChoice &choice) {
  std::vector<std::string> pieces;
  if (!choice.instrumentId.empty())
    pieces.push_back("instrument:" + choice.instrumentId);
  if (choice.instrumentType)
    pieces.push_back("instrumentType:" + Instrument::toString(choice.instrumentType));
  if (!choice.programId.empty()) pieces.push_back("program:" + choice.programId);
  if (!choice.programSequenceBindingId.empty())
    pieces.push_back("programSequenceBinding:" + choice.programSequenceBindingId);
  if (!choice.programSequenceId.empty())
    pieces.push_back("programSequence:" + choice.programSequenceId);
  if (choice.programType)
    pieces.push_back("programType:" + Program::toString(choice.programType));
  if (!choice.programVoiceId.empty())
    pieces.push_back("programVoice:" + choice.programVoiceId);
  return StringUtils::join(pieces, ", ");
}


long SegmentUtils::getEndAtChainMicros(const Segment *segment) {
  return segment->durationMicros.has_value() ? segment->beginAtChainMicros + segment->durationMicros.value()
                                            : segment->beginAtChainMicros;
}


bool SegmentUtils::isSameButUpdated(const Segment *s1, const Segment *s2) {
  if (s1->id != s2->id)
    return false;

  // true if state has changed
  if (s1->state != s2->state)
    return true;

  // true if updated-at has changed
  return s1->updatedAt != s2->updatedAt;
}


long SegmentUtils::getDurationMinMicros(std::vector<Segment> &segments) {
  long micros = 0;
  for (Segment &s: segments)
    if (s.durationMicros.has_value() && (micros == 0 || s.durationMicros.value() < micros))
      micros = s.durationMicros.value();
  return micros;
}

