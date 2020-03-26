// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.Segment;

/**
 Fabricator content = contentFactory.workOn(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 */
@FunctionalInterface
public interface SegmentWorkbenchFactory {
  /**
   Create a workbench to fabricate a particular segment

   @param access  control
   @param segment Segment to be worked on
   @return SegmentWorkbench
   @throws HubException on failure
   */
  SegmentWorkbench workOn(
    @Assisted("access") Access access,
    @Assisted("segment") Segment segment
  ) throws HubException;
}
