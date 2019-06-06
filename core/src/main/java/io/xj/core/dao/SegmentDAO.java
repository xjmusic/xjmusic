// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

public interface SegmentDAO extends DAO<Segment> {

  /**
   Fetch id for the Segment in a Chain at a given offset, if present

   @param access  control
   @param chainId to fetch segment for
   @param offset  to fetch segment at
   @return segment id
   */
  Segment readOneAtChainOffset(Access access, BigInteger chainId, BigInteger offset) throws CoreException;

  /**
   Fetch one Segment by chainId and state, if present

   @param access             control
   @param chainId            to find segment in
   @param segmentState       segmentState to find segment in
   @param segmentBeginBefore ahead to look for segments
   @return Segment if found
   @throws CoreException on failure
   */
  Segment readOneInState(Access access, BigInteger chainId, SegmentState segmentState, Instant segmentBeginBefore) throws CoreException;

  /**
   Fetch many records for many parents by id, if accessible

   @param chainIdentifier to fetch records for.
   @return collection of retrieved records
   @throws CoreException on failure
   */
  Collection<Segment> readAll(String chainIdentifier) throws CoreException;

  /**
   Read all Segments that are accessible, by Chain Id, starting at a particular offset
   limit max # of segments readable at once in environment configuration

   @param access     control
   @param chainId    to read all segments from
   @param fromOffset to read segments form
   @return array of segments as JSON
   @throws CoreException on failure
   */
  Collection<Segment> readAllFromOffset(Access access, BigInteger chainId, BigInteger fromOffset) throws CoreException;

  /**
   Read all Segments that are accessible, by Chain Id, starting and ending at particular offsets

   @param access     control
   @param chainId    to read all segments from
   @param fromOffset to read segments form
   @return array of segments as JSON
   @throws CoreException on failure
   */
  Collection<Segment> readAllFromToOffset(Access access, BigInteger chainId, BigInteger fromOffset, BigInteger toOffset) throws CoreException;

  /**
   Read all Segments in a specified state

   @param access  control
   @param chainId to read segments in
   @param state   of segments to read
   @return segments
   */
  Collection<Segment> readAllInState(Access access, BigInteger chainId, SegmentState state) throws CoreException;

  /**
   Read all Segments that are accessible, by Chain Embed Key, starting at a particular offset
   limit max # of segments readable at once in environment configuration
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param chainEmbedKey to read all segments from
   @param fromOffset    to read segments form
   @return array of segments as JSON
   @throws CoreException on failure
   */
  Collection<Segment> readAllFromOffset(String chainEmbedKey, BigInteger fromOffset) throws CoreException;

  /**
   Read all Segments that are accessible, by Chain Id, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#278] Chain Player lives in navbar, and handles all playback (audio waveform, segment waveform, continuous chain) so the user always has central control over listening.

   @return array of segments as JSON
   @throws CoreException on failure
   @param access         control
   @param chainId        to read all segments from
   @param fromSecondsUTC to read segments from
   */
  Collection<Segment> readAllFromSecondsUTC(Access access, BigInteger chainId, Long fromSecondsUTC) throws CoreException;

  /**
   Read all Segments that are accessible, by Chain Embed Key, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @return array of segments as JSON
   @throws CoreException on failure
   @param chainEmbedKey  to read all segments from
   @param fromSecondsUTC to read segments from
   */
  Collection<Segment> readAllFromSecondsUTC(String chainEmbedKey, Long fromSecondsUTC) throws CoreException;

  /**
   Update the state of a specified Segment

   @param id    of specific Segment to update.
   @param state for the updated Segment.
   */
  void updateState(Access access, BigInteger id, SegmentState state) throws CoreException;

  /**
   Reverts a segment in Planned state, by destroying all its child entities. Only the segment messages remain, for purposes of debugging.
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for fabrication, in the event that such a Segment has just failed its fabrication process, in order to ensure Chain fabrication fault tolerance

   @param access control
   @param id     of segment to revert
   @throws CoreException on failure
   */
  void revert(Access access, BigInteger id) throws CoreException;

}
