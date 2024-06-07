// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/SegmentUtils.h"

namespace XJ {

  std::optional<const SegmentChoice *>
  SegmentUtils::findFirstOfType(const std::vector<SegmentChoice> &segmentChoices, Program::Type type) {
    auto it = std::find_if(segmentChoices.begin(), segmentChoices.end(), [type](const SegmentChoice &choice) {
      return choice.programType == type;
    });
    if (it == segmentChoices.end()) {
      return std::nullopt;
    }
    return &*it;
  }

  std::optional<const SegmentChoice *>
  SegmentUtils::findFirstOfType(const std::vector<SegmentChoice> &segmentChoices, Instrument::Type type) {
    auto it = std::find_if(segmentChoices.begin(), segmentChoices.end(), [type](const SegmentChoice &choice) {
      return choice.instrumentType == type;
    });
    if (it == segmentChoices.end()) {
      return std::nullopt;
    }
    return &*it;
  }

  std::string SegmentUtils::getIdentifier(Segment *segment) {
    if (segment == nullptr) {
      return "N/A";
    }
    return segment->storageKey.empty() ? std::to_string(segment->id) : segment->storageKey;
  }

  std::optional<Segment *> SegmentUtils::getLastCrafted(const std::vector<Segment> &segments) {
    auto craftedSegments = getCrafted(segments);
    return getLast(craftedSegments);
  }

  std::optional<Segment *> SegmentUtils::getLast(std::vector<Segment> &segments) {
    if (segments.empty()) {
      return nullptr;
    }
    return &*std::max_element(segments.begin(), segments.end(), [](const Segment &a, const Segment &b) {
      return a.id < b.id;
    });
  }

  std::vector<Segment> SegmentUtils::getCrafted(const std::vector<Segment> &segments) {
    std::vector<Segment> result;
    std::copy_if(segments.begin(), segments.end(), std::back_inserter(result), [](const Segment &segment) {
      return segment.state == Segment::State::Crafted;
    });
    return result;
  }

  bool SegmentUtils::containsAnyValidNotes(SegmentChordVoicing &voicing) {
    return Note::containsAnyValidNotes(voicing.notes);
  }


  bool SegmentUtils::isSpanning(Segment &segment, long long fromChainMicros, long long toChainMicros) {
    if (segment.durationMicros) {
      return segment.beginAtChainMicros + segment.durationMicros > fromChainMicros &&
             segment.beginAtChainMicros <= toChainMicros;
    }
    return false;
  }


  bool SegmentUtils::isIntersecting(Segment &segment, long long atChainMicros, long long thresholdMicros) {
    if (segment.durationMicros && atChainMicros) {
      return segment.beginAtChainMicros + segment.durationMicros + thresholdMicros > atChainMicros &&
             segment.beginAtChainMicros - thresholdMicros <= atChainMicros;
    }
    return false;
  }

  std::string SegmentUtils::getStorageFilename(Segment &segment, const std::string &extension) {
    return segment.storageKey + "." + extension;
  }

  std::string SegmentUtils::getStorageFilename(Segment &segment) {
    return getStorageFilename(segment, "wav");
  }

  std::string SegmentUtils::describe(SegmentChoice &choice) {
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

  long SegmentUtils::getEndAtChainMicros(Segment &segment) {
    return segment.durationMicros ? segment.beginAtChainMicros + segment.durationMicros
                                  : segment.beginAtChainMicros;
  }

  bool SegmentUtils::isSameButUpdated(Segment &s1, Segment &s2) {
    if (s1.id != s2.id)
      return false;

    // true if state has changed
    if (s1.state != s2.state)
      return true;

    // true if updated-at has changed
    return s1.updatedAt != s2.updatedAt;
  }

  long SegmentUtils::getDurationMinMicros(std::vector<Segment> &segments) {
    long micros = 0;
    for (Segment &s: segments)
      if (s.durationMicros && (micros == 0 || s.durationMicros < micros))
        micros = s.durationMicros;
    return micros;
  }

  template<typename N>
  int SegmentUtils::getSegmentId(N &entity) {
    if (dynamic_cast<Segment *>(&entity)) return dynamic_cast<Segment *>(&entity)->id;
    if (dynamic_cast<SegmentChoice *>(&entity)) return dynamic_cast<SegmentChoice *>(&entity)->segmentId;
    if (dynamic_cast<SegmentChoiceArrangement *>(&entity)) return dynamic_cast<SegmentChoiceArrangement *>(&entity)->segmentId;
    if (dynamic_cast<SegmentChoiceArrangementPick *>(&entity)) return dynamic_cast<SegmentChoiceArrangementPick *>(&entity)->segmentId;
    if (dynamic_cast<SegmentChord *>(&entity)) return dynamic_cast<SegmentChord *>(&entity)->segmentId;
    if (dynamic_cast<SegmentChordVoicing *>(&entity)) return dynamic_cast<SegmentChordVoicing *>(&entity)->segmentId;
    if (dynamic_cast<SegmentMeme *>(&entity)) return dynamic_cast<SegmentMeme *>(&entity)->segmentId;
    if (dynamic_cast<SegmentMessage *>(&entity)) return dynamic_cast<SegmentMessage *>(&entity)->segmentId;
    if (dynamic_cast<SegmentMeta *>(&entity)) return dynamic_cast<SegmentMeta *>(&entity)->segmentId;
    throw std::invalid_argument(std::string("Can't get segment id for ") + typeid(entity).name());
  }


} // namespace XJ