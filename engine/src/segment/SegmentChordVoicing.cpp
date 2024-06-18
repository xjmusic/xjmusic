// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/SegmentChordVoicing.h"

using namespace XJ;


bool SegmentChordVoicing::equals(const SegmentChordVoicing &segmentChordVoicing) const {
  return id == segmentChordVoicing.id &&
         segmentId == segmentChordVoicing.segmentId &&
         segmentChordId == segmentChordVoicing.segmentChordId &&
         type == segmentChordVoicing.type &&
         notes == segmentChordVoicing.notes;
}


unsigned long long SegmentChordVoicing::hashCode() const {
  return std::hash<std::string>{}(id) ^
         std::hash<int>{}(segmentId) ^
         std::hash<std::string>{}(segmentChordId) ^
         std::hash<std::string>{}(type) ^
         std::hash<std::string>{}(notes);
}
