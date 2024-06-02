// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;


import io.xj.hub.HubContent;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.SegmentType;
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
   @throws NexusException            on retry-able network or service failure
   @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/workstation/issues/263
   */
  Fabricator fabricate(
    HubContent sourceMaterial,
    Integer segmentId,
    double outputFrameRate,
    int outputChannels,
    @Nullable SegmentType overrideSegmentType
  ) throws NexusException, FabricationFatalException;

  /**
   Create a retrospective to fabricate a particular segment
   <p>
   Fabricator content = contentFactory.workOn(segment);
   ... do things with this content, like craft or dub ...
   content.putReport();

   @param segmentId Segment that's currently on the workbench
   @return SegmentRetrospective
   @throws NexusException            on retry-able network or service failure
   @throws FabricationFatalException on failure requiring a chain restart https://github.com/xjmusic/workstation/issues/263
   */
  SegmentRetrospective loadRetrospective(
    Integer segmentId
  ) throws NexusException, FabricationFatalException;
}
