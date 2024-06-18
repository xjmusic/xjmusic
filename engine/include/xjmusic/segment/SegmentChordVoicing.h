// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_CHORD_VOICING_H
#define SEGMENT_CHORD_VOICING_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "SegmentEntity.h"

namespace XJ {

  class SegmentChordVoicing : public SegmentEntity {
  public:

    SegmentChordVoicing() = default;

    UUID segmentChordId;
    std::string type;
    std::string notes;

    /**
     * Assert equality with another Segment Chord Voicing
     * @param segmentChordVoicing  The Segment Chord Voicing to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentChordVoicing &segmentChordVoicing) const;

    /**
     * Determine a unique hash code for the Segment Chord Voicing
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

  };

}// namespace XJ

#endif//SEGMENT_CHORD_VOICING_H
