// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.fabricator;


import io.xj.model.ContentStore;
import io.xj.engine.FabricationException;
import io.xj.model.enums.Segment::Type;
import jakarta.annotation.Nullable;

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 <p>
 Unify factory with explicit methods to construct components from Fabricator
 */
public interface FabricatorFactory {
  /**
   Create a fabricator to fabricate a segment

   @param sourceMaterial      from which to fabricate
   @param segmentId           segment to be fabricated
   @param outputFrameRate     output frame rate
   @param outputChannels      output channels
   @param overrideSegmentType override segment type
   @return Fabricator
   @throws FabricationException            on retry-able network or service failure
   @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/workstation/issues/263
   */
  Fabricator fabricate(
    ContentStore sourceMaterial,
    Integer segmentId,
    double outputFrameRate,
    int outputChannels,
    @Nullable Segment::Type overrideSegmentType
  ) throws FabricationException, FabricationFatalException;

  /**
   Create a retrospective to fabricate a particular segment
   <p>
   Fabricator content = contentFactory.workOn(segment);
   ... do things with this content, like craft or dub ...
   content.putReport();

   @param segmentId Segment that's currently on the workbench
   @return SegmentRetrospective
   @throws FabricationException            on retry-able network or service failure
   @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/workstation/issues/263
   */
  SegmentRetrospective loadRetrospective(
    Integer segmentId
  ) throws FabricationException, FabricationFatalException;
}
