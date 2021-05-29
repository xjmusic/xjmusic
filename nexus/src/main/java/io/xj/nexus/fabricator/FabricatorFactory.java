// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.hub.client.HubClientAccess;
import io.xj.hub.client.HubContent;
import io.xj.nexus.NexusException;

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 <p>
 [#176625033] Unify guice factory with explicit methods to construct components from Fabricator
 */
public interface FabricatorFactory {
  /**
   Create a fabricator to fabricate a segment

   @param access  control
   @param segment Segment to be worked on
   @return Fabricator
   @throws NexusException on failure
   */
  Fabricator fabricate(
    @Assisted("access") HubClientAccess access,
    @Assisted("segment") Segment segment
  ) throws NexusException;

  /**
   Create a retrospective to fabricate a particular segment
   <p>
   Fabricator content = contentFactory.workOn(segment);
   ... do things with this content, like craft or dub ...
   content.putReport();

   @param access         control
   @param currentSegment Segment that's on the workbench
   @param sourceMaterial to get answers about the segment content
   @return SegmentRetrospective
   @throws NexusException on failure
   */
  SegmentRetrospective loadRetrospective(
    @Assisted("access") HubClientAccess access,
    @Assisted("currentSegment") Segment currentSegment,
    @Assisted("sourceMaterial") HubContent sourceMaterial
  ) throws NexusException;

  /**
   Configure a TimeComputer instance for a segment
   <p>
   [#153542275] Segment wherein velocity changes expect perfectly smooth sound of previous segment through to following segment

   @param totalBeats of the segment
   @param fromTempo  at the beginning of the segment (in Beats Per Minute)
   @param toTempo    at the end of the segment  (in Beats Per Minute)
   @return TimeComputer instance
   */
  TimeComputer createTimeComputer(
    @Assisted("totalBeats") double totalBeats,
    @Assisted("fromTempo") double fromTempo,
    @Assisted("toTempo") double toTempo
  );

  /**
   Create a workbench to fabricate a particular segment

   @param access  control
   @param segment Segment to be worked on
   @return SegmentWorkbench
   @throws NexusException on failure
   */
  SegmentWorkbench setupWorkbench(
    @Assisted("access") HubClientAccess access,
    @Assisted("chain") Chain chain,
    @Assisted("segment") Segment segment
  ) throws NexusException;
}
