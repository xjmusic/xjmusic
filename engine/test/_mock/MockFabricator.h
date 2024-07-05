// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_MOCK_FABRICATOR_H
#define XJMUSIC_MOCK_FABRICATOR_H

#include <gmock/gmock.h>

#include "xjmusic/fabricator/Fabricator.h"

namespace XJ {

  class MockFabricator final : public Fabricator {
  public:
    // Constructor
    MockFabricator(
        ContentEntityStore *contentEntityStore,
        SegmentEntityStore *segmentEntityStore,
        SegmentRetrospective *segmentRetrospective,
        const int segmentId,
        const std::optional<Segment::Type> overrideSegmentType) : Fabricator(contentEntityStore,
                                                                       segmentEntityStore,
                                                                       segmentRetrospective,
                                                                       segmentId,
                                                                       overrideSegmentType) {}

    MOCK_METHOD(SegmentRetrospective *, getRetrospective, (), (override));
    MOCK_METHOD(ContentEntityStore *, getSourceMaterial, (), (override));
    MOCK_METHOD(const Segment *, getSegment, (), (override));
    MOCK_METHOD(MemeIsometry, getMemeIsometryOfSegment, (), (override));
  };

}// namespace XJ
#endif//XJMUSIC_MOCK_FABRICATOR_H
