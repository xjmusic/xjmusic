// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import io.xj.hub.enums.ProgramType;
import io.xj.engine.FabricationException;
import io.xj.engine.model.Chain;
import io.xj.engine.model.Segment;
import io.xj.engine.model.SegmentChoice;
import io.xj.engine.model.SegmentChoiceArrangementPick;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 FabricationEntityStore segments and child entities partitioned by segment id for rapid addressing https://github.com/xjmusic/workstation/issues/276
 <p>
 XJ Lab Distributed Architecture https://github.com/xjmusic/workstation/issues/207
 Chains, ChainBindings, TemplateConfigs, Segments and all Segment content sub-entities persisted in memory
 */
public interface FabricationEntityStore {

  /**
   Put a {@link N} into the record store.
   Requires PayloadDataType.HasOne in order to infer the entity type and id, for computing the key

   @param <N>    types of entities
   @param entity to store
   @return payload that was stored, for chaining methods
   @throws FabricationException on failure to persist the specified payload
   */
  <N> N put(N entity) throws FabricationException;

  /**
   Get an entity by partition (segment id), type, and id from the record store

   @param <N>       type of entity
   @param segmentId partition (segment id) of entity
   @return N of given type and id
   @throws FabricationException on failure to retrieve the requested key
   */
  <N> Optional<N> read(int segmentId, Class<N> type, UUID id) throws FabricationException;

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
   @throws FabricationException on failure to retrieve the requested key
   */
  <N, B> Collection<N> readAll(
    int segmentId,
    Class<N> type,
    Class<B> belongsToType,
    Collection<UUID> belongsToIds
  ) throws FabricationException;

  /**
   Get all picks for the given segments

   @param segments for which to get picks
   @return picks
   */
  List<SegmentChoiceArrangementPick> readPicks(List<Segment> segments) throws FabricationException;

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
   @throws FabricationException on failure to retrieve the requested key
   */
  List<Segment> readAllSegments() throws FabricationException;

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
   */
  <N> Collection<N> readManySubEntities(Collection<Integer> segmentIds, Boolean includePicks);

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

   @param segment for the updated Entity.
   @throws FabricationException on failure
   */
  void updateSegment(Segment segment) throws FabricationException;

  /**
   Delete a Segment entity specified by partition (segment id), class and id

   @param <N>       type of entity
   @param segmentId partition (segment id) of entity
   @param type      of class to delete
   @param id        to delete
   */
  <N> void delete(int segmentId, Class<N> type, UUID id);

  /**
   Delete all entities for a Segment of a given type

   @param <N>       type of entities
   @param segmentId partition (segment id) of entity
   @param type      of class to delete
   */
  <N> void clear(Integer segmentId, Class<N> type) throws FabricationException;

  /**
   Delete all records in the store (e.g. during integration testing)
   */
  void clear() throws FabricationException;

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
