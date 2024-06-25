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

    MOCK_METHOD(std::set<SegmentChoiceArrangementPick*>, getPreviousPicksForInstrument, (UUID instrumentId));
    MOCK_METHOD(SegmentChoiceArrangement *, getArrangement, (const SegmentChoiceArrangementPick * pick));
    MOCK_METHOD(std::set<SegmentChoice *>, getChoices, ());
    MOCK_METHOD(SegmentChoice, getChoice, (const SegmentChoiceArrangement& arrangement));
    MOCK_METHOD(Instrument::Type, getInstrumentType, (SegmentChoiceArrangementPick pick));
    MOCK_METHOD(std::optional<SegmentMeta*>, getPreviousMeta, (const std::string& key));
    MOCK_METHOD(std::set<SegmentChoice*>, getPreviousChoicesForInstrument, (const UUID& instrumentId));
    MOCK_METHOD(std::set<SegmentChoiceArrangement*>, getPreviousArrangementsForInstrument, (UUID instrumentId));
    MOCK_METHOD(std::optional<SegmentChoice*>, getPreviousChoiceOfType, (const Segment &segment, Program::Type type));
    MOCK_METHOD(std::optional<SegmentChoice*>, getPreviousChoiceOfType, (Program::Type type));
    MOCK_METHOD(std::set<SegmentChoice*>, getPreviousChoicesOfMode, (Instrument::Mode instrumentMode));
    MOCK_METHOD(std::set<SegmentChoice*>, getPreviousChoicesOfTypeMode, (Instrument::Type instrumentType, Instrument::Mode instrumentModes));
    MOCK_METHOD(std::optional<SegmentChoice*>, getPreviousChoiceOfType, (Instrument::Type instrumentType));
    MOCK_METHOD(std::optional<Segment*>, getPreviousSegment, ());
    MOCK_METHOD(std::vector<Segment*>, getSegments, ());
    MOCK_METHOD(std::vector<SegmentChord*>, getSegmentChords, (int segmentId));
  };

} // namespace XJ

#endif // XJMUSIC_FABRICATOR_MOCK_SEGMENT_RETROSPECTIVE_H
