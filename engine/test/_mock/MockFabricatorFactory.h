// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_MOCK_FABRICATOR_FACTORY_H
#define XJMUSIC_FABRICATOR_MOCK_FABRICATOR_FACTORY_H

#include <gmock/gmock.h>
#include "xjmusic/fabricator/FabricatorFactory.h"

namespace XJ {

  class MockFabricatorFactory : public FabricatorFactory {
  public:
    // Constructor
    explicit MockFabricatorFactory(SegmentEntityStore &entityStore) : FabricatorFactory(entityStore) {}

    // Mock the loadRetrospective method
    MOCK_METHOD(SegmentRetrospective*, loadRetrospective, (int segmentId));
  };

} // namespace XJ

#endif // XJMUSIC_FABRICATOR_MOCK_FABRICATOR_FACTORY_H
