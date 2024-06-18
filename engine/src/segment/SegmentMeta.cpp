// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/SegmentMeta.h"

using namespace XJ;


bool SegmentMeta::equals(const SegmentMeta &segmentMeta) const {
  return id == segmentMeta.id &&
         segmentId == segmentMeta.segmentId &&
         key == segmentMeta.key &&
         value == segmentMeta.value;
}


unsigned long long SegmentMeta::hashCode() const {
  return std::hash<std::string>{}(id) ^
         std::hash<int>{}(segmentId) ^
         std::hash<std::string>{}(key) ^
         std::hash<std::string>{}(value);
}

