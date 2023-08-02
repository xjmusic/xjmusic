// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.persistence;

import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.model.SegmentState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SegmentManager extends Manager<Segment> {

  /**
   * Create a Message in a given Segment
   *
   * @param access control
   * @param entity Segment Message, including belong-to Segment ID
   * @return newly created Segment message
   * @throws ManagerValidationException on failure
   * @throws ManagerPrivilegeException  on failure
   * @throws ManagerExistenceException  on failure
   * @throws ManagerFatalException      on failure
   */
  SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws ManagerValidationException, ManagerPrivilegeException, ManagerExistenceException, ManagerFatalException;

  /**
   * Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
   *
   * @param access control
   * @param entity Segment Meta, including belong-to Segment ID
   * @return newly created Segment meta
   * @throws ManagerValidationException on failure
   * @throws ManagerPrivilegeException  on failure
   * @throws ManagerExistenceException  on failure
   * @throws ManagerFatalException      on failure
   */
  SegmentMeta create(HubClientAccess access, SegmentMeta entity) throws ManagerValidationException, ManagerPrivilegeException, ManagerExistenceException, ManagerFatalException;

  /**
   * Get the segments that span the given instant
   *
   * @param chainId         for which to get segments
   * @param fromChainMicros for which to get segments
   * @param toChainMicros   for which to get segments
   * @return segments that span the given instant, empty if none found
   */
  List<Segment> readAllSpanning(UUID chainId, Long fromChainMicros, Long toChainMicros);

  /**
   * Get the segment at the given chain microseconds, if it is ready
   * segment beginning <= chain microseconds <= end
   * <p>
   * Note this algorithm intends to get the latter segment when the lookup point is on the line between two segments
   *
   * @param chainId     the chain id for which to get the segment
   * @param chainMicros the chain microseconds for which to get the segment
   * @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
   */
  Optional<Segment> readOneAtChainMicros(UUID chainId, long chainMicros);

  /**
   * Fetch id for the Segment in a Chain at a given offset, if present
   *
   * @param chainId to fetch segment for
   * @param offset  to fetch segment at
   * @return segment id
   */
  Optional<Segment> readOneAtChainOffset(UUID chainId, Long offset);

  /**
   * Fetch one Segment by chainId and state, if present
   *
   * @param access                        control
   * @param chainId                       to find segment in
   * @param segmentState                  segmentState to find segment in
   * @param segmentBeginBeforeChainMicros ahead to look for segments
   * @return Segment if found
   * @throws ManagerFatalException     on failure
   * @throws ManagerExistenceException if the entity does not exist
   * @throws ManagerPrivilegeException if access is prohibited
   */
  Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Long segmentBeginBeforeChainMicros) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException;

  /**
   * Fetch all sub-entities records for many parent segments by id
   *
   * @param segmentIds   to fetch records for.
   * @param includePicks whether to include the segment choice arrangement picks
   * @return collection of all sub entities of these parent segments, different classes that extend Entity
   * @throws ManagerFatalException     on failure
   * @throws ManagerFatalException     if the entity does not exist
   * @throws ManagerPrivilegeException if access is prohibited
   */
  <N> Collection<N> readManySubEntities(Collection<UUID> segmentIds, Boolean includePicks) throws ManagerPrivilegeException, ManagerFatalException;

  /**
   * Create all sub-entities for a given segment
   * does not actually check if all; these entities belong to the same sub-entity,
   * this is a top-level access method only@param entities to of
   */
  <N> void createAllSubEntities(Collection<N> entities) throws ManagerPrivilegeException, ManagerFatalException;

  /**
   * Read all Segments that are accessible, by Chain ID, starting and ending at particular offsets
   *
   * @param chainId    to read all segments of
   * @param fromOffset to read segments form
   * @param toOffset   to read segments to
   * @return array of segments as JSON
   * @throws ManagerFatalException     on failure
   * @throws ManagerExistenceException if the entity does not exist
   * @throws ManagerPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromToOffset(UUID chainId, Long fromOffset, Long toOffset) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   * Read the last segment in a Chain, Segments sorted by offset ascending
   *
   * @param chainId of chain
   * @return Last Segment in Chain
   */
  Optional<Segment> readLastSegment(UUID chainId) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   * Read the last dubbed-state segment in a Chain, Segments sorted by offset ascending
   *
   * @param access  control
   * @param chainId of chain
   * @return Last Dubbed-state Segment in Chain
   */
  Optional<Segment> readLastCraftedSegment(HubClientAccess access, UUID chainId) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   * Read a choice for a given segment id and program type
   *
   * @param segmentId   for which to get choice
   * @param programType to get
   * @return main choice
   */
  Optional<SegmentChoice> readChoice(UUID segmentId, ProgramType programType) throws ManagerFatalException;

  /**
   * Whether the given segment exists in the store
   *
   * @param id to test
   * @return true if segment exists
   */
  boolean exists(UUID id);

  /**
   * Get chain for segment
   *
   * @param segment for which to get chain
   * @return chain for segment
   */
  Chain getChain(Segment segment) throws NexusException, ManagerFatalException;
}
