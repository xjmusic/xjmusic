// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_FACTORY_H
#define XJMUSIC_FABRICATOR_FACTORY_H

#include "xjmusic/entities/segment/SegmentEntityStore.h"
#include "xjmusic/entities/content/ContentEntityStore.h"

#include "SegmentRetrospective.h"
#include "Fabricator.h"

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
     * @param segmentEntityStore  for factory
     */
    explicit FabricatorFactory(SegmentEntityStore *segmentEntityStore);

    /**
     Create a fabricator to fabricate a segment

     @param contentEntityStore      from which to fabricate
     @param segmentId           segment to be fabricated
     @param outputFrameRate     output frame rate
     @param outputChannels      output channels
     @param overrideSegmentType override segment type
     @return Fabricator
     @throws FabricationException            on retry-able network or service failure
     @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/xjmusic/issues/263
     */
    virtual Fabricator * fabricate(
        ContentEntityStore* contentEntityStore,
        int segmentId,
        float outputFrameRate,
        int outputChannels,
        std::optional<Segment::Type> overrideSegmentType
    );

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
    virtual SegmentRetrospective * loadRetrospective(
        int segmentId
    );

  private:
    SegmentEntityStore* segmentEntityStore;

  };

}// namespace XJ

#endif//XJMUSIC_FABRICATOR_FACTORY_H