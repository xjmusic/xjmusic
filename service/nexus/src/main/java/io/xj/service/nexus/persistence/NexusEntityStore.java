// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.persistence;

import io.xj.lib.entity.Entity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 Nexus service record store interface
 <p>
 [#171553408] XJ Mk3 Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface NexusEntityStore {

  /**
   Put a {@link N} into the record store.
   Requires PayloadDataType.HasOne in order to infer the entity type and id, for computing the key

   @param <N>    types of entities
   @param entity to store
   @return payload that was stored, for chaining methods
   @throws NexusEntityStoreException on failure to persist the specified payload
   */
  <N extends Entity> N put(N entity) throws NexusEntityStoreException;

  /**
   Put all entities into the store, keyed by their individual type and id

   @param <N>      types of entities
   @param entities to put into the store
   @return collection of entities just added to the store
   */
  <N extends Entity> Collection<N> putAll(Collection<N> entities) throws NexusEntityStoreException;

  /**
   Get an entity by type and id from the record store in the form of a {@link N}
   Will always have PayloadDataType.HasOne in order to have inferred the entity type and id

   @param <N> types of entities
   @return N of given type and id
   @throws NexusEntityStoreException on failure to retrieve the requested key
   */
  <N extends Entity> Optional<N> get(Class<N> type, UUID id) throws NexusEntityStoreException;

  /**
   Get all entities by type from the record store

   @param <N>  types of entities
   @param type of entity
   @return collection of given type
   @throws NexusEntityStoreException on failure to retrieve the requested key
   */
  <N extends Entity> Collection<N> getAll(Class<N> type) throws NexusEntityStoreException;

  /**
   Get all entities by type and a belongs-to relationship from the record store

   @param <N>           types of entities
   @param <B>           types of belongs-to entities
   @param type          of entity
   @param belongsToType type of belongs-to entity
   @param belongsToIds  ids of belongs-to entity
   @return collection of given type
   @throws NexusEntityStoreException on failure to retrieve the requested key
   */
  <N extends Entity, B extends Entity> Collection<N> getAll(Class<N> type, Class<B> belongsToType, Collection<UUID> belongsToIds) throws NexusEntityStoreException;

  /**
   Delete an entity specified by class and id

   @param <N>  types of entities
   @param type of class to delete
   @param id   to delete
   */
  <N extends Entity> void delete(Class<N> type, UUID id);

  /**
   Delete all records in the store (e.g. during integration testing)
   */
  void deleteAll();

  /**
   Delete all records in the store of a given type belonging to a specified record

   @param <N>           types of entities
   @param <B>           types of belongs-to entities
   @param type          of record to delete all of
   @param belongsToType type of parent to filter entities by
   @param belongsToId   id of parent to filter entities by
   */
  <N extends Entity, B extends Entity> void deleteAll(Class<N> type, Class<B> belongsToType, UUID belongsToId) throws NexusEntityStoreException;

  /**
   Delete all records in the store of a given type

   @param <N>  types of entities
   @param type of record to delete all of
   */
  <N extends Entity> void deleteAll(Class<N> type);
}
