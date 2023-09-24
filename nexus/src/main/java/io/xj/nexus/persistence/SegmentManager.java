// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.persistence;

import io.xj.hub.enums.ProgramType;
import io.xj.nexus.NexusException;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SegmentManager /* does not extend Manager<Segment> because it is special, id is an int (not UUID) */ {

  /**
   Create a Message in a given Segment

   @param access control
   @param entity Segment Message, including belong-to Segment ID
   @return newly created Segment message
   @throws ManagerValidationException on failure
   @throws ManagerPrivilegeException  on failure
   @throws ManagerExistenceException  on failure
   @throws ManagerFatalException      on failure
   */
  SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws ManagerValidationException, ManagerPrivilegeException, ManagerExistenceException, ManagerFatalException;

  /**
   Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787

   @param access control
   @param entity Segment Meta, including belong-to Segment ID
   @return newly created Segment meta
   @throws ManagerValidationException on failure
   @throws ManagerPrivilegeException  on failure
   @throws ManagerExistenceException  on failure
   @throws ManagerFatalException      on failure
   */
  SegmentMeta create(HubClientAccess access, SegmentMeta entity) throws ManagerValidationException, ManagerPrivilegeException, ManagerExistenceException, ManagerFatalException;

  /**
   Get the segments that span the given instant

   @param fromChainMicros for which to get segments
   @param toChainMicros   for which to get segments
   @return segments that span the given instant, empty if none found
   */
  List<Segment> readAllSpanning(Long fromChainMicros, Long toChainMicros);

  /**
   Get the segment at the given chain microseconds, if it is ready
   segment beginning <= chain microseconds <= end
   <p>
   Note this algorithm intends to get the latter segment when the lookup point is on the line between two segments

   @param chainMicros the chain microseconds for which to get the segment
   @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
   */
  Optional<Segment> readOneAtChainMicros(long chainMicros);

  /**
   Fetch id for the Segment in a Chain at a given offset, if present

   @param offset to fetch segment at
   @return segment id
   */
  Optional<Segment> readOneById(int offset);

  /**
   Fetch one Segment by chainId and state, if present

   @param access                        control
   @param segmentState                  segmentState to find segment in
   @param segmentBeginBeforeChainMicros ahead to look for segments
   @return Segment if found
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Segment readFirstInState(HubClientAccess access, SegmentState segmentState, Long segmentBeginBeforeChainMicros) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException;

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
   @throws ManagerFatalException     on failure
   @throws ManagerFatalException     if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  <N> Collection<N> readManySubEntitiesOfType(int segmentId, Class<N> type) throws ManagerPrivilegeException, ManagerFatalException;


  /**
   Fetch all sub-entities records for many parent segments by ids

   @param segmentIds for which to fetch records
   @param <N>        type of sub-entity
   @return collection of all sub entities of these parent segments, of the given type
   */
  <N> Collection<N> readManySubEntitiesOfType(Collection<Integer> segmentIds, Class<N> type);

  /**
   Create all sub-entities for a given segment
   does not actually check if all; these entities belong to the same sub-entity,
   this is a top-level access method only@param entities to of
   */
  <N> void createAllSubEntities(Collection<N> entities) throws ManagerPrivilegeException, ManagerFatalException;

  /**
   Read all Segments that are accessible, by Chain ID, starting and ending at particular offsets

   @param fromOffset to read segments form
   @param toOffset   to read segments to
   @return list of segments as JSON
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  List<Segment> readManyFromToOffset(int fromOffset, int toOffset) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Read the last segment in a Chain, Segments sorted by offset ascending

   @return Last Segment in Chain
   */
  Optional<Segment> readLastSegment() throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Read the last dubbed-state segment in a Chain, Segments sorted by offset ascending

   @param access control
   @return Last Dubbed-state Segment in Chain
   */
  Optional<Segment> readLastCraftedSegment(HubClientAccess access) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Read a choice for a given segment id and program type

   @param segmentId   for which to get choice
   @param programType to get
   @return main choice
   */
  Optional<SegmentChoice> readChoice(int segmentId, ProgramType programType) throws ManagerFatalException;

  /**
   Get chain for segment

   @param segment for which to get chain
   @return chain for segment
   */
  Chain getChain(Segment segment) throws NexusException, ManagerFatalException;

  /**
   Read all segments

   @return all segments for that chain
   */
  List<Segment> readAll();

  /**
   Reset the store to its initial state
   */
  void reset();


  /**
   Create a new Record

   @param segment for the new Record
   @return newly readMany record
   @throws ManagerFatalException on failure
   */
  Segment create(Segment segment) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;

  /**
   Fetch one record  if accessible

   @param segmentId of record to fetch
   @return retrieved record
   @throws ManagerPrivilegeException if access is prohibited
   */
  Segment readOne(int segmentId) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Update a specified Entity

   @param segmentId of specific Entity to update.
   @param segment   for the updated Entity.
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Segment update(int segmentId, Segment segment) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;

  /**
   Get the total number of segments in the store

   @return number of segments
   */
  Integer size();

  /**
   Whether the segment manager is completely empty

   @return true if there are zero segments
   */
  Boolean isEmpty();
}
