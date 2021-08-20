// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.google.inject.assistedinject.Assisted;
import io.xj.hub.access.HubAccess;

import java.util.Collection;
import java.util.UUID;

/**
 [#154350346] Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 HubIngest ingest = evaluationFactory.of(...any combination of libraries, instruments, and sequences...);
 */
@FunctionalInterface
public interface HubIngestFactory {

  /**
   Ingest the entities for the give template
   <p>
   Templates: enhanced preview chain creation for artists in Lab UI #178457569

   @param hubAccess control
   @return entities to be evaluated
   @throws HubIngestException on failure to of target entities
   */
  HubIngest ingest(
    @Assisted("hubAccess") HubAccess hubAccess,
    @Assisted("templateId") UUID templateId
  ) throws HubIngestException;
}
