// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.segment.Segment;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface ChainDAO extends DAO<Chain> {

  /**
   [INTERNAL USE ONLY]
   Read all records in a given state

   @param access control
   @param state  to read chains in
   @return array of chains as JSON
   @throws CoreException on failure
   */
  Collection<Chain> readAllInState(Access access, ChainState state) throws CoreException;

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param access   control
   @param embedKey of record to fetch
   @return retrieved record
   @throws CoreException on failure
   */
  Chain readOne(Access access, String embedKey) throws CoreException;

  /**
   Update the state of a specified Chain

   @param id    of specific Chain to update.
   @param state for the updated Chain.
   */
  void updateState(Access access, BigInteger id, ChainState state) throws CoreException;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next segment in this Chain,
   or set the Chain state to COMPLETE we are past the end time
   @param chain                   to build segment for
   @param segmentBeginBefore      ahead to create Segment before end of previous Segment  @return array of chain Ids
   @param chainStopCompleteBefore behind to consider a chain complete


   */
  Optional<Segment> buildNextSegmentOrComplete(Access access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteBefore) throws CoreException;

  /**
   Erase a specified Chain (mark it for deletion by worker)

   @param chainId of specific Chain to erase.
   */
  void erase(Access access, BigInteger chainId) throws CoreException;

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.

   @param access      control
   @param priorChainId to revived
   @return newly created revived chain
   */
  Chain revive(Access access, BigInteger priorChainId) throws CoreException;

  /**

   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.
   @return collection of chains (if any) which were revived from stale chains.
   @param access  control
   */
  Collection<Chain> checkAndReviveAll(Access access) throws CoreException;
}
