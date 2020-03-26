// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.Segment;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ChainDAO extends DAO<Chain> {

  /**
   [INTERNAL USE ONLY]
   Read all records in a given state

   @param access control
   @param state  to read chains in
   @return array of chains as JSON
   @throws HubException on failure
   */
  Collection<Chain> readAllInState(Access access, ChainState state) throws HubException;

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param access   control
   @param embedKey of record to fetch
   @return retrieved record
   @throws HubException on failure
   */
  Chain readOne(Access access, String embedKey) throws HubException;

  /**
   Update the state of a specified Chain

   @param id    of specific Chain to update.
   @param state for the updated Chain.
   */
  void updateState(Access access, UUID id, ChainState state) throws HubException, RestApiException, ValueException;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next segment in this Chain,
   or set the Chain state to COMPLETE we are past the end time

   @param chain                   to build segment for
   @param segmentBeginBefore      ahead to of Segment before end of previous Segment  @return array of chain Ids
   @param chainStopCompleteBefore behind to consider a chain complete
   */
  Optional<Segment> buildNextSegmentOrComplete(Access access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteBefore) throws HubException, RestApiException, ValueException;

  /**
   Erase a specified Chain (mark it for deletion by nexus)

   @param chainId of specific Chain to erase.
   */
  void erase(Access access, UUID chainId) throws HubException, RestApiException, ValueException;

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   [#170273871] Revived chain should always start now

   @param access       control
   @param priorChainId to revived
   @return newly createdrevived chain
   */
  Chain revive(Access access, UUID priorChainId) throws HubException, RestApiException, ValueException;

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.

   @param access control
   @return collection of chains (if any) which were revived of stale chains.
   */
  Collection<Chain> checkAndReviveAll(Access access) throws HubException, RestApiException, ValueException;
}
