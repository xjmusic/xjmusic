// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/SegmentChoice.h"

namespace XJ {

  bool SegmentChoice::equals(const SegmentChoice &segmentChoice) const {
    return id == segmentChoice.id &&
           segmentId == segmentChoice.segmentId &&
           position == segmentChoice.position &&
           name == segmentChoice.name &&
           programId == segmentChoice.programId &&
           programSequenceId == segmentChoice.programSequenceId &&
           programSequenceBindingId == segmentChoice.programSequenceBindingId &&
           programVoiceId == segmentChoice.programVoiceId &&
           instrumentId == segmentChoice.instrumentId &&
           deltaIn == segmentChoice.deltaIn &&
           deltaOut == segmentChoice.deltaOut &&
           mute == segmentChoice.mute &&
           instrumentType == segmentChoice.instrumentType &&
           instrumentMode == segmentChoice.instrumentMode &&
           programType == segmentChoice.programType;
  }

  unsigned long long SegmentChoice::hashCode() const {
    return std::hash<UUID>{}(id) ^
           std::hash<int>{}(segmentId) ^
           std::hash<float>{}(position) ^
           std::hash<std::string>{}(name) ^
           std::hash<UUID>{}(programId) ^
           std::hash<UUID>{}(programSequenceId) ^
           std::hash<UUID>{}(programSequenceBindingId) ^
           std::hash<UUID>{}(programVoiceId) ^
           std::hash<UUID>{}(instrumentId) ^
           std::hash<int>{}(deltaIn) ^
           std::hash<int>{}(deltaOut) ^
           std::hash<bool>{}(mute) ^
           std::hash<int>{}(instrumentType) ^
           std::hash<int>{}(instrumentMode) ^
           std::hash<Program::Type>{}(programType);
  }

}// namespace XJ
