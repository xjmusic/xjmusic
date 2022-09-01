// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.persistence;

import io.xj.nexus.model.*;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.client.HubClientAccess;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SegmentManager extends Manager<Segment> {

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
   Fetch id for the Segment in a Chain at a given offset, if present

   @param chainId to fetch segment for
   @param offset  to fetch segment at
   @return segment id
   */
  Segment readOneAtChainOffset(UUID chainId, Long offset) throws ManagerPrivilegeException, ManagerExistenceException, ManagerFatalException;

  /**
   Fetch one Segment by chainId and state, if present

   @param access             control
   @param chainId            to find segment in
   @param segmentState       segmentState to find segment in
   @param segmentBeginBefore ahead to look for segments
   @return Segment if found
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException;

  /**
   Fetch many records for Chain by ship key, if accessible

   @param shipKey to fetch records for.
   @return collection of retrieved records
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyByShipKey(String shipKey) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Fetch all sub-entities records for many parent segments by id

   @param segmentIds   to fetch records for.
   @param includePicks whether to include the segment choice arrangement picks
   @return collection of all sub entities of these parent segments, different classes that extend Entity
   @throws ManagerFatalException     on failure
   @throws ManagerFatalException     if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  <N> Collection<N> readManySubEntities(Collection<UUID> segmentIds, Boolean includePicks) throws ManagerPrivilegeException, ManagerFatalException;

  /**
   Create all sub-entities for a given segment
   does not actually check if all; these entities belong to the same sub-entity,
   this is a top-level access method only@param entities to of
   */
  <N> void createAllSubEntities(Collection<N> entities) throws ManagerPrivilegeException, ManagerFatalException;

  /**
   Read all Segments that are accessible, by Chain ID, starting and ending at particular offsets

   @param chainId    to read all segments of
   @param fromOffset to read segments form
   @param toOffset   to read segments to
   @return array of segments as JSON
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromToOffset(UUID chainId, Long fromOffset, Long toOffset) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Read all Segments that are accessible, by Chain ID, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#278] Chain Player lives in navbar, and handles all playback (audio waveform, segment waveform, continuous chain) so the user always has central control over listening.

   @param access         control
   @param chainId        to read all segments of
   @param fromSecondsUTC to read segments of
   @return array of segments as JSON
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, UUID chainId, Long fromSecondsUTC) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Read all Segments that are accessible, by Chain Ship key, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   https://www.pivotaltracker.com/story/show/150279540 Unauthenticated public Client wants to access a Chain by ship key (as alias for chain id) in order to provide data for playback.

   @param access         control
   @param shipKey        to read all segments of
   @param fromSecondsUTC to read segments of
   @return array of segments as JSON
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromSecondsUTCbyShipKey(HubClientAccess access, String shipKey, Long fromSecondsUTC) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Reverts a segment in Planned state, by destroying all its child entities. Only the segment messages remain, for purposes of debugging.
   https://www.pivotaltracker.com/story/show/158610991 Engineer wants a Segment to be reverted, and re-queued for fabrication, in the event that such a Segment has just failed its fabrication process, in order to ensure Chain fabrication fault tolerance

   @param access control
   @param id     of segment to revert
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   w
   */
  void revert(HubClientAccess access, UUID id) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, ManagerValidationException;

  /**
   Read the last segment in a Chain, Segments sorted by offset ascending

   @param chainId of chain
   @return Last Segment in Chain
   */
  Optional<Segment> readLastSegment(UUID chainId) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Read the last dubbed-state segment in a Chain, Segments sorted by offset ascending

   @param access  control
   @param chainId of chain
   @return Last Dubbed-state Segment in Chain
   */
  Optional<Segment> readLastDubbedSegment(HubClientAccess access, UUID chainId) throws ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException;

  /**
   Read a choice for a given segment id and program type

   @param segmentId   for which to get choice
   @param programType to get
   @return main choice
   */
  Optional<SegmentChoice> readChoice(UUID segmentId, ProgramType programType) throws ManagerFatalException;

  /**
   Whether the given segment exists in the store

   @param id to test
   @return true if segment exists
   */
  boolean exists(UUID id);
}
