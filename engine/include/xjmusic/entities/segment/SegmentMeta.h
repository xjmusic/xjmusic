// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_META_H
#define SEGMENT_META_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class SegmentMeta : public Entity {
  public:

    SegmentMeta() = default;

    UUID id;
    int segmentId;
    std::string key;
    std::string value;

    /**
     * Assert equality with another Segment Meta
     * @param segmentMeta  The Segment Meta to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentMeta &segmentMeta) const;
    
    /**
     * Determine a unique hash code for the Segment Meta
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;
  };

}// namespace XJ

#endif//SEGMENT_META_H
