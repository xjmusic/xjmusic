// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.entity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 Entity store interface
 <p>
 XJ Lab Distributed Architecture https://www.pivotaltracker.com/story/show/171553408
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface EntityStore {

  /**
   Put an entity into the store.

   @param <N>    types of entities
   @param entity to store
   @return payload that was stored, for chaining methods
   @throws EntityStoreException on failure to persist the specified payload
   */
  <N> N put(N entity) throws EntityStoreException;

  /**
   Put all entities into the store, keyed by their individual type and id

   @param <N>      types of entities
   @param entities to put into the store
   @return collection of entities just added to the store
   */
  <N> Collection<N> putAll(Collection<N> entities) throws EntityStoreException;

  /**
   Get an entity by type and id from the store

   @param <N> types of entities
   @return N of given type and id
   @throws EntityStoreException on failure to retrieve the requested key
   */
  <N> Optional<N> get(Class<N> type, UUID id);

  /**
   Get all entities by type from the store

   @param <N>  types of entities
   @param type of entity
   @return collection of given type
   */
  <N> Collection<N> getAll(Class<N> type);

  /**
   Get all entities by type and a belongs-to relationship from the store

   @param <N>           types of entities
   @param <B>           types of belongs-to entities
   @param type          of entity
   @param belongsToType type of belongs-to entity
   @param belongsToIds  ids of belongs-to entity
   @return collection of given type
   @throws EntityStoreException on failure to retrieve the requested key
   */
  <N, B> Collection<N> getAll(Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws EntityStoreException;

  /**
   Delete an entity specified by class and id@param <N>  types of entities

   @param type of class to delete
   @param id   to delete
   */
  <N> void delete(Class<N> type, UUID id);

  /**
   Get all Entities from the store

   @return all entities from the store
   */
  Collection<Object> getAll();

  /**
   Return the size of the store, in # of entities

   @return size of store
   */
  int size();

}
