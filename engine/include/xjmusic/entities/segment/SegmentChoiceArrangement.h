// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_CHOICE_ARRANGEMENT_H
#define SEGMENT_CHOICE_ARRANGEMENT_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class SegmentChoiceArrangement : public Entity {
  public:

    SegmentChoiceArrangement() = default;

    UUID id;
    int segmentId{};
    UUID segmentChoiceId;
    UUID programSequencePatternId;

    /**
     * Assert equality with another Segment Choice Arrangement
     * @param segmentChoiceArrangement  The Segment Choice Arrangement to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentChoiceArrangement &segmentChoiceArrangement) const;
    
    /**
     * Determine a unique hash code for the Segment Choice Arrangement
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

    /**
     * Compare two Segment Choice Arrangements
     * @param lhs segment choice arrangement
     * @param rhs segment choice arrangement
     * @return true if lhs < rhs
     */
    friend bool operator<(const SegmentChoiceArrangement &lhs, const SegmentChoiceArrangement &rhs) {
      return lhs.id < rhs.id;
    }

  };

}// namespace XJ

#endif//SEGMENT_CHOICE_ARRANGEMENT_H
