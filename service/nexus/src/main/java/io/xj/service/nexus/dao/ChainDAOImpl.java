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
import io.xj.lib.entity.EntityStoreException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

/**
 Chain D.A.O. Implementation
 <p>
 Core directive here is to keep business logic CENTRAL ("oneness")
 <p>
 All variants on an update resolve (after recordUpdateFirstStep transformation,
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
  private static final Set<ChainState> REVIVE_FROM_STATES_ALLOWED = ImmutableSet.of(
    ChainState.Fabricate,
    ChainState.Complete,
    ChainState.Failed
  );
  private final ChainBindingDAO chainBindingDAO;
  private final Config config;
  private final SegmentDAO segmentDAO;
  private final int previewLengthMaxHours;
  private final PubSubProvider pubSub;
  private final int previewEmbedKeyLength;
  private final SecureRandom secureRandom = new SecureRandom();

  @Inject
  public ChainDAOImpl(
    Config config,
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    SegmentDAO segmentDAO,
    ChainBindingDAO chainBindingDAO,
    PubSubProvider pubSubProvider
  ) {
    super(entityFactory, nexusEntityStore);
    this.config = config;
    this.segmentDAO = segmentDAO;
    this.pubSub = pubSubProvider;

    previewLengthMaxHours = config.getInt("fabrication.previewLengthMaxHours");
    previewEmbedKeyLength = config.getInt("fabrication.previewEmbedKeyLength");
    this.chainBindingDAO = chainBindingDAO;
  }

  @Override
  public Chain create(HubClientAccess access, Chain chain) throws DAOFatalException, DAOPrivilegeException, DAOValidationException {
    try {
      // [#126] Chains are always created in DRAFT state
      chain.setStateEnum(ChainState.Draft);

      // logic based on Chain Type
      switch (chain.getType()) {

        case Production:
          requireAccount(access, chain.getAccountId(), UserRoleType.Engineer);
          requireUniqueEmbedKey(access, chain);
          break;

        case Preview:
          requireAccount(access, chain.getAccountId(), UserRoleType.Artist);
          chain.setEmbedKey(generatePreviewEmbedKey());
          break;
      }

      // Give model a fresh unique ID and Validate
      chain.setId(UUID.randomUUID());
      chain.validate();

      // [#175347578] validate TypeSafe chain config
      new ChainConfig(chain, config);

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

      // [#174153691] Cannot change stop-at time or Embed Key of Preview chain
      if (ChainType.Preview == existing.getType())
        chain
          .setStopAtInstant(existing.getStopAt())
          .setEmbedKey(existing.getEmbedKey());

      // override id (cannot be changed) from existing chain, and then validate
      chain.setId(id);
      chain.validate();

      // [#175347578] validate TypeSafe chain config
      new ChainConfig(chain, config);

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
        pubSub.publish(String.format("Updated Chain %s to state %s", chain.getId(), chain.getState()), MessageType.Info.toString());
      }

    } catch (EntityStoreException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Optional<Segment> buildNextSegmentOrCompleteTheChain(HubClientAccess access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteAfter) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException {
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
      if (chain.getStopAt().isBefore(chainStopCompleteAfter)
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
    switch (chain.getType()) {
      case Production:
        requireAccount(access, chain.getAccountId(), UserRoleType.Engineer);
        break;

      case Preview:
        requireAccount(access, chain.getAccountId(), UserRoleType.Engineer, UserRoleType.Artist);
        break;
    }
  }

  @Override
  public Chain revive(HubClientAccess access, UUID priorChainId, String reason) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException {
    Chain prior = readOne(access, priorChainId);

    if (!REVIVE_FROM_STATES_ALLOWED.contains(prior.getState()))
      throw new DAOPrivilegeException(String.format("Can't revive a Chain unless it's in %s state",
        CSV.prettyFrom(REVIVE_FROM_STATES_ALLOWED, "or")));

    // save the embed key to re-use on new chain
    String embedKey = prior.getEmbedKey();

    // update the prior chain to failed state and null embed key
    prior.setStateEnum(ChainState.Failed);
    prior.setEmbedKey(null);
    update(access, priorChainId, prior);

    // of new chain with original properties (implicitly created in draft state)
    Chain toCreate = makeClone(prior);
    toCreate.setId(UUID.randomUUID()); // new id
    toCreate.setEmbedKey(embedKey);
    toCreate.setStartAtNow();// [#170273871] Revived chain should always start now
    Chain created = create(access, toCreate);

    // Re-create all chain bindings of original chain
    for (ChainBinding chainBinding : makeClones(chainBindingDAO.readMany(access, ImmutableList.of(priorChainId))))
      chainBindingDAO.create(access, chainBinding.setChainId(created.getId()));

    // update new chain into ready, then fabricate, which begins the new work
    updateState(access, created.getId(), ChainState.Ready);
    updateState(access, created.getId(), ChainState.Fabricate);
    created.setStateEnum(ChainState.Fabricate);

    // publish a notification reporting the event
    log.info("Revived Chain created {} from prior {} because {}", created.getId(), prior.getId(), reason);
    pubSub.publish(String.format("Revived Chain created %s create from prior %s because %s", created.getId(), prior.getId(), reason), MessageType.Info.toString());

    // return newly created chain
    return created;
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
   Generate a Preview Chain embed key

   @return generated Preview Chain embed key
   */
  private String generatePreviewEmbedKey() {
    byte[] L = new byte[previewEmbedKeyLength];
    for (int i = 0; i < previewEmbedKeyLength; i++)
      L[i] = (byte) (secureRandom.nextInt(26) + 'a');
    return String.format("preview_%s", new String(L));
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
    requireAccount(access, chain);

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
        chain.setStartAtNow();
        if (ChainType.Preview.equals(chain.getType()))
          chain.setStopAtInstant(chain.getStartAt().plus(previewLengthMaxHours, HOURS)); // [#174153691]
      case Complete:
      case Failed:
        // no op
        break;
    }
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
