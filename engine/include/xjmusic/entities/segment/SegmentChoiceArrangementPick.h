// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_CHOICE_ARRANGEMENT_PICK_H
#define SEGMENT_CHOICE_ARRANGEMENT_PICK_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class SegmentChoiceArrangementPick : public Entity {
  public:
    SegmentChoiceArrangementPick() = default;

    UUID id;
    int segmentId;
    UUID segmentChoiceArrangementId;
    UUID segmentChordVoicingId;
    UUID instrumentAudioId;
    UUID programSequencePatternEventId;
    long startAtSegmentMicros;
    long lengthMicros;
    float amplitude;
    std::string tones;
    std::string event;

    /**
     * Assert equality with another Segment Choice ArrangementPick
     * @param segmentChoiceArrangementPick  The Segment Choice ArrangementPick to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentChoiceArrangementPick &segmentChoiceArrangementPick) const;

    /**
     * Determine a unique hash code for the Segment Choice ArrangementPick
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;
  };

}// namespace XJ

#endif//SEGMENT_CHOICE_ARRANGEMENT_PICK_H
