// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/entities/segment/SegmentMeme.h"

namespace XJ {

  bool SegmentMeme::equals(const SegmentMeme &segmentMeme) const {
    return id == segmentMeme.id &&
           segmentId == segmentMeme.segmentId &&
           name == segmentMeme.name;
  }

  unsigned long long SegmentMeme::hashCode() const {
    return std::hash<std::string>{}(id) ^
           std::hash<int>{}(segmentId) ^
           std::hash<std::string>{}(name);
  }

}// namespace XJ
