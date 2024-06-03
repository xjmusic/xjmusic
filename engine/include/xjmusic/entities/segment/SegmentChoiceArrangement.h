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
    int segmentId;
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
  };

}// namespace XJ

#endif//SEGMENT_CHOICE_ARRANGEMENT_H
