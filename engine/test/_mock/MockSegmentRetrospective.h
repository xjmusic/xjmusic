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
    MockSegmentRetrospective(SegmentEntityStore *entityStore, int segmentId) : SegmentRetrospective(entityStore, segmentId, false) {
      auto segment = Segment();
    }

    MOCK_METHOD(std::set<const SegmentChoiceArrangementPick *>, getPreviousPicksForInstrument, (const UUID &instrumentId), (const, override));
    MOCK_METHOD(const SegmentChoiceArrangement *, getArrangement, (const SegmentChoiceArrangementPick * pick), (const, override));
    MOCK_METHOD(std::set<const SegmentChoice *>, getChoices, (), (const, override));
    MOCK_METHOD(const SegmentChoice *, getChoice, (const SegmentChoiceArrangement& arrangement), (const));
    MOCK_METHOD(Instrument::Type, getInstrumentType, (SegmentChoiceArrangementPick pick), (const));
    MOCK_METHOD(std::optional<const SegmentMeta*>, getPreviousMeta, (const std::string& key), (const, override));
    MOCK_METHOD(std::set<const SegmentChoice*>, getPreviousChoicesForInstrument, (const UUID& instrumentId), (const, override));
    MOCK_METHOD(std::set<const SegmentChoiceArrangement*>, getPreviousArrangementsForInstrument, (UUID instrumentId), (const));
    MOCK_METHOD(std::optional<const SegmentChoice*>, getPreviousChoiceOfType, (const Segment &segment, Program::Type type), (const));
    MOCK_METHOD(std::optional<const SegmentChoice*>, getPreviousChoiceOfType, (Program::Type type), (const, override));
    MOCK_METHOD(std::set<const SegmentChoice*>, getPreviousChoicesOfMode, (Instrument::Mode instrumentMode), (const, override));
    MOCK_METHOD(std::set<const SegmentChoice*>, getPreviousChoicesOfTypeMode, (Instrument::Type instrumentType, Instrument::Mode instrumentModes), (const, override));
    MOCK_METHOD(std::optional<const SegmentChoice*>, getPreviousChoiceOfType, (Instrument::Type instrumentType), (const, override));
    MOCK_METHOD(std::optional<const Segment*>, getPreviousSegment, (), (const, override));
    MOCK_METHOD(std::vector<const Segment*>, getSegments, (), (const, override));
    MOCK_METHOD(std::vector<const SegmentChord*>, getSegmentChords, (int segmentId), (const, override));
  };

} // namespace XJ

#endif // XJMUSIC_FABRICATOR_MOCK_SEGMENT_RETROSPECTIVE_H
