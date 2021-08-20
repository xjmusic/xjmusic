// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import io.xj.api.TemplatePlayback;
import io.xj.hub.access.HubAccess;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 DAO for Template Playback
 <p>
 Templates: enhanced preview chain creation for artists in Lab UI #178457569
 */
public interface TemplatePlaybackDAO extends DAO<TemplatePlayback> {

  /**
   Read the one template playback for a given user

   @return template playback
   @throws DAOException on failure to read
   @param hubAccess control
   @param userId    for which to read playback
   */
  Optional<TemplatePlayback> readOneForUser(HubAccess hubAccess, UUID userId) throws DAOException;

  /**
   Fetch all playbacks newer than the default threshold

   @return collection of retrieved records
   @throws DAOException on failure
   @param hubAccess control
   */
  Collection<TemplatePlayback> readAll(HubAccess hubAccess) throws DAOException;
}
