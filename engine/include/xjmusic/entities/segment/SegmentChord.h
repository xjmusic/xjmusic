// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_CHORD_H
#define SEGMENT_CHORD_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class SegmentChord : public Entity {
  public:

    SegmentChord() = default;

    UUID id;
    int segmentId{};
    float position{};
    std::string name;

    /**
     * Assert equality with another Segment Chord
     * @param segmentChord  The Segment Chord to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentChord &segmentChord) const;
    
    /**
     * Determine a unique hash code for the Segment Chord
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

    /**
     * Compare two Segment Chords
     * @param lhs segment chord
     * @param rhs segment chord
     * @return true if lhs < rhs
     */
    friend bool operator<(const SegmentChord &lhs, const SegmentChord &rhs) {
      return lhs.id < rhs.id;
    }
  };

}// namespace XJ

#endif//SEGMENT_CHORD_H
