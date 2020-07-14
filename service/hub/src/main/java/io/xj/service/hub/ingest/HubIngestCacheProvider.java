// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import io.xj.service.hub.access.HubAccess;

import java.util.Set;
import java.util.UUID;

/**
 [#154350346] Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 HubIngest ingest = evaluationFactory.of(...any combination of libraries, instruments, and sequences...);
 */
@FunctionalInterface
public interface HubIngestCacheProvider {

  /**
   Evaluate any combination of Entities for ingest. Assumes inclusion of child entities of all entities provided
   CACHES the result for any hubAccess+entities signature, for N seconds.
   Where N is configurable in system properties `ingest.cache.seconds`

   @param hubAccess     control
   @param libraryIds    to ingest
   @param programIds    to ingest
   @param instrumentIds to ingest
   @return bindings to be ingested
   @throws HubIngestException on failure to of target entities
   */
  HubIngest ingest(HubAccess hubAccess, Set<UUID> libraryIds, Set<UUID> programIds, Set<UUID> instrumentIds) throws HubIngestException;
}
