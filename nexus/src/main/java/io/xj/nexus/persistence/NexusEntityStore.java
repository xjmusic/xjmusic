// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.hub.enums.ProgramType;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 NexusEntityStore segments and child entities partitioned by segment id for rapid addressing https://www.pivotaltracker.com/story/show/175880468
 <p>
 XJ Lab Distributed Architecture https://www.pivotaltracker.com/story/show/171553408
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in memory
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
   */
  <N> void createAll(Collection<N> entities) throws NexusException;

  /**
   Create a new Record

   @param segment for the new Record
   @return newly readMany record
   @throws ManagerFatalException on failure
   */
  Segment createSegment(Segment segment) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;

  /**
   Get an entity by partition (segment id), type, and id from the record store

   @param <N>       type of entity
   @param segmentId partition (segment id) of entity
   @return N of given type and id
   @throws NexusException on failure to retrieve the requested key
   */
  <N> Optional<N> read(int segmentId, Class<N> type, UUID id) throws NexusException;

  /**
   Get all entities by partition (segment id) and type from the record store

   @param <N>       types of entities
   @param segmentId partition (segment id) of entity
   @param type      of entities
   @return collection of given type
   */
  <N> Collection<N> readAll(
    int segmentId,
    Class<N> type
  );

  /**
   Get all entities by partition (segment id), type, and a belongs-to relationship from the record store

   @param <N>           types of entities
   @param <B>           types of belongs-to entities
   @param segmentId     partition (segment id) of entity
   @param type          of entity
   @param belongsToType type of belongs-to entity
   @param belongsToIds  ids of belongs-to entity
   @return collection of given type
   @throws NexusException on failure to retrieve the requested key
   */
  <N, B> Collection<N> readAll(
    int segmentId,
    Class<N> type,
    Class<B> belongsToType,
    Collection<UUID> belongsToIds
  ) throws NexusException;

  /**
   Get all picks for the given segments

   @param segments for which to get picks
   @return picks
   */
  List<SegmentChoiceArrangementPick> readPicks(List<Segment> segments) throws NexusException;

  /**
   Get the last known segment id

   @return last segment id
   */
  int readLastSegmentId();

  /**
   Retrieve the chain
   */
  Optional<Chain> readChain();

  /**
   Retrieve a segment by id

   @param id of segment to retrieve
   */
  Optional<Segment> readSegment(int id);

  /**
   Read the last segment in a Chain, Segments sorted by offset ascending

   @return Last Segment in Chain
   */
  Optional<Segment> readSegmentLast();

  /**
   Get the segment at the given chain microseconds, if it is ready
   segment beginning <= chain microseconds <= end
   <p>
   Note this algorithm intends to get the latter segment when the lookup point is on the line between two segments

   @param chainMicros the chain microseconds for which to get the segment
   @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
   */
  Optional<Segment> readSegmentAtChainMicros(long chainMicros);

  /**
   Get the segments that span the given instant

   @param fromChainMicros for which to get segments
   @param toChainMicros   for which to get segments
   @return segments that span the given instant, empty if none found
   */
  List<Segment> readAllSegmentsSpanning(Long fromChainMicros, Long toChainMicros);

  /**
   Get all segments for a chain id

   @return collection of segments
   @throws NexusException on failure to retrieve the requested key
   */
  List<Segment> readAllSegments() throws NexusException;

  /**
   Read all Segments that are accessible, by Chain ID, starting and ending at particular offsets

   @param fromOffset to read segments form
   @param toOffset   to read segments to
   @return list of segments as JSON
   */
  List<Segment> readSegmentsFromToOffset(int fromOffset, int toOffset);

  /**
   Fetch all sub-entities records for many parent segments by id

   @param segmentIds   to fetch records for.
   @param includePicks whether to include the segment choice arrangement picks
   @return collection of all sub entities of these parent segments, different classes that extend Entity
   @throws ManagerFatalException     on failure
   @throws ManagerFatalException     if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  <N> Collection<N> readManySubEntities(Collection<Integer> segmentIds, Boolean includePicks) throws ManagerPrivilegeException, ManagerFatalException;

  /**
   Fetch all sub-entities records for a parent segments by id

   @param segmentId for which to fetch records
   @param <N>       type of sub-entity
   @return collection of all sub entities of these parent segments, of the given type
   */
  <N> Collection<N> readManySubEntitiesOfType(int segmentId, Class<N> type);

  /**
   Fetch all sub-entities records for many parent segments by ids

   @param segmentIds for which to fetch records
   @param <N>        type of sub-entity
   @return collection of all sub entities of these parent segments, of the given type
   */
  <N> Collection<N> readManySubEntitiesOfType(Collection<Integer> segmentIds, Class<N> type);

  /**
   Read a choice for a given segment id and program type

   @param segmentId   for which to get choice
   @param programType to get
   @return main choice
   */
  Optional<SegmentChoice> readChoice(int segmentId, ProgramType programType);

  /**
   Get a hash of all the choices for the given segment

   @param segment for which to get the choice hash
   @return hash of all the ids of the choices for the given segment
   */
  String readChoiceHash(Segment segment);

  /**
   Update a specified Entity

   @param segmentId of specific Entity to update.
   @param segment   for the updated Entity.
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  void updateSegment(int segmentId, Segment segment) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;

  /**
   Delete a Segment entity specified by partition (segment id), class and id

   @param <N>       type of entity
   @param segmentId partition (segment id) of entity
   @param type      of class to delete
   @param id        to delete
   */
  <N> void delete(int segmentId, Class<N> type, UUID id) throws NexusException;

  /**
   Delete all entities for a Segment of a given type

   @param <N>       type of entities
   @param segmentId partition (segment id) of entity
   @param type      of class to delete
   */
  <N> void clear(Integer segmentId, Class<N> type) throws NexusException;

  /**
   Delete all records in the store (e.g. during integration testing)
   */
  void clear() throws NexusException;

  /**
   Delete all segments before the given segment id

   @param lastSegmentId segment id
   */
  void deleteSegmentsBefore(int lastSegmentId);

  /**
   Delete a segment by id

   @param segmentId segment id
   */
  void deleteSegment(int segmentId);

  /**
   Delete all segments after the given segment id

   @param lastSegmentId segment id
   */
  void deleteSegmentsAfter(int lastSegmentId);

  /**
   Get the total number of segments in the store

   @return number of segments
   */
  Integer getSegmentCount();

  /**
   Whether the segment manager is completely empty

   @return true if there are zero segments
   */
  Boolean isEmpty();
}
