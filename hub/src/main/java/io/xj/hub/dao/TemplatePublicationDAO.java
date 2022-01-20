// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.TemplatePublication;

import java.util.Optional;
import java.util.UUID;

/**
 DAO for Template Publication
 <p>
 Templates: enhanced preview chain creation for artists in Lab UI #178457569
 */
public interface TemplatePublicationDAO extends DAO<TemplatePublication> {

  /**
   Read the one template publication for a given user

   @param hubAccess control
   @param userId    for which to read publication
   @return template publication
   @throws DAOException on failure to read
   */
  Optional<TemplatePublication> readOneForUser(HubAccess hubAccess, UUID userId) throws DAOException;
}
