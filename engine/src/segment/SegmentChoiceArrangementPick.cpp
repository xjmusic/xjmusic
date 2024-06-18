// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/SegmentChoiceArrangementPick.h"

namespace XJ {

  bool SegmentChoiceArrangementPick::equals(const SegmentChoiceArrangementPick &segmentChoiceArrangementPick) const {
    return id == segmentChoiceArrangementPick.id &&
           segmentId == segmentChoiceArrangementPick.segmentId &&
           segmentChoiceArrangementId == segmentChoiceArrangementPick.segmentChoiceArrangementId &&
           segmentChordVoicingId == segmentChoiceArrangementPick.segmentChordVoicingId &&
           instrumentAudioId == segmentChoiceArrangementPick.instrumentAudioId &&
           programSequencePatternEventId == segmentChoiceArrangementPick.programSequencePatternEventId &&
           startAtSegmentMicros == segmentChoiceArrangementPick.startAtSegmentMicros &&
           lengthMicros == segmentChoiceArrangementPick.lengthMicros &&
           amplitude == segmentChoiceArrangementPick.amplitude &&
           tones == segmentChoiceArrangementPick.tones &&
           event == segmentChoiceArrangementPick.event;
  }

  unsigned long long SegmentChoiceArrangementPick::hashCode() const {
    return std::hash<UUID>{}(id) ^
           std::hash<int>{}(segmentId) ^
           std::hash<UUID>{}(segmentChoiceArrangementId) ^
           std::hash<UUID>{}(segmentChordVoicingId) ^
           std::hash<UUID>{}(instrumentAudioId) ^
           std::hash<UUID>{}(programSequencePatternEventId) ^
           std::hash<long>{}(startAtSegmentMicros) ^
           std::hash<long>{}(lengthMicros) ^
           std::hash<float>{}(amplitude) ^
           std::hash<std::string>{}(tones) ^
           std::hash<std::string>{}(event);
  }

}// namespace XJ
