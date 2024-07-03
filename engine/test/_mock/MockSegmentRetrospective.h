// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_MOCK_SEGMENT_RETROSPECTIVE_H
#define XJMUSIC_FABRICATOR_MOCK_SEGMENT_RETROSPECTIVE_H

#include <gmock/gmock.h>

#include <utility>
#include "xjmusic/fabricator/SegmentRetrospective.h"

namespace XJ {

  class MockSegmentRetrospective final : public SegmentRetrospective {
  public:
    // Constructor
    MockSegmentRetrospective(SegmentEntityStore* entityStore, int segmentId)
        : SegmentRetrospective(entityStore, segmentId) {
      auto segment = Segment();
    }

    MOCK_METHOD(std::set<const SegmentChoiceArrangementPick*>, getPreviousPicksForInstrument, (UUID instrumentId));
    MOCK_METHOD(const SegmentChoiceArrangement *, getArrangement, (const SegmentChoiceArrangementPick * pick));
    MOCK_METHOD(std::set<const SegmentChoice *>, getChoices, ());
    MOCK_METHOD(const SegmentChoice *, getChoice, (const SegmentChoiceArrangement& arrangement));
    MOCK_METHOD(Instrument::Type, getInstrumentType, (SegmentChoiceArrangementPick pick));
    MOCK_METHOD(std::optional<const SegmentMeta*>, getPreviousMeta, (const std::string& key));
    MOCK_METHOD(std::set<const SegmentChoice*>, getPreviousChoicesForInstrument, (const UUID& instrumentId));
    MOCK_METHOD(std::set<const SegmentChoiceArrangement*>, getPreviousArrangementsForInstrument, (UUID instrumentId));
    MOCK_METHOD(std::optional<const SegmentChoice*>, getPreviousChoiceOfType, (const Segment &segment, Program::Type type));
    MOCK_METHOD(std::optional<const SegmentChoice*>, getPreviousChoiceOfType, (Program::Type type));
    MOCK_METHOD(std::set<const SegmentChoice*>, getPreviousChoicesOfMode, (Instrument::Mode instrumentMode));
    MOCK_METHOD(std::set<const SegmentChoice*>, getPreviousChoicesOfTypeMode, (Instrument::Type instrumentType, Instrument::Mode instrumentModes));
    MOCK_METHOD(std::optional<const SegmentChoice*>, getPreviousChoiceOfType, (Instrument::Type instrumentType));
    MOCK_METHOD(std::optional<const Segment*>, getPreviousSegment, ());
    MOCK_METHOD(std::vector<const Segment*>, getSegments, ());
    MOCK_METHOD(std::vector<const SegmentChord*>, getSegmentChords, (int segmentId));
  };

} // namespace XJ

#endif // XJMUSIC_FABRICATOR_MOCK_SEGMENT_RETROSPECTIVE_H
