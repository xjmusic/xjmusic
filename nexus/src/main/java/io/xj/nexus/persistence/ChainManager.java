// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.persistence;

import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.TemplateType;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ChainManager extends Manager<Chain> {

  /**
   [INTERNAL USE ONLY]
   Read all records in a given state

   @param state to read chains in
   @return array of chains as JSON
   @throws ManagerFatalException     on failure
   @throws ManagerPrivilegeException if access is prohibited
   */
  Collection<Chain> readManyInState(ChainState state) throws ManagerFatalException, ManagerPrivilegeException;

  /**
   [#176285826] Nexus bootstraps Chains from JSON file on startup

   @param type  of template
   @param chain to bootstrap
   @return newly bootstrapped Chain
   @throws ManagerFatalException      on failure
   @throws ManagerPrivilegeException  if access is prohibited
   @throws ManagerValidationException on invalid data
   */
  Chain bootstrap(
    TemplateType type,
    Chain chain
  ) throws ManagerFatalException, ManagerPrivilegeException, ManagerValidationException, ManagerExistenceException;

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by ship key (as alias for chain id) in order to provide data for playback.

   @param shipKey of record to fetch
   @return retrieved record
   @throws ManagerPrivilegeException if access is prohibited
   */
  Chain readOneByShipKey(String shipKey) throws ManagerPrivilegeException, ManagerExistenceException, ManagerFatalException;

  /**
   Update the state of a specified Chain

   @param id    of specific Chain to update.
   @param state for the updated Chain.
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  void updateState(UUID id, ChainState state) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException;

  /**
   [INTERNAL USE ONLY]
   Build a template for the next segment in this Chain,
   or set the Chain state to COMPLETE if we are past the end time

   @param chain                  to build segment for
   @param segmentBeginBefore     build the next Segment if we are before this time
   @param chainStopCompleteAfter complete the Chain if we are after this time
   @return next segment if one needed to be built, or empty if no action needs to be taken
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException if the entity does not exist
   @throws ManagerPrivilegeException if access is prohibited
   */
  Optional<Segment> buildNextSegmentOrCompleteTheChain(Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteAfter) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException, ManagerValidationException;

  /**
   Read all chains in fabricating state

   @return all chains in fabricating state
   */
  Collection<Chain> readAllFabricating() throws ManagerPrivilegeException, ManagerFatalException;

  /**
   Destroy a chain if it exists for the given ship key@param access

   @param key for which to lookup chain
   */
  void destroyIfExistsForShipKey(String key);

  /**
   Read all chains

   @return all chains
   */
  Collection<Chain> readAll() throws NexusException;

  /**
   Put this chain in the store as-is@param chain to put

   @return chain that was put
   */
  Chain put(Chain chain) throws ManagerFatalException;

  /**
   Whether a chain exists in the store for the given ship key

   @param shipKey for which to test
   @return true if exists in store
   */
  boolean existsForShipKey(String shipKey);

  /**
   Get the template config for a given chain id

   @param chainId for which to get chain id
   @return template config
   @throws ManagerFatalException     on failure
   @throws ManagerExistenceException on failure
   @throws ManagerPrivilegeException on failure
   @throws ValueException            on failure
   */
  TemplateConfig getTemplateConfig(UUID chainId) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ValueException;

}
