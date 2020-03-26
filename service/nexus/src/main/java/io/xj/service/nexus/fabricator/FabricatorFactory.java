// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.Segment;

/**
 Fabricator content = contentFactory.fabricate(segment);
 ... do things with this content, like craft or dub ...
 content.putReport();
 */
@FunctionalInterface
public interface FabricatorFactory {
  /**
   Create a fabricator to fabricate a segment

   @return Fabricator
   @throws HubException on failure
   @param access control
   @param segment Segment to be worked on
   */
  Fabricator fabricate(
    @Assisted("access") Access access,
    @Assisted("segment") Segment segment
  ) throws HubException;
}
