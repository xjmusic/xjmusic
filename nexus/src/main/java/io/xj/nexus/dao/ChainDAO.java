// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.TemplateType;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;

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
   [#176285826] Nexus bootstraps Chains from JSON file on startup

   @return newly bootstrapped Chain
   @throws DAOFatalException      on failure
   @throws DAOPrivilegeException  if access is prohibited
   @throws DAOValidationException on invalid data
   @param access   control
   @param type
   @param chain    to bootstrap
   */
  Chain bootstrap(
    HubClientAccess access,
    TemplateType type, Chain chain
  ) throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException;

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param access   control
   @param embedKey of record to fetch
   @return retrieved record
   @throws DAOPrivilegeException if access is prohibited
   */
  Chain readOneByEmbedKey(HubClientAccess access, String embedKey) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException;

  /**
   Update the state of a specified Chain

   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   @param access control
   @param id     of specific Chain to update.
   @param state  for the updated Chain.
   */
  void updateState(HubClientAccess access, UUID id, ChainState state) throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next segment in this Chain,
   or set the Chain state to COMPLETE if we are past the end time

   @param access                 control needs to be internal
   @param chain                  to build segment for
   @param segmentBeginBefore     build the next Segment if we are before this time
   @param chainStopCompleteAfter complete the Chain if we are after this time
   @return next segment if one needed to be built, or empty if no action needs to be taken
   @throws DAOFatalException     on failure
   @throws DAOExistenceException if the entity does not exist
   @throws DAOPrivilegeException if access is prohibited
   */
  Optional<Segment> buildNextSegmentOrCompleteTheChain(HubClientAccess access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteAfter) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException;

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

   @return newly created revived chain
   @param access       control
   @param priorChainId to revived
   @param reason       provided description why we are reviving this chain
   */
  Chain revive(HubClientAccess access, UUID priorChainId, String reason) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException;

  /**
   Read all chains
   @return all chains
   @param access
   */
  Collection<Chain> readAllFabricating(HubClientAccess access) throws DAOPrivilegeException, DAOFatalException;

  /**
   Whether the chain is playing for the given template
   @return true if playing, else false
   @param key for which to lookup chain
   */
  boolean existsForEmbedKey(String key);
}
