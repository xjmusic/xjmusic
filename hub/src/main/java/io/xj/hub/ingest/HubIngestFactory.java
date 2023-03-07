// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;


import io.xj.hub.access.HubAccess;

import java.util.UUID;

/**
 * Architect wants a universal HubIngest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose. https://www.pivotaltracker.com/story/show/154350346
 * HubIngest ingest = evaluationFactory.of(...any combination of libraries, instruments, and sequences...);
 */
public interface HubIngestFactory {

  /**
   * Ingest the entities for the give template
   * <p>
   * Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
   *
   * @param access control
   * @return entities to be evaluated
   * @throws HubIngestException on failure to of target entities
   */
  HubIngest ingest(
     HubAccess access,
     UUID templateId
  ) throws HubIngestException;
}
