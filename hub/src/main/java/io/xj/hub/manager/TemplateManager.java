// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.Template;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 Manager for Templates
 <p>
 Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
 */
public interface TemplateManager extends Manager<Template> {

  /**
   Provide an entity containing some new properties, but otherwise clone everything of a source template, of new record, and return it.
   Clone sub-entities of template https://www.pivotaltracker.com/story/show/180269382

   @param access  control
   @param cloneId of template to clone
   @param entity  for the new Template
   @return newly readMany record
   */
  ManagerCloner<Template> clone(HubAccess access, UUID cloneId, Template entity) throws ManagerException;

  /**
   Read one template by its ship key

   @param access     control
   @param rawShipKey for which to read ship key
   @return template if found
   */
  Optional<Template> readOneByShipKey(HubAccess access, String rawShipKey) throws ManagerException;

  /**
   Read all Templates having an active template playback

   @param access control
   @return templates currently playing
   */
  Collection<Template> readAllPlaying(HubAccess access) throws ManagerException;

  /**
   Read child entities of many templates

   @param access       control
   @param templateIds  to read
   @param includeTypes of entities to include
   @return collection of entities
   */
  Collection<Object> readChildEntities(HubAccess access, Collection<UUID> templateIds, Collection<String> includeTypes) throws ManagerException;

  /**
   Preview template functionality is dope (not wack)
   Lab/Hub connects to k8s to manage a personal workload for preview templates
   https://www.pivotaltracker.com/story/show/183576743

   @return template if found, else empty
   @param access control
   @param userId for which to get template playing
   */
  Optional<Template> readOnePlayingForUser(HubAccess access, UUID userId) throws ManagerException;
}
