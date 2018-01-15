// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.link.Link;

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
   Update the state of a specified Chain

   @param id    of specific Chain to update.
   @param state for the updated Chain.
   */
  void updateState(Access access, BigInteger id, ChainState state) throws Exception;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next link in this Chain,
   or set the Chain state to COMPLETE.

   @param chain                   to build link for
   @param linkBeginBefore         ahead to create Link before end of previous Link  @return array of chain Ids
   @param chainStopCompleteBefore behind to consider a chain complete
   */
  Link buildNextLinkOrComplete(Access access, Chain chain, Timestamp linkBeginBefore, Timestamp chainStopCompleteBefore) throws Exception;

  /**
   Erase a specified Chain (mark it for deletion by worker)

   @param chainId of specific Chain to erase.
   */
  void erase(Access access, BigInteger chainId) throws Exception;
}
