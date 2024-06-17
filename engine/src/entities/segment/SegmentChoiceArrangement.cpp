// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/entities/segment/SegmentChoiceArrangement.h"

namespace XJ {

  bool SegmentChoiceArrangement::equals(const SegmentChoiceArrangement &segmentChoiceArrangement) const {
    return id == segmentChoiceArrangement.id &&
           segmentId == segmentChoiceArrangement.segmentId &&
           segmentChoiceId == segmentChoiceArrangement.segmentChoiceId &&
           programSequencePatternId == segmentChoiceArrangement.programSequencePatternId;
  }

  unsigned long long SegmentChoiceArrangement::hashCode() const {
    return std::hash<std::string>{}(id) ^
           std::hash<int>{}(segmentId) ^
           std::hash<std::string>{}(segmentChoiceId) ^
           std::hash<std::string>{}(programSequencePatternId);
  }

}// namespace XJ
