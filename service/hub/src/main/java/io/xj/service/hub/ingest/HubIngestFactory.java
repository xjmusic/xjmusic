// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.access.HubAccess;

import java.util.Set;

/**
 [#154350346] Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 HubIngest ingest = evaluationFactory.of(...any combination of libraries, instruments, and sequences...);
 */
@FunctionalInterface
public interface HubIngestFactory {

  /**
   Evaluate any combination of Entities for ingest. Assumes inclusion of child entities of all entities provided

   @param hubAccess control
   @return entities to be evaluated
   @throws HubIngestException on failure to of target entities
   */
  HubIngest ingest(
    @Assisted("hubAccess") HubAccess hubAccess,
    @Assisted("libraryIds") Set<String> libraryIds,
    @Assisted("programIds") Set<String> programIds,
    @Assisted("instrumentIds") Set<String> instrumentIds
  ) throws HubIngestException;
}
