// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.persistence;

import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Segment;
import io.xj.service.nexus.NexusException;

import java.util.Collection;
import java.util.Optional;

/**
 [#175880468] NexusEntityStore segments and child entities partitioned by segment id for rapid addressing
 <p>
 [#171553408] XJ Lab Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface NexusEntityStore {

  /**
   Put a {@link N} into the record store.
   Requires PayloadDataType.HasOne in order to infer the entity type and id, for computing the key

   @param <N>    types of entities
   @param entity to store
   @return payload that was stored, for chaining methods
   @throws NexusException on failure to persist the specified payload
   */
  <N> N put(N entity) throws NexusException;

  /**
   Put all entities into the store, keyed by their individual type and id

   @param <N>      types of entities
   @param entities to put into the store
   @return collection of entities just added to the store
   */
  <N> Collection<N> putAll(Collection<N> entities) throws NexusException;

  /**
   Delete a Chain specified by id

   @param id of Chain   to delete
   */
  void deleteChain(String id) throws NexusException;

  /**
   Delete a Chain Binding specified by id

   @param id of ChainBinding to delete
   */
  void deleteChainBinding(String id) throws NexusException;

  /**
   Delete a Segment specified by id

   @param id of Segment   to delete
   */
  void deleteSegment(String id) throws NexusException;

  /**
   Delete a Segment entity specified by partition (segment id), class and id

   @param segmentId partition (segment id) of entity
   @param type      of class to delete
   @param id        to delete
   @param <N>       type of entity
   */
  <N> void delete(String segmentId, Class<N> type, String id) throws NexusException;

  /**
   Delete all entities for a Segment of a given type

   @param segmentId partition (segment id) of entity
   @param type      of class to delete
   @param <N>       type of entities
   */
  <N> void deleteAll(String segmentId, Class<N> type) throws NexusException;

  /**
   Delete all records in the store (e.g. during integration testing)
   */
  void deleteAll() throws NexusException;

  /**
   Retrieve a chain by id

   @param id of chain to retrieve
   @throws NexusException on failure to retrieve the requested key
   */
  Optional<Chain> getChain(String id) throws NexusException;

  /**
   Retrieve a chainBinding by id

   @param id of chainBinding to retrieve
   @throws NexusException on failure to retrieve the requested key
   */
  Optional<ChainBinding> getChainBinding(String id) throws NexusException;

  /**
   Retrieve a segment by id

   @param id of segment to retrieve
   @throws NexusException on failure to retrieve the requested key
   */
  Optional<Segment> getSegment(String id) throws NexusException;

  /**
   Get all chains

   @return collection of chains
   @throws NexusException on failure to retrieve the requested key
   */
  Collection<Chain> getAllChains() throws NexusException;

  /**
   Get all chainBindings for a chain id

   @param chainId to get chainBindings for
   @return collection of chainBindings
   @throws NexusException on failure to retrieve the requested key
   */
  Collection<ChainBinding> getAllChainBindings(
          String chainId
  ) throws NexusException;

  /**
   Get all segments for a chain id

   @param chainId to get segments for
   @return collection of segments
   @throws NexusException on failure to retrieve the requested key
   */
  Collection<Segment> getAllSegments(
          String chainId
  ) throws NexusException;

  /**
   Get an entity by partition (segment id), type, and id from the record store

   @param segmentId partition (segment id) of entity
   @param <N>       type of entity
   @return N of given type and id
   @throws NexusException on failure to retrieve the requested key
   */
  <N> Optional<N> get(String segmentId, Class<N> type, String id) throws NexusException;

  /**
   Get all entities by partition (segment id) and type from the record store

   @param segmentId partition (segment id) of entity
   @param <N>       types of entities
   @param type      of entities
   @return collection of given type
   @throws NexusException on failure to retrieve the requested key
   */
  <N> Collection<N> getAll(
          String segmentId,
          Class<N> type
  ) throws NexusException;

  /**
   Get all entities by partition (segment id), type, and a belongs-to relationship from the record store

   @param segmentId     partition (segment id) of entity
   @param <N>           types of entities
   @param <B>           types of belongs-to entities
   @param type          of entity
   @param belongsToType type of belongs-to entity
   @param belongsToIds  ids of belongs-to entity
   @return collection of given type
   @throws NexusException on failure to retrieve the requested key
   */
  <N, B> Collection<N> getAll(
          String segmentId, Class<N> type,
          Class<B> belongsToType,
          Collection<String> belongsToIds
  ) throws NexusException;

  /**
   Return the size of the store, in # of entities

   @return size of store
   */
  int size();

}
