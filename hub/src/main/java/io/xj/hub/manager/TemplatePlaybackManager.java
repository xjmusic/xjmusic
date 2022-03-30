// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.TemplatePlayback;

import java.util.Optional;
import java.util.UUID;

/**
 Manager for Template Playback
 <p>
 Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
 */
public interface TemplatePlaybackManager extends Manager<TemplatePlayback> {

  /**
   Read the one template playback for a given user

   @param access control
   @param userId    for which to read playback
   @return template playback
   @throws ManagerException on failure to read
   */
  Optional<TemplatePlayback> readOneForUser(HubAccess access, UUID userId) throws ManagerException;
}
