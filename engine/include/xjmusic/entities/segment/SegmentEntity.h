// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_ENTITY_H
#define SEGMENT_ENTITY_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"
#include "xjmusic/entities/content/Instrument.h"
#include "xjmusic/entities/content/Program.h"

namespace XJ {

  class SegmentEntity : public Entity {
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
      return lhs.id < rhs.id;
    }

  };

}// namespace XJ

#endif//SEGMENT_ENTITY_H
