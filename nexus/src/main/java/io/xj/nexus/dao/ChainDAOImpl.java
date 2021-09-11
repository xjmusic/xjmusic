// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.api.TemplateType;
import io.xj.api.UserRoleType;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.MessageType;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.persistence.NexusEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
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
 of the logic around adding segments to chain and updating chain state to complete.
 <p>
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class ChainDAOImpl extends DAOImpl<Chain> implements ChainDAO {
  private static final Logger LOG = LoggerFactory.getLogger(ChainDAOImpl.class);
  private static final Set<ChainState> NOTIFY_ON_CHAIN_STATES = ImmutableSet.of(
    ChainState.FABRICATE,
    ChainState.FAILED
  );
  private static final Set<ChainState> REVIVE_FROM_STATES_ALLOWED = ImmutableSet.of(
    ChainState.FABRICATE,
    ChainState.COMPLETE,
    ChainState.FAILED
  );
  private final SegmentDAO segmentDAO;
  private final int previewLengthMaxHours;
  private final NotificationProvider pubSub;
  private final int previewEmbedKeyLength;
  private final SecureRandom secureRandom = new SecureRandom();

  // [#176375238] Chains should N seconds into the future
  private final int chainStartInFutureSeconds;

  @Inject
  public ChainDAOImpl(
    Config config,
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    SegmentDAO segmentDAO,
    NotificationProvider notificationProvider
  ) {
    super(entityFactory, nexusEntityStore);
    this.segmentDAO = segmentDAO;
    this.pubSub = notificationProvider;

    previewLengthMaxHours = config.getInt("fabrication.previewLengthMaxHours");
    previewEmbedKeyLength = config.getInt("fabrication.previewEmbedKeyLength");
    chainStartInFutureSeconds = config.getInt("nexus.chainStartInFutureSeconds");
  }

  @Override
  public Chain bootstrap(
    HubClientAccess access,
    TemplateType type,
    Chain entity
  ) throws DAOFatalException, DAOPrivilegeException, DAOValidationException {
    try {

      // Chains are always bootstrapped in FABRICATED state and PRODUCTION type
      entity.setState(ChainState.FABRICATE);
      entity.setStartAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
      requireAccount(access, entity.getAccountId(), UserRoleType.ENGINEER);
      requireUniqueEmbedKey(access, entity);
      require(String.format("%s-type", type.toString()), type.equals(entity.getType()));

      // Give model a fresh unique ID and Validate
      entity.setId(UUID.randomUUID());
      validate(entity);

      // return chain
      return store.put(entity);

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Chain create(HubClientAccess access, Chain entity) throws DAOFatalException, DAOPrivilegeException, DAOValidationException {
    try {

      // [#126] Chains are always created in DRAFT state
      entity.setState(ChainState.DRAFT);

      // Give model a fresh unique ID and Validate
      entity.setId(UUID.randomUUID());
      validate(entity);

      // Further logic based on Chain Type
      switch (entity.getType()) {
        case PRODUCTION -> {
          requireAccount(access, entity.getAccountId(), UserRoleType.ENGINEER);
          requireUniqueEmbedKey(access, entity);
        }
        case PREVIEW -> {
          requireAccount(access, entity.getAccountId(), UserRoleType.ARTIST);
          entity.setEmbedKey(generatePreviewEmbedKey());
        }
      }

      // store and return sanitized payload comprising only the valid Chain
      return store.put(entity);

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  private void validate(Chain chain) throws ValueException {
    Value.require(chain.getAccountId(), "Account ID");
    Value.require(chain.getName(), "Name");

    if (Value.isEmpty(chain.getType()))
      chain.setType(TemplateType.PREVIEW);
    if (Value.isEmpty(chain.getState()))
      chain.setState(ChainState.DRAFT);
    if (Value.isSet(chain.getEmbedKey()))
      chain.setEmbedKey(Text.toEmbedKey(chain.getEmbedKey()));
  }

  @Override
  public Chain readOne(HubClientAccess access, UUID id) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      var chain = store.getChain(id)
        .orElseThrow(() -> new DAOExistenceException(Chain.class, id.toString()));
      requireAccount(access, chain);
      return chain;

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Chain readOneByEmbedKey(HubClientAccess access, String rawEmbedKey) throws DAOExistenceException, DAOFatalException {
    try {
      String key = Text.toEmbedKey(rawEmbedKey);
      return store.getAllChains().stream()
        .filter(c -> Objects.equals(key, c.getEmbedKey()))
        .findFirst()
        .orElseThrow(() -> new DAOExistenceException(Chain.class, rawEmbedKey));

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readMany(HubClientAccess access, Collection<UUID> accountIds) throws DAOFatalException, DAOPrivilegeException {
    try {
      for (UUID accountId : accountIds) requireAccount(access, accountId);
      return store.getAllChains().stream()
        .filter(chain -> accountIds.contains(chain.getAccountId()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readManyInState(HubClientAccess access, ChainState state) throws DAOFatalException, DAOPrivilegeException {
    try {
      Collection<Chain> chains = store.getAllChains().stream()
        .filter(chain -> state.equals(chain.getState()))
        .collect(Collectors.toList());
      for (Chain chain : chains)
        requireAccount(access, chain);
      return chains;

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Chain update(HubClientAccess access, UUID id, Chain chain)
    throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException {
    try {
      // cache existing chain from-state
      var existing = readOne(access, id);
      ChainState fromState = existing.getState();

      // cannot change type of chain
      if (existing.getType() != chain.getType())
        throw new DAOValidationException("Cannot modify Chain Type");

      // [#174153691] Cannot change stop-at time or Embed Key of Preview chain
      if (TemplateType.PREVIEW == existing.getType()) {
        chain.setStopAt(existing.getStopAt());
        chain.setEmbedKey(existing.getEmbedKey());
      }

      // override id (cannot be changed) from existing chain, and then validate
      chain.setId(id);
      validate(chain);

      // If we have an embed key, it must not belong to another chain
      requireUniqueEmbedKey(access, chain);

      // Final before-update validation, then store
      beforeUpdate(access, chain, fromState);

      // [#116] block update Chain state: cannot change chain startAt time after has segments
      if (Value.isSet(chain.getStartAt()))
        if (!existing.getStartAt().equals(chain.getStartAt()))
          if (!segmentDAO.readMany(access, ImmutableList.of(chain.getId())).isEmpty())
            throw new DAOValidationException("cannot change chain startAt time after it has segments");

      // Commit changes
      store.put(chain);
      return chain;

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (NexusException e) {
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
      chain.setState(state);

      // all standard before-update tests
      beforeUpdate(access, chain, fromState);

      // commit changes and publish notification
      store.put(chain);
      if (NOTIFY_ON_CHAIN_STATES.contains(chain.getState())) {
        LOG.info("Updated Chain {} to state {}", chain.getId(), chain.getState());
        pubSub.publish(String.format("Updated Chain %s to state %s", chain.getId(), chain.getState()), MessageType.Info.toString());
      }

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Optional<Segment> buildNextSegmentOrCompleteTheChain(HubClientAccess access, Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteAfter) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException {
    requireTopLevel(access);

    // Get the last segment in the chain
    // If the chain had no last segment, it must be empty; return a template for its first segment
    var maybeLastSegmentInChain = segmentDAO.readLastSegment(access, chain.getId());
    if (maybeLastSegmentInChain.isEmpty())
      return Optional.of(new Segment()
        .id(UUID.randomUUID())
        .chainId(chain.getId())
        .beginAt(chain.getStartAt())
        .offset(0L)
        .delta(0)
        .type(SegmentType.PENDING)
        .state(SegmentState.PLANNED));
    var lastSegmentInChain = maybeLastSegmentInChain.get();

    // [#204] Craft process updates Chain to COMPLETE state when the final segment is in dubbed state.
    if (Value.isSet(lastSegmentInChain.getEndAt()) &&
      Value.isSet(chain.getStopAt()) &&
      !Strings.isNullOrEmpty(lastSegmentInChain.getEndAt()) &&
      Instant.parse(lastSegmentInChain.getEndAt())
        .isAfter(Instant.parse(chain.getStopAt()))) {
      // this is where we check to see if the chain is ready to be COMPLETE.
      if (Instant.parse(chain.getStopAt()).isBefore(chainStopCompleteAfter)
        // and [#122] require the last segment in the chain to be in state DUBBED.
        && SegmentState.DUBBED.equals(lastSegmentInChain.getState())) {
        updateState(access, chain.getId(), ChainState.COMPLETE);
      }
      LOG.info("Chain[{}] is complete.", Chains.getIdentifier(chain));
      return Optional.empty();
    }

    // Build the template of the segment that follows the last known one
    return Optional.of(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .beginAt(lastSegmentInChain.getEndAt())
      .offset(lastSegmentInChain.getOffset() + 1)
      .delta(lastSegmentInChain.getDelta())
      .type(SegmentType.PENDING)
      .state(SegmentState.PLANNED));
  }

  @Override
  public void destroy(HubClientAccess access, UUID id) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    try {
      var chain = store.getChain(id)
        .orElseThrow(() -> new DAOExistenceException(Chain.class, id.toString()));
      requireAccount(access, chain);

      for (Segment segment : segmentDAO.readMany(access, ImmutableList.of()))
        segmentDAO.destroy(access, segment.getId());

      store.deleteChain(id);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void requireAccount(HubClientAccess access, Chain chain) throws DAOPrivilegeException {
    switch (chain.getType()) {
      case PRODUCTION -> requireAccount(access, chain.getAccountId(), UserRoleType.ENGINEER);
      case PREVIEW -> requireAccount(access, chain.getAccountId(), UserRoleType.ENGINEER, UserRoleType.ARTIST);
    }
  }

  @Override
  public Chain revive(HubClientAccess access, UUID priorChainId, String reason) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException {
    try {
      Chain prior = readOne(access, priorChainId);

      if (!REVIVE_FROM_STATES_ALLOWED.contains(prior.getState()))
        throw new DAOPrivilegeException(String.format("Can't revive a Chain unless it's in %s state",
          CSV.prettyFrom(REVIVE_FROM_STATES_ALLOWED, "or")));

      // save the embed key to re-use on new chain
      String embedKey = prior.getEmbedKey();

      // update the prior chain to failed state and null embed key
      prior.setState(ChainState.FAILED);
      prior.setEmbedKey(null);
      update(access, priorChainId, prior);

      // of new chain with original properties (implicitly created in draft state)
      Chain created = create(access, entityFactory.clone(prior)
        .id(UUID.randomUUID()) // new id
        .embedKey(embedKey)
        // [#170273871] Revived chain should always start now
        .startAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)))
        // [#177191499] When chain is revived, reset its fabricatedAheadSeconds value
        .fabricatedAheadSeconds(0.0));

      // update new chain into ready, then fabricate, which begins the new work
      updateState(access, created.getId(), ChainState.READY);
      updateState(access, created.getId(), ChainState.FABRICATE);
      created = created.state(ChainState.FABRICATE);

      // publish a notification reporting the event
      LOG.info("Revived Chain created {} from revived {} because {}", Chains.getIdentifier(created), created.getId(), reason);
      pubSub.publish(String.format("Revived Chain created %s create from revived %s because %s", Chains.getIdentifier(created), created.getId(), reason), MessageType.Info.toString());

      // return newly created chain
      return created;
    } catch (EntityException e) {
      throw new DAOFatalException("Failed to clone prior chain", e);
    }
  }

  @Override
  public Collection<Chain> readAllFabricating(HubClientAccess access) throws DAOPrivilegeException, DAOFatalException {
    try {
      requireTopLevel(access);
      return store.getAllChains().stream()
        .filter(chain -> ChainState.FABRICATE.equals(chain.getState()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void destroyIfExistsForEmbedKey(HubClientAccess access, String key) {
    try {
      var chain = store.getAllChains().stream()
        .filter(c -> Objects.equals(key, c.getEmbedKey()))
        .findAny();
      if (chain.isPresent())
        destroy(access, chain.get().getId());
    } catch (NexusException | DAOFatalException | DAOPrivilegeException | DAOExistenceException e) {
      LOG.error("Failed to destroy chain for embed key {}", key, e);
    }
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
    if (Value.isSet(chain.getEmbedKey()))
      try {
        var existing = readOneByEmbedKey(access, chain.getEmbedKey());
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
   @throws DAOPrivilegeException on insufficient privileges
   */
  private void beforeUpdate(HubClientAccess access, Chain chain, ChainState fromState) throws DAOPrivilegeException, DAOValidationException {
    // Conditions based on Chain type
    requireAccount(access, chain);

    // Conditions based on Chain state
    switch (fromState) {
      case DRAFT -> {
        onlyAllowTransitions(chain.getState(), ChainState.DRAFT, ChainState.READY);
        if (ChainState.READY.equals(chain.getState()) && Objects.isNull(chain.getTemplateId()))
          throw new DAOValidationException("Chain must come from a Template");
      }
      case READY -> onlyAllowTransitions(chain.getState(), ChainState.DRAFT, ChainState.READY, ChainState.FABRICATE);
      case FABRICATE -> onlyAllowTransitions(chain.getState(), ChainState.FABRICATE, ChainState.FAILED, ChainState.COMPLETE);
      case COMPLETE -> onlyAllowTransitions(chain.getState(), ChainState.COMPLETE);
      case FAILED -> onlyAllowTransitions(chain.getState(), ChainState.FAILED);
    }

    // If state is changing, there may be field updates based on the new state
    if (fromState != chain.getState()) switch (chain.getState()) {
      case DRAFT:
      case READY:
      case FABRICATE:
        chain.startAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
        if (TemplateType.PREVIEW.equals(chain.getType())) {
          chain.setStopAt(Value.formatIso8601UTC(
            Instant.parse(chain.getStartAt()).plus(previewLengthMaxHours, HOURS))); // [#174153691]
        }
      case COMPLETE:
      case FAILED:
      default:
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
