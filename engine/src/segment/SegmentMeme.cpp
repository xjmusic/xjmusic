// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/SegmentMeme.h"

using namespace XJ;


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


std::set<std::string> SegmentMeme::getNames(const std::set<SegmentMeme *> &segmentMemes) {
  std::set<std::string> names;
  for (const auto &segmentMeme: segmentMemes) {
    names.insert(segmentMeme->name);
  }
  return names;
}

