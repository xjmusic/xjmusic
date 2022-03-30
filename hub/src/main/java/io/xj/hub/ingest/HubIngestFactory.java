// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.google.inject.assistedinject.Assisted;
import io.xj.hub.access.HubAccess;

import java.util.UUID;

/**
 https://www.pivotaltracker.com/story/show/154350346 Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 HubIngest ingest = evaluationFactory.of(...any combination of libraries, instruments, and sequences...);
 */
@FunctionalInterface
public interface HubIngestFactory {

  /**
   Ingest the entities for the give template
   <p>
   Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569

   @param access control
   @return entities to be evaluated
   @throws HubIngestException on failure to of target entities
   */
  HubIngest ingest(
    @Assisted("access") HubAccess access,
    @Assisted("templateId") UUID templateId
  ) throws HubIngestException;
}
