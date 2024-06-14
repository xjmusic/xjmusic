// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <utility>

#include "xjmusic/fabricator/FabricatorFactory.h"

using namespace XJ;

FabricatorFactory::FabricatorFactory(
    FabricationEntityStore entityStore
) {
  this->entityStore = std::move(entityStore);
}

/*
 * TODO: Implement FabricatorFactory::fabricate
Fabricator fabricate(ContentStore sourceMaterial, Integer segmentId, double outputFrameRate, int outputChannels,
std::optional<Segment::Type>
overrideSegmentType)
throws FabricationException, FabricationFatalException{
    return new FabricatorImpl(this, entityStore, sourceMaterial, segmentId, jsonapiPayloadFactory, jsonProvider, outputFrameRate, outputChannels, overrideSegmentType);
}
*/

SegmentRetrospective FabricatorFactory::loadRetrospective(int segmentId) {
    return SegmentRetrospective(entityStore, segmentId);
}
