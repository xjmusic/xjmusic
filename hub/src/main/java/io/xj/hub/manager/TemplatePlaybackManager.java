// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.TemplatePlayback;

import java.util.Optional;
import java.util.UUID;

/**
 Manager for Template Playback
 <p>
 Templates: enhanced preview chain creation for artists in Lab UI #178457569
 */
public interface TemplatePlaybackManager extends Manager<TemplatePlayback> {

  /**
   Read the one template playback for a given user

   @param hubAccess control
   @param userId    for which to read playback
   @return template playback
   @throws ManagerException on failure to read
   */
  Optional<TemplatePlayback> readOneForUser(HubAccess hubAccess, UUID userId) throws ManagerException;
}
