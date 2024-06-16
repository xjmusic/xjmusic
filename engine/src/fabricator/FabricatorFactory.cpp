// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/FabricatorFactory.h"

using namespace XJ;

FabricatorFactory::FabricatorFactory(SegmentEntityStore *entityStore) : entityStore(entityStore) {}

/*
 * TODO: Implement FabricatorFactory::fabricate
Fabricator fabricate(ContentEntityStore sourceMaterial, Integer segmentId, double outputFrameRate, int outputChannels,
std::optional<Segment::Type>
overrideSegmentType)
throws FabricationException, FabricationFatalException{
    return new FabricatorImpl(this, entityStore, sourceMaterial, segmentId, jsonapiPayloadFactory, jsonProvider, outputFrameRate, outputChannels, overrideSegmentType);
}
*/

SegmentRetrospective * FabricatorFactory::loadRetrospective(int segmentId) {
  return new SegmentRetrospective(entityStore, segmentId);
}
