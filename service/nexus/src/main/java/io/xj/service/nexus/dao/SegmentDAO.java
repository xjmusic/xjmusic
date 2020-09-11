// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentMessage;
import io.xj.service.nexus.entity.SegmentState;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface SegmentDAO extends DAO<Segment> {

  /**
   Create a Message in a given Segment

   @param access control
   @param entity Segment Message, including belong-to Segment ID
   @return newly created Segment message
   @throws NexusException   on failure in Nexus service
   @throws JsonApiException on failure in REST API serdes
   @throws ValueException   on invalid values in provided entity
   */
  SegmentMessage create(HubClientAccess access, SegmentMessage entity) throws DAOValidationException, DAOPrivilegeException, DAOExistenceException, DAOFatalException;

  /**
   Fetch id for the Segment in a Chain at a given offset, if present

   @param access  control
   @param chainId to fetch segment for
   @param offset  to fetch segment at
   @return segment id
   */
  Segment readOneAtChainOffset(HubClientAccess access, UUID chainId, Long offset) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException;

  /**
   Fetch one Segment by chainId and state, if present

   @param access             control
   @param chainId            to find segment in
   @param segmentState       segmentState to find segment in
   @param segmentBeginBefore ahead to look for segments
   @return Segment if found
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Segment readOneInState(HubClientAccess access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException;

  /**
   Fetch many records for Chain by embed key, if accessible

   @param access        control
   @param chainEmbedKey to fetch records for.
   @return collection of retrieved records
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Segment> readMany(HubClientAccess access, String chainEmbedKey) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Fetch all sub-entities records for many parent segments by id

   @param access       control
   @param segmentIds   to fetch records for.
   @param includePicks whether to include the segment choice arrangement picks
   @return collection of all sub entities of these parent segments, different classes that extend Entity
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  <N extends Entity> Collection<N> readManySubEntities(HubClientAccess access, Collection<UUID> segmentIds, Boolean includePicks) throws DAOPrivilegeException, DAOFatalException;

  /**
   Create all sub-entities for a given segment
   does not actually check if all; these entities belong to the same sub-entity,
   this is a top-level access method only

   @param access   control
   @param entities to of
   */
  <N extends Entity> void createAllSubEntities(HubClientAccess access, Collection<N> entities) throws DAOPrivilegeException, DAOFatalException;

  /**
   Read all Segments that are accessible, by Chain Id, starting at a particular offset
   limit max # of segments readable at once in environment configuration

   @param access     control
   @param chainId    to read all segments of
   @param fromOffset to read segments form
   @return array of segments as JSON
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromOffset(HubClientAccess access, UUID chainId, Long fromOffset) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Read all Segments that are accessible, by Chain Id, starting and ending at particular offsets

   @param access     control
   @param chainId    to read all segments of
   @param fromOffset to read segments form
   @param toOffset   to read segments to
   @return array of segments as JSON
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromToOffset(HubClientAccess access, UUID chainId, Long fromOffset, Long toOffset) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Read all Segments in a specified state

   @param access  control
   @param chainId to read segments in
   @param state   of segments to read
   @return segments
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyInState(HubClientAccess access, UUID chainId, SegmentState state) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Read all Segments that are accessible, by Chain Embed Key, starting at a particular offset
   limit max # of segments readable at once in environment configuration
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param access        control
   @param chainEmbedKey to read all segments of
   @param fromOffset    to read segments form
   @return array of segments as JSON
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromOffset(HubClientAccess access, String chainEmbedKey, Long fromOffset) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Read all Segments that are accessible, by Chain Id, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#278] Chain Player lives in navbar, and handles all playback (audio waveform, segment waveform, continuous chain) so the user always has central control over listening.

   @param access         control
   @param chainId        to read all segments of
   @param fromSecondsUTC to read segments of
   @return array of segments as JSON
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, UUID chainId, Long fromSecondsUTC) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Read all Segments that are accessible, by Chain Embed Key, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param access         control
   @param chainEmbedKey  to read all segments of
   @param fromSecondsUTC to read segments of
   @return array of segments as JSON
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Segment> readManyFromSecondsUTC(HubClientAccess access, String chainEmbedKey, Long fromSecondsUTC) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Reverts a segment in Planned state, by destroying all its child entities. Only the segment messages remain, for purposes of debugging.
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for fabrication, in the event that such a Segment has just failed its fabrication process, in order to ensure Chain fabrication fault tolerance

   @param access control
   @param id     of segment to revert
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   w
   */
  void revert(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException;

  /**
   Read the last segment in a Chain, Segments sorted by offset ascending

   @param access  control
   @param chainId of chain
   @return Last Segment in Chain
   */
  Segment readLastSegment(HubClientAccess access, UUID chainId) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;

  /**
   Read the last dubbed-state segment in a Chain, Segments sorted by offset ascending

   @param access  control
   @param chainId of chain
   @return Last Dubbed-state Segment in Chain
   */
  Segment readLastDubbedSegment(HubClientAccess access, UUID chainId) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException;
}
