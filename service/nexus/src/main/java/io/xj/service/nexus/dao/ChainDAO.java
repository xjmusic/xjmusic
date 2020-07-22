// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.Segment;

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
   @throws DAOFatalException     on failure
   @throws DAOPrivilegeException if access is prohibited
   */
  Collection<Chain> readManyInState(HubClientAccess access, ChainState state) throws DAOFatalException, DAOPrivilegeException;

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param access   control
   @param embedKey of record to fetch
   @return retrieved record
   @throws DAOPrivilegeException if access is prohibited
   */
  Chain readOne(HubClientAccess access, String embedKey) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException;

  /**
   Update the state of a specified Chain

   @param access control
   @param id     of specific Chain to update.
   @param state  for the updated Chain.
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  void updateState(HubClientAccess access, UUID id, ChainState state) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next segment in this Chain,
   or set the Chain state to COMPLETE if we are past the end time

   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   @param access                  control needs to be internal
   @param chain                   to build segment for
   @param segmentBeginBefore      ahead to of Segment before end of previous Segment
   @param chainStopCompleteBefore behind to consider a chain complete
   @return next segment if one needed to be built, or empty if no action needs to be taken
   */
  Optional<Segment> buildNextSegmentOrCompleteTheChain(HubClientAccess access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteBefore) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException;

  /**
   Require Account and role from given access

   @param access given to check
   @param chain  to check for account and role permissions
   @throws DAOPrivilegeException if access is prohibited
   */
  void requireAccount(HubClientAccess access, Chain chain) throws DAOPrivilegeException;

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   [#170273871] Revived chain should always start now

   @param access       control
   @param priorChainId to revived
   @return newly created revived chain
   */
  Chain revive(HubClientAccess access, UUID priorChainId) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException;

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.

   @param access control
   @return collection of chains (if any) which were revived of stale chains.
   */
  Collection<Chain> checkAndReviveAll(HubClientAccess access) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException;
}
