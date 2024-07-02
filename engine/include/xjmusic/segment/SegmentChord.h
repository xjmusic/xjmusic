// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_CHORD_H
#define SEGMENT_CHORD_H

#include <set>
#include <string>

#include "SegmentEntity.h"

namespace XJ {

  class SegmentChord : public SegmentEntity {
  public:
    SegmentChord() = default;

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
     * Get the names of a set of Segment Chords
     * @param segmentChords  The set of Segment Chords
     * @return       The names
     */
    static std::set<std::string> getNames(const std::set<const SegmentChord *> &segmentChords);
  };

}// namespace XJ

#endif//SEGMENT_CHORD_H
