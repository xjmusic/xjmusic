// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.ingest.Ingest;
import io.xj.service.hub.model.Segment;

/**
 Fabricator content = contentFactory.workOn(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 */
@FunctionalInterface
public interface SegmentRetrospectiveFactory {
  /**
   Create a retrospective to fabricate a particular segment

   @param access         control
   @param currentSegment Segment that's on the workbench
   @param sourceMaterial to get answers about the segment content
   @return SegmentRetrospective
   @throws HubException on failure
   */
  SegmentRetrospective workOn(
    @Assisted("access") Access access,
    @Assisted("currentSegment") Segment currentSegment,
    @Assisted("sourceMaterial") Ingest sourceMaterial) throws HubException;
}
