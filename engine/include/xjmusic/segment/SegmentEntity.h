// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_ENTITY_H
#define SEGMENT_ENTITY_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "xjmusic/content/Instrument.h"
#include "xjmusic/content/Program.h"

namespace XJ {

  class SegmentEntity {
  public:
    SegmentEntity() = default;

    UUID id;
    int segmentId{};

    /**
     * Compare two Segment Entities
     * @param lhs segment entity
     * @param rhs segment entity
     * @return true if lhs < rhs
     */
    friend bool operator<(const SegmentEntity &lhs, const SegmentEntity &rhs) {
      if (lhs.segmentId == rhs.segmentId)
        return lhs.id < rhs.id;
      return lhs.segmentId < rhs.segmentId;
    }

  };

}// namespace XJ

#endif//SEGMENT_ENTITY_H
