// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.entity.Segment;

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 */
@FunctionalInterface
public interface FabricatorFactory {
  /**
   Create a fabricator to fabricate a segment

   @param access  control
   @param segment Segment to be worked on
   @return Fabricator
   @throws FabricationException on failure
   */
  Fabricator fabricate(
    @Assisted("access") HubClientAccess access,
    @Assisted("segment") Segment segment
  ) throws FabricationException;
}
