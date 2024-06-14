// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_FABRICATOR_FACTORY_H
#define XJMUSIC_FABRICATOR_FABRICATOR_FACTORY_H

#include "xjmusic/entities/segment/SegmentEntityStore.h"
#include "SegmentRetrospective.h"

namespace XJ {

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 <p>
 Unify factory with explicit methods to construct components from Fabricator
 */
  class FabricatorFactory {
  public:

    /**
     * Constructor from entity store
     * @param entityStore  for factory
     */
    explicit FabricatorFactory(SegmentEntityStore &entityStore);

    /**
     Create a fabricator to fabricate a segment

     @param sourceMaterial      from which to fabricate
     @param segmentId           segment to be fabricated
     @param outputFrameRate     output frame rate
     @param outputChannels      output channels
     @param overrideSegmentType override segment type
     @return Fabricator
     @throws FabricationException            on retry-able network or service failure
     @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/xjmusic/issues/263
     */
/*
 * TODO: Implement FabricatorFactory::fabricate
    Fabricator fabricate(
        ContentEntityStore sourceMaterial,
        int segmentId,
        double outputFrameRate,
        int outputChannels,
        std::optional <Segment::Type> overrideSegmentType
    );
*/

    /**
     Create a retrospective to fabricate a particular segment
     <p>
     Fabricator content = contentFactory.workOn(segment);
     ... do things with this content, like craft or dub ...
     content.putReport();

     @param segmentId Segment that's currently on the workbench
     @return SegmentRetrospective
     @throws FabricationException            on retry-able network or service failure
     @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/xjmusic/issues/263
     */
    SegmentRetrospective loadRetrospective(
        int segmentId
    );

  private:
    SegmentEntityStore &entityStore;

  };

}// namespace XJ

#endif//XJMUSIC_FABRICATOR_FABRICATOR_FACTORY_H