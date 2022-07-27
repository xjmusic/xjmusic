// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.api.Chain;
import io.xj.api.Segment;
import io.xj.nexus.NexusException;
import io.xj.hub.client.HubContent;

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 <p>
 https://www.pivotaltracker.com/story/show/176625033 Unify guice factory with explicit methods to construct components from Fabricator
 */
public interface FabricatorFactory {
  /**
   Create a fabricator to fabricate a segment

   @param sourceMaterial from which to fabricate
   @param segment        segment to be fabricated
   @return Fabricator
   @throws NexusException       on retry-able network or service failure
   @throws FabricationFatalException on failure requiring a chain restart https://www.pivotaltracker.com/story/show/182131722
   */
  Fabricator fabricate(
    @Assisted("sourceMaterial") HubContent sourceMaterial,
    @Assisted("segment") Segment segment
  ) throws NexusException, FabricationFatalException;

  /**
   Create a retrospective to fabricate a particular segment
   <p>
   Fabricator content = contentFactory.workOn(segment);
   ... do things with this content, like craft or dub ...
   content.putReport();

   @param segment        Segment that's on the workbench
   @param sourceMaterial to get answers about the segment content
   @return SegmentRetrospective
   @throws NexusException       on retry-able network or service failure
   @throws FabricationFatalException on failure requiring a chain restart https://www.pivotaltracker.com/story/show/182131722
   */
  SegmentRetrospective loadRetrospective(
    @Assisted("segment") Segment segment,
    @Assisted("sourceMaterial") HubContent sourceMaterial
  ) throws NexusException, FabricationFatalException;

  /**
   Create a workbench to fabricate a particular segment

   @param segment Segment to be worked on
   @return SegmentWorkbench
   @throws NexusException on failure
   */
  SegmentWorkbench setupWorkbench(
    @Assisted("chain") Chain chain,
    @Assisted("segment") Segment segment
  ) throws NexusException;
}
