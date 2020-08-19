// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.MessageType;
import io.xj.lib.pubsub.PubSubProvider;
import io.xj.lib.util.CSV;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainBinding;
import io.xj.service.nexus.entity.ChainConfig;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentState;
import io.xj.service.nexus.entity.SegmentType;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.lib.entity.EntityStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Chain D.A.O. Implementation
 <p>
 Core directive here is to keep business logic CENTRAL ("oneness")
 <p>
 All variants on an update resolve (afterafter recordUpdateFirstStep transformation,
 or additional validation) to one singular central update(fieldValues)
 <p>
 Also note buildNextSegmentOrComplete(...) is one singular central implementation
 of the logic around adding segments to chains and updating chain state to complete.
 <p>
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class ChainDAOImpl extends DAOImpl<Chain> implements ChainDAO {
  private static final Logger log = LoggerFactory.getLogger(ChainDAOImpl.class);
  private static final Set<ChainState> NOTIFY_ON_CHAIN_STATES = ImmutableSet.of(
    ChainState.Fabricate,
    ChainState.Failed
  );
  private final int chainReviveThresholdStartSeconds;
  private final ChainConfigDAO chainConfigDAO;
  private final ChainBindingDAO chainBindingDAO;
  private final int chainReviveThresholdHeadSeconds;
  private final SegmentDAO segmentDAO;
  private final int previewLengthMaxSeconds;
  private final PubSubProvider pubsub;

  @Inject
  public ChainDAOImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    Config config,
    SegmentDAO segmentDAO,
    ChainConfigDAO chainConfigDAO,
    ChainBindingDAO chainBindingDAO,
    PubSubProvider pubSubProvider
  ) {
    super(entityFactory, nexusEntityStore);
    this.segmentDAO = segmentDAO;
    this.pubsub = pubSubProvider;

    previewLengthMaxSeconds = config.getInt("chain.previewLengthMaxSeconds");
    chainReviveThresholdHeadSeconds = config.getInt("chain.reviveThresholdHeadSeconds");
    chainReviveThresholdStartSeconds = config.getInt("chain.reviveThresholdStartSeconds");
    this.chainConfigDAO = chainConfigDAO;
    this.chainBindingDAO = chainBindingDAO;
  }

  @Override
  public Chain create(HubClientAccess access, Chain chain) throws DAOFatalException, DAOPrivilegeException, DAOValidationException {
    try {
      // [#126] Chains are always created in DRAFT state
      chain.setId(UUID.randomUUID());
      chain.validate();
      chain.setStateEnum(ChainState.Draft);

      // logic based on Chain Type
      switch (chain.getType()) {

        case Production:
          requireAccount(access, chain.getAccountId(), UserRoleType.Engineer);
          requireUniqueEmbedKey(access, chain);
          break;

        case Preview:
          requireAccount(access, chain.getAccountId(), UserRoleType.Artist);
          chain.setStartAtInstant(Instant.now());
          chain.setStopAtInstant(instantLimitUpper(chain.getStopAt(), chain.getStartAt().plusSeconds(previewLengthMaxSeconds)));
          chain.setEmbedKey(null); // [#402] Preview Chain cannot be public
          break;
      }

      // store and return sanitized payload comprising only the valid Chain
      return store.put(chain);

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Chain readOne(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      Chain chain = store.get(Chain.class, id)
        .orElseThrow(() -> new DAOExistenceException(Chain.class, id));
      requireAccount(access, chain);
      return chain;

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Chain readOne(HubClientAccess access, String rawEmbedKey) throws DAOExistenceException, DAOFatalException {
    try {
      String key = Chain.toEmbedKey(rawEmbedKey);
      return store.getAll(Chain.class).stream()
        .filter(c -> Objects.equals(key, c.getEmbedKey()))
        .findFirst()
        .orElseThrow(() -> new DAOExistenceException(Chain.class, rawEmbedKey));

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readMany(HubClientAccess access, Collection<UUID> accountIds) throws DAOFatalException, DAOPrivilegeException {
    try {
      for (UUID accountId : accountIds) requireAccount(access, accountId);
      return store.getAll(Chain.class, Account.class, accountIds);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readManyInState(HubClientAccess access, ChainState state) throws DAOFatalException, DAOPrivilegeException {
    try {
      Collection<Chain> chains = store.getAll(Chain.class).stream()
        .filter(chain -> state.equals(chain.getState()))
        .collect(Collectors.toList());
      for (Chain chain : chains)
        requireAccount(access, chain);
      return chains;

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void update(HubClientAccess access, UUID id, Chain chain)
    throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException {
    try {
      // cache existing chain from-state
      Chain existing = readOne(access, id);
      ChainState fromState = existing.getState();

      // cannot change type of chain
      if (existing.getType() != chain.getType())
        throw new DAOValidationException("Cannot modify Chain Type");

      // override id (cannot be changed) from existing chain, and then validate
      chain.setId(id);
      chain.validate();

      // If we have an embed key, it must not belong to another chain
      requireUniqueEmbedKey(access, chain);

      // Final before-update validation, then store
      beforeUpdate(access, chain, fromState);

      // [#116] block update Chain state: cannot change chain startAt time after has segments
      if (Objects.nonNull(chain.getStartAt()))
        if (!existing.getStartAt().equals(chain.getStartAt()))
          if (!segmentDAO.readMany(access, ImmutableList.of(chain.getId())).isEmpty())
            throw new DAOValidationException("cannot change chain startAt time after it has segments");

      // Commit changes
      store.put(chain);

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void updateState(HubClientAccess access, UUID id, ChainState state)
    throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException {
    try {
      Chain chain = readOne(access, id);
      ChainState fromState = chain.getState();

      // update to chain state only
      chain.setStateEnum(state);

      // all standard before-update tests
      beforeUpdate(access, chain, fromState);

      // commit changes and publish notification
      store.put(chain);
      if (NOTIFY_ON_CHAIN_STATES.contains(chain.getState())) {
        log.info("Updated Chain {} to state {}", chain.getId(), chain.getState());
        pubsub.publish(String.format("Updated Chain %s to state %s", chain.getId(), chain.getState()), MessageType.Info.toString());
      }

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Optional<Segment> buildNextSegmentOrCompleteTheChain(HubClientAccess access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteBefore) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException {
    requireTopLevel(access);

    // If there's already a no-endAt-time-having Segment at the end of this Chain, get outta here
    if (segmentDAO.readMany(access, ImmutableSet.of(chain.getId()))
      .stream()
      .filter(s -> Objects.isNull(s.getEndAt()))
      .max(Comparator.comparing(Segment::getOffset))
      .isPresent())
      return Optional.empty();

    // Get the last segment in the chain
    // If the chain had no last segment, it must be empty; return a template for its first segment
    Segment lastSegmentInChain;
    try {
      lastSegmentInChain = segmentDAO.readLastSegment(access, chain.getId());
    } catch (DAOExistenceException ignored2) {
      Segment pilotTemplate = new Segment();
      pilotTemplate.setChainId(chain.getId());
      pilotTemplate.setBeginAtInstant(chain.getStartAt());
      pilotTemplate.setOffset(0L);
      pilotTemplate.setTypeEnum(SegmentType.Pending);
      pilotTemplate.setState(SegmentState.Planned.toString());
      return Optional.of(pilotTemplate);
    }

    // If the last segment begins after our boundary, we're here early; get outta here.
    if (lastSegmentInChain.getBeginAt().isAfter(segmentBeginBefore))
      return Optional.empty();

    // [#204] Craft process updates Chain to COMPLETE state when the final segment is in dubbed state.
    if (Objects.nonNull(lastSegmentInChain.getEndAt())
      && Objects.nonNull(chain.getStopAt())
      && lastSegmentInChain.getEndAt().isAfter(chain.getStopAt())) {
      // this is where we check to see if the chain is ready to be COMPLETE.
      if (chain.getStopAt().isBefore(chainStopCompleteBefore)
        // and [#122] require the last segment in the chain to be in state DUBBED.
        && SegmentState.Dubbed.equals(lastSegmentInChain.getState())) {
        updateState(access, chain.getId(), ChainState.Complete);
      }
      return Optional.empty();
    }

    // Build the template of the segment that follows the last known one
    Segment pilotTemplate = new Segment();
    Long pilotOffset = lastSegmentInChain.getOffset() + 1;
    pilotTemplate.setChainId(chain.getId());
    pilotTemplate.setBeginAtInstant(lastSegmentInChain.getEndAt());
    pilotTemplate.setOffset(pilotOffset);
    pilotTemplate.setTypeEnum(SegmentType.Pending);
    pilotTemplate.setState(SegmentState.Planned.toString());
    return Optional.of(pilotTemplate);
  }

  @Override
  public void destroy(HubClientAccess access, UUID id) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    try {
      Chain chain = store.get(Chain.class, id)
        .orElseThrow(() -> new DAOExistenceException(Chain.class, id));
      requireAccount(access, chain);

      for (ChainConfig chainConfig : chainConfigDAO.readMany(access, ImmutableList.of()))
        chainConfigDAO.destroy(access, chainConfig.getId());

      for (ChainBinding chainBinding : chainBindingDAO.readMany(access, ImmutableList.of()))
        chainBindingDAO.destroy(access, chainBinding.getId());

      for (Segment segment : segmentDAO.readMany(access, ImmutableList.of()))
        segmentDAO.destroy(access, segment.getId());

      store.delete(Chain.class, id);

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void requireAccount(HubClientAccess access, Chain chain) throws DAOPrivilegeException {
    requireAccount(access, chain.getAccountId());
  }

  @Override
  public Chain revive(HubClientAccess access, UUID priorChainId) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException {
    Chain priorChain = readOne(access, priorChainId);

    if (ChainState.Fabricate != priorChain.getState())
      throw new DAOPrivilegeException("Only a Fabricate-state Chain can be revived.");

    if (ChainType.Production != priorChain.getType())
      throw new DAOPrivilegeException("Only a Production-type Chain can be revived.");

    // save the embed key to re-use on new chain
    String embedKey = priorChain.getEmbedKey();

    // update the prior chain to failed state and null embed key
    priorChain.setStateEnum(ChainState.Failed);
    priorChain.setEmbedKey(null);
    update(access, priorChainId, priorChain);

    // of new chain with original properties (implicitly created in draft state)
    priorChain.setId(UUID.randomUUID()); // new id
    priorChain.setEmbedKey(embedKey);
    priorChain.setStartAtInstant(Instant.now()); // [#170273871] Revived chain should always start now
    Chain created = create(access, priorChain);

    // Re-create all chain configs of original chain
    for (ChainConfig chainConfig : chainConfigDAO.readMany(access, ImmutableList.of(priorChainId)))
      chainConfigDAO.create(access, chainConfig.setChainId(created.getId()));

    // Re-create all chain bindings of original chain
    for (ChainBinding chainBinding : chainBindingDAO.readMany(access, ImmutableList.of(priorChainId)))
      chainBindingDAO.create(access, chainBinding.setChainId(created.getId()));

    // update new chain into ready, then fabricate, which begins the new work
    updateState(access, created.getId(), ChainState.Ready);
    updateState(access, created.getId(), ChainState.Fabricate);
    created.setStateEnum(ChainState.Fabricate);

    // publish a notification reporting the event
    log.info("Revived Chain created {} from prior {}}", created.getId(), priorChain.getId());
    pubsub.publish(String.format("Revived Chain created %s create from prior %s", created.getId(), priorChain.getId()), MessageType.Info.toString());

    // return newly created chain
    return created;
  }

  @Override
  public Collection<Chain> checkAndReviveAll(HubClientAccess access) throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    requireTopLevel(access);

    Collection<Chain> outcome = Lists.newArrayList();
    Instant thresholdChainStartAt = Instant.now().minusSeconds(chainReviveThresholdStartSeconds);
    Instant thresholdChainHeadAt = Instant.now().minusSeconds(chainReviveThresholdHeadSeconds);

    Collection<UUID> stalledChainIds = Lists.newArrayList();
    readManyInState(access, ChainState.Fabricate)
      .stream()
      .filter((chain) -> chain.isProductionStartedBefore(thresholdChainStartAt))
      .forEach(chain -> {
        try {
          if (store.getAll(Segment.class, Chain.class, ImmutableSet.of(chain.getId())).stream()
            .noneMatch(segment -> segment.isDubbedEndingAfter(thresholdChainHeadAt))) {
            log.warn("Found stalled Chain {} with no Segments Dubbed ending after {}", chain.getId(), thresholdChainHeadAt);
            stalledChainIds.add(chain.getId());
          }
        } catch (EntityStoreException e) {
          log.warn("Failure while checking for Chains to revive!", e);
        }
      });

    // revive all stalled chains
    for (UUID stalledChainId : stalledChainIds) {
      outcome.add(revive(access, stalledChainId));
      // [#173968355] Nexus deletes entire chain when no current segments are left.
      destroy(access, stalledChainId);
    }

    return outcome;
  }

  @Override
  public Chain newInstance() {
    try {
      return entityFactory.getInstance(Chain.class);
    } catch (EntityException ignored) {
      return new Chain();
    }
  }

  /**
   Require that the provided chain is the only one existing with this embed key

   @param access control
   @param chain  to test embed key uniqueness of
   @throws DAOValidationException if another Chain exists with this embed key
   @throws DAOFatalException      on failure to determine
   */
  private void requireUniqueEmbedKey(HubClientAccess access, Chain chain) throws DAOValidationException, DAOFatalException {
    if (Objects.nonNull(chain.getEmbedKey()))
      try {
        Chain existing = readOne(access, chain.getEmbedKey());
        if (!Objects.equals(chain.getId(), existing.getId()))
          throw new DAOValidationException(String.format("Chain already exists with embed key '%s'!", chain.getEmbedKey()));
      } catch (DAOExistenceException ignored) {
        // OK if no other chain exists with this embed key
      }
  }

  /**
   Validate access and make other modifications to a chain before update

   @param access    control to test with
   @param chain     payload  to test and modify before update
   @param fromState to test for transition from
   @throws DAOFatalException     on impossible transition
   @throws DAOPrivilegeException on insufficient privileges
   */
  private void beforeUpdate(HubClientAccess access, Chain chain, ChainState fromState) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    // Conditions based on Chain type
    switch (chain.getType()) {
      case Production:
        requireAccount(access, chain.getAccountId(), UserRoleType.Engineer);
        break;

      case Preview:
        requireAccount(access, chain.getAccountId(), UserRoleType.Engineer, UserRoleType.Artist);
        chain.setStopAtInstant(instantLimitUpper(chain.getStopAt(), chain.getStartAt().plusSeconds(previewLengthMaxSeconds)));

        // [#402] Preview Chain cannot be public
        chain.setEmbedKey(null);
        break;
    }

    // Conditions based on Chain state
    switch (fromState) {
      case Draft:
        onlyAllowTransitions(chain.getState(), ChainState.Draft, ChainState.Ready);
        // block update of Chain away from draft unless Chain is bound to at least one Library, Sequence, or Instrument
        if (ChainState.Ready.equals(chain.getState()))
          if (chainBindingDAO.readMany(access, ImmutableList.of(chain.getId())).isEmpty())
            throw new DAOValidationException("Chain must be bound to at least one Library, Sequence, or Instrument");
        break;

      case Ready:
        onlyAllowTransitions(chain.getState(), ChainState.Draft, ChainState.Ready, ChainState.Fabricate);
        break;

      case Fabricate:
        onlyAllowTransitions(chain.getState(), ChainState.Fabricate, ChainState.Failed, ChainState.Complete);
        break;

      case Complete:
        onlyAllowTransitions(chain.getState(), ChainState.Complete);
        break;

      case Failed:
        onlyAllowTransitions(chain.getState(), ChainState.Failed);
        break;
    }


    // If state is changing, there may be field updates based on the new state
    if (fromState != chain.getState()) switch (chain.getState()) {
      case Draft:
      case Ready:
      case Fabricate:
      case Complete:
      case Failed:
        // no op
        break;
    }
  }

  /**
   Limit a moment in time by an upper limit of another moment in time

   @param source     instant
   @param upperLimit instant to limit upper threshold
   @return source limited to upper limit
   */
  private Instant instantLimitUpper(Instant source, Instant upperLimit) {
    return source.isBefore(upperLimit) ? source : upperLimit;
  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   */
  private void onlyAllowTransitions(ChainState toState, ChainState... allowedStates) throws DAOPrivilegeException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (ChainState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (search == toState) {
        return;
      }
    }
    throw new DAOPrivilegeException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }
}
