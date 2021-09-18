// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.service;

import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.hub.enums.TemplateType;
import io.xj.nexus.service.exception.ServiceExistenceException;
import io.xj.nexus.service.exception.ServiceFatalException;
import io.xj.nexus.service.exception.ServicePrivilegeException;
import io.xj.nexus.service.exception.ServiceValidationException;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ChainService extends Service<Chain> {

  /**
   [INTERNAL USE ONLY]
   Read all records in a given state

   @param state to read chains in
   @return array of chains as JSON
   @throws ServiceFatalException     on failure
   @throws ServicePrivilegeException if access is prohibited
   */
  Collection<Chain> readManyInState(ChainState state) throws ServiceFatalException, ServicePrivilegeException;

  /**
   [#176285826] Nexus bootstraps Chains from JSON file on startup

   @param type  of template
   @param chain to bootstrap
   @return newly bootstrapped Chain
   @throws ServiceFatalException      on failure
   @throws ServicePrivilegeException  if access is prohibited
   @throws ServiceValidationException on invalid data
   */
  Chain bootstrap(
    TemplateType type,
    Chain chain
  ) throws ServiceFatalException, ServicePrivilegeException, ServiceValidationException, ServiceExistenceException;

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by ship key (as alias for chain id) in order to provide data for playback.

   @param shipKey of record to fetch
   @return retrieved record
   @throws ServicePrivilegeException if access is prohibited
   */
  Chain readOneByShipKey(String shipKey) throws ServicePrivilegeException, ServiceExistenceException, ServiceFatalException;

  /**
   Update the state of a specified Chain

   @param id    of specific Chain to update.
   @param state for the updated Chain.
   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   */
  void updateState(UUID id, ChainState state) throws ServiceFatalException, ServiceExistenceException, ServicePrivilegeException, ServiceValidationException;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next segment in this Chain,
   or set the Chain state to COMPLETE if we are past the end time

   @param chain                  to build segment for
   @param segmentBeginBefore     build the next Segment if we are before this time
   @param chainStopCompleteAfter complete the Chain if we are after this time
   @return next segment if one needed to be built, or empty if no action needs to be taken
   @throws ServiceFatalException     on failure
   @throws ServiceExistenceException if the entity does not exist
   @throws ServicePrivilegeException if access is prohibited
   */
  Optional<Segment> buildNextSegmentOrCompleteTheChain(Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteAfter) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException, ServiceValidationException;

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   [#170273871] Revived chain should always start now

   @param priorChainId to revived
   @param reason       provided description why we are reviving this chain
   @return newly created revived chain
   */
  Chain revive(UUID priorChainId, String reason) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException, ServiceValidationException;

  /**
   Read all chains

   @return all chains
   */
  Collection<Chain> readAllFabricating() throws ServicePrivilegeException, ServiceFatalException;

  /**
   Destroy a chain if it exists for the given ship key@param access

   @param key for which to lookup chain
   */
  void destroyIfExistsForShipKey(String key);
}
