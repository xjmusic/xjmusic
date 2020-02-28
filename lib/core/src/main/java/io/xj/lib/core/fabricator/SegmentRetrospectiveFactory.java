// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.ingest.Ingest;
import io.xj.lib.core.model.Segment;

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
   @throws CoreException on failure
   */
  SegmentRetrospective workOn(
    @Assisted("access") Access access,
    @Assisted("currentSegment") Segment currentSegment,
    @Assisted("sourceMaterial") Ingest sourceMaterial) throws CoreException;
}
