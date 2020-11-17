// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.service.hub.client.HubClientAccess;

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
   @throws FabricationException on failure
   */
  SegmentWorkbench workOn(
    @Assisted("access") HubClientAccess access,
    @Assisted("chain") Chain chain,
    @Assisted("segment") Segment segment
  ) throws FabricationException;
}
