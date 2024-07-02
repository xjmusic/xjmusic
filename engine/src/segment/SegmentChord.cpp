// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/SegmentChord.h"

using namespace XJ;


bool SegmentChord::equals(const SegmentChord &segmentChord) const {
  return id == segmentChord.id &&
         segmentId == segmentChord.segmentId &&
         position == segmentChord.position &&
         name == segmentChord.name;
}


unsigned long long SegmentChord::hashCode() const {
  return std::hash<std::string>{}(id) ^
         std::hash<int>{}(segmentId) ^
         std::hash<float>{}(position) ^
         std::hash<std::string>{}(name);
}


std::set<std::string> SegmentChord::getNames(const std::set<const SegmentChord *> &segmentChords) {
  std::set<std::string> names;
  for (const auto &segmentChord: segmentChords) {
    names.insert(segmentChord->name);
  }
  return names;
}

