// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.service;

import io.xj.api.Segment;
import io.xj.api.SegmentMessage;
import io.xj.api.SegmentState;
import io.xj.nexus.service.exception.ServiceExistenceException;
import io.xj.nexus.service.exception.ServiceFatalException;
import io.xj.nexus.service.exception.ServicePrivilegeException;
import io.xj.nexus.service.exception.ServiceValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SegmentService extends Service<Segment> {

  /**
   Create a Message in a given Segment

   @param access control
   @param entity Segment Message, including belong-to Segment ID
   @return newly created Segment message
   @throws ServiceValidationException on failure
   @throws ServicePrivilegeException  on failure
   @throws ServiceExistenceException  on failure
   @throws ServiceFatalException      on failure
   */
  SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws ServiceValidationException, ServicePrivilegeException, ServiceExistenceException, ServiceFatalException;

  /**
   Fetch id for the Segment in a Chain at a given offset, if present

   @return segment id
   @param chainId to fetch segment for
   @param offset  to fetch segment at
   */
  Segment readOneAtChainOffset(UUID chainId, Long offset) throws ServicePrivilegeException, ServiceExistenceException, ServiceFatalException;

  /**
   Fetch one Segment by chainId and state, if present

   @param access             control
   @param chainId            to find segment in
   @param segmentState       segmentState to find segment in
   @param segmentBeginBefore ahead to look for segments
   @return Segment if found
   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   */
  Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException;

  /**
   Fetch many records for Chain by ship key, if accessible

   @param access        control
   @param chainShipKey to fetch records for.
   @return collection of retrieved records
   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   */
  Collection<Segment> readManyByShipKey(HubClientAccess access, String chainShipKey) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException;

  /**
   Fetch all sub-entities records for many parent segments by id

   @return collection of all sub entities of these parent segments, different classes that extend Entity
   @throws ServiceFatalException     on failure
   @throws ServiceFatalException     if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   @param segmentIds   to fetch records for.
   @param includePicks whether to include the segment choice arrangement picks
   */
  <N> Collection<N> readManySubEntities(Collection<UUID> segmentIds, Boolean includePicks) throws ServicePrivilegeException, ServiceFatalException;

  /**
   Create all sub-entities for a given segment
   does not actually check if all; these entities belong to the same sub-entity,
   this is a top-level access method only@param entities to of


   */
  <N> void createAllSubEntities(Collection<N> entities) throws ServicePrivilegeException, ServiceFatalException;

  /**
   Read all Segments that are accessible, by Chain ID, starting and ending at particular offsets

   @return array of segments as JSON
   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   @param chainId    to read all segments of
   @param fromOffset to read segments form
   @param toOffset   to read segments to
   */
  Collection<Segment> readManyFromToOffset(UUID chainId, Long fromOffset, Long toOffset) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException;

  /**
   Read all Segments that are accessible, by Chain ID, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#278] Chain Player lives in navbar, and handles all playback (audio waveform, segment waveform, continuous chain) so the user always has central control over listening.

   @param access         control
   @param chainId        to read all segments of
   @param fromSecondsUTC to read segments of
   @return array of segments as JSON
   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, UUID chainId, Long fromSecondsUTC) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException;

  /**
   Read all Segments that are accessible, by Chain Ship key, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#150279540] Unauthenticated public Client wants to access a Chain by ship key (as alias for chain id) in order to provide data for playback.

   @param access         control
   @param chainShipKey  to read all segments of
   @param fromSecondsUTC to read segments of
   @return array of segments as JSON
   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromSecondsUTCbyShipKey(HubClientAccess access, String chainShipKey, Long fromSecondsUTC) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException;

  /**
   Reverts a segment in Planned state, by destroying all its child entities. Only the segment messages remain, for purposes of debugging.
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for fabrication, in the event that such a Segment has just failed its fabrication process, in order to ensure Chain fabrication fault tolerance

   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   w
   @param access control
   @param id     of segment to revert
   */
  void revert(HubClientAccess access, UUID id) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException, ServiceValidationException;

  /**
   Read the last segment in a Chain, Segments sorted by offset ascending

   @return Last Segment in Chain
   @param chainId of chain
   */
  Optional<Segment> readLastSegment(UUID chainId) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException;

  /**
   Read the last dubbed-state segment in a Chain, Segments sorted by offset ascending

   @param access  control
   @param chainId of chain
   @return Last Dubbed-state Segment in Chain
   */
  Optional<Segment> readLastDubbedSegment(HubClientAccess access, UUID chainId) throws ServicePrivilegeException, ServiceFatalException, ServiceExistenceException;
}
