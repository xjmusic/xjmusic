// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.TemplatePublication;

import java.util.Optional;
import java.util.UUID;

/**
 Manager for Template Publication
 <p>
 Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
 */
public interface TemplatePublicationManager extends Manager<TemplatePublication> {

  /**
   Read the one template publication for a given user

   @param access control
   @param userId    for which to read publication
   @return template publication
   @throws ManagerException on failure to read
   */
  Optional<TemplatePublication> readOneForUser(HubAccess access, UUID userId) throws ManagerException;
}
