// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import io.xj.api.Chain;
import io.xj.api.Template;
import io.xj.hub.access.HubAccess;

import java.util.Optional;

/**
 DAO for Templates
 <p>
 Templates: enhanced preview chain creation for artists in Lab UI #178457569
 */
public interface TemplateDAO extends DAO<Template> {

  /**
   Read one template by its embed key

   @param access      control
   @param rawEmbedKey for which to read embed key
   @return template if found
   */
  Optional<Template> readOneByEmbedKey(HubAccess access, String rawEmbedKey) throws DAOException;
}
