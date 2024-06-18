// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/content/ContentEntityStore.h"

using namespace XJ;

FabricatorFactory::FabricatorFactory(SegmentEntityStore *segmentEntityStore) : segmentEntityStore(segmentEntityStore) {}

Fabricator *FabricatorFactory::fabricate(
    ContentEntityStore *contentEntityStore,
    int segmentId,
    float outputFrameRate,
    int outputChannels,
    std::optional<Segment::Type> overrideSegmentType
) {
  return new Fabricator(
      contentEntityStore,
      segmentEntityStore,
      loadRetrospective(segmentId),
      segmentId,
      outputFrameRate,
      outputChannels,
      overrideSegmentType
  );
}

SegmentRetrospective *FabricatorFactory::loadRetrospective(int segmentId) {
  auto retro = new SegmentRetrospective(segmentEntityStore, segmentId);
  retro->load();
  return retro;
}

