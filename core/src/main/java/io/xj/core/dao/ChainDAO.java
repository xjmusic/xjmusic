// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.segment.Segment;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

public interface ChainDAO extends DAO<Chain> {

  /**
   [INTERNAL USE ONLY]
   Read all records in a given state

   @param access control
   @param state  to read chains in
   @return array of chains as JSON
   @throws Exception on failure
   */
  Collection<Chain> readAllInState(Access access, ChainState state) throws Exception;

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param access   control
   @param embedKey of record to fetch
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  Chain readOne(Access access, String embedKey) throws Exception;

  /**
   Update the state of a specified Chain

   @param id    of specific Chain to update.
   @param state for the updated Chain.
   */
  void updateState(Access access, BigInteger id, ChainState state) throws Exception;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next segment in this Chain,
   or set the Chain state to COMPLETE.

   @param chain                   to build segment for
   @param segmentBeginBefore      ahead to create Segment before end of previous Segment  @return array of chain Ids
   @param chainStopCompleteBefore behind to consider a chain complete
   */
  Segment buildNextSegmentOrComplete(Access access, Chain chain, Timestamp segmentBeginBefore, Timestamp chainStopCompleteBefore) throws Exception;

  /**
   Erase a specified Chain (mark it for deletion by worker)

   @param chainId of specific Chain to erase.
   */
  void erase(Access access, BigInteger chainId) throws Exception;

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.

   @param access      control
   @param priorChainId to revived
   @return newly created revived chain
   */
  Chain revive(Access access, BigInteger priorChainId) throws Exception;
}
