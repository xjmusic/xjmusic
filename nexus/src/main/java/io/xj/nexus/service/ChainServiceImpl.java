// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.hub.enums.TemplateType;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.MessageType;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.Chains;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.service.exception.ServiceExistenceException;
import io.xj.nexus.service.exception.ServiceFatalException;
import io.xj.nexus.service.exception.ServicePrivilegeException;
import io.xj.nexus.service.exception.ServiceValidationException;
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
 Nexus Services are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class ChainServiceImpl extends ServiceImpl<Chain> implements ChainService {
  private static final Logger LOG = LoggerFactory.getLogger(ChainServiceImpl.class);
  private static final Set<ChainState> NOTIFY_ON_CHAIN_STATES = ImmutableSet.of(
    ChainState.FABRICATE,
    ChainState.FAILED
  );
  private static final Set<ChainState> REVIVE_FROM_STATES_ALLOWED = ImmutableSet.of(
    ChainState.FABRICATE,
    ChainState.COMPLETE,
    ChainState.FAILED
  );
  private final SegmentService segmentService;
  private final int previewLengthMaxHours;
  private final NotificationProvider pubSub;
  private final int previewShipKeyLength;
  private final SecureRandom secureRandom = new SecureRandom();

  // [#176375238] Chains should N seconds into the future
  private final int chainStartInFutureSeconds;

  @Inject
  public ChainServiceImpl(
    Config config,
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    SegmentService segmentService,
    NotificationProvider notificationProvider
  ) {
    super(entityFactory, nexusEntityStore);
    this.segmentService = segmentService;
    this.pubSub = notificationProvider;

    previewLengthMaxHours = config.getInt("fabrication.previewLengthMaxHours");
    previewShipKeyLength = config.getInt("fabrication.previewShipKeyLength");
    chainStartInFutureSeconds = config.getInt("nexus.chainStartInFutureSeconds");
  }

  @Override
  public Chain bootstrap(
    TemplateType type,
    Chain entity
  ) throws ServiceFatalException, ServicePrivilegeException, ServiceValidationException {
    try {

      // Chains are always bootstrapped in FABRICATED state and PRODUCTION type
      entity.setState(ChainState.FABRICATE);
      entity.setStartAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
      requireUniqueShipKey(entity);
      require(String.format("%s-type", type.toString()), type.toString().equals(entity.getType().toString()));

      // Give model a fresh unique ID and Validate
      entity.setId(UUID.randomUUID());
      validate(entity);

      // return chain
      return store.put(entity);

    } catch (ValueException e) {
      throw new ServiceValidationException(e);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Chain create(Chain entity) throws ServiceFatalException, ServicePrivilegeException, ServiceValidationException {
    try {

      // [#126] Chains are always created in DRAFT state
      entity.setState(ChainState.DRAFT);

      // Give model a fresh unique ID and Validate
      entity.setId(UUID.randomUUID());
      validate(entity);

      // Further logic based on Chain Type
      switch (entity.getType()) {
        case PRODUCTION -> requireUniqueShipKey(entity);
        case PREVIEW -> entity.setShipKey(generatePreviewShipKey());
      }

      // store and return sanitized payload comprising only the valid Chain
      return store.put(entity);

    } catch (ValueException e) {
      throw new ServiceValidationException(e);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  private void validate(Chain chain) throws ValueException {
    Value.require(chain.getAccountId(), "Account ID");
    Value.require(chain.getName(), "Name");

    if (Value.isEmpty(chain.getType()))
      chain.setType(ChainType.PREVIEW);
    if (Value.isEmpty(chain.getState()))
      chain.setState(ChainState.DRAFT);
    if (Value.isSet(chain.getShipKey()))
      chain.setShipKey(Text.toShipKey(chain.getShipKey()));
  }

  @Override
  public Chain readOne(UUID id) throws ServiceExistenceException, ServiceFatalException {
    try {
      return store.getChain(id)
        .orElseThrow(() -> new ServiceExistenceException(Chain.class, id.toString()));

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Chain readOneByShipKey(String rawShipKey) throws ServiceExistenceException, ServiceFatalException {
    try {
      String key = Text.toShipKey(rawShipKey);
      return store.getAllChains().stream()
        .filter(c -> Objects.equals(key, c.getShipKey()))
        .findFirst()
        .orElseThrow(() -> new ServiceExistenceException(Chain.class, rawShipKey));

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readMany(Collection<UUID> accountIds) throws ServiceFatalException {
    try {
      return store.getAllChains().stream()
        .filter(chain -> accountIds.contains(chain.getAccountId()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readManyInState(ChainState state) throws ServiceFatalException {
    try {
      return store.getAllChains().stream()
        .filter(chain -> state.equals(chain.getState()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Chain update(UUID id, Chain chain)
    throws ServiceFatalException, ServiceExistenceException, ServicePrivilegeException, ServiceValidationException {
    try {
      // cache existing chain from-state
      var existing = readOne(id);
      ChainState fromState = existing.getState();

      // cannot change type of chain
      if (existing.getType() != chain.getType())
        throw new ServiceValidationException("Cannot modify Chain Type");

      // [#174153691] Cannot change stop-at time or Ship key of Preview chain
      if (ChainType.PREVIEW == existing.getType()) {
        chain.setStopAt(existing.getStopAt());
        chain.setShipKey(existing.getShipKey());
      }

      // override id (cannot be changed) from existing chain, and then validate
      chain.setId(id);
      validate(chain);

      // If we have an ship key, it must not belong to another chain
      requireUniqueShipKey(chain);

      // Final before-update validation, then store
      beforeUpdate(chain, fromState);

      // [#116] block update Chain state: cannot change chain startAt time after has segments
      if (Value.isSet(chain.getStartAt()))
        if (!existing.getStartAt().equals(chain.getStartAt()))
          if (!segmentService.readMany(ImmutableList.of(chain.getId())).isEmpty())
            throw new ServiceValidationException("cannot change chain startAt time after it has segments");

      // Commit changes
      store.put(chain);
      return chain;

    } catch (ValueException e) {
      throw new ServiceValidationException(e);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public void updateState(UUID id, ChainState state)
    throws ServiceFatalException, ServiceExistenceException, ServicePrivilegeException, ServiceValidationException {
    try {
      Chain chain = readOne(id);
      ChainState fromState = chain.getState();

      // update to chain state only
      chain.setState(state);

      // all standard before-update tests
      beforeUpdate(chain, fromState);

      // commit changes and publish notification
      store.put(chain);
      if (NOTIFY_ON_CHAIN_STATES.contains(chain.getState())) {
        LOG.info("Updated Chain {} to state {}", chain.getId(), chain.getState());
        pubSub.publish(String.format("Updated Chain %s to state %s", chain.getId(), chain.getState()), MessageType.Info.toString());
      }

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Optional<Segment> buildNextSegmentOrCompleteTheChain(Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteAfter) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException, ServiceValidationException {
    // Get the last segment in the chain
    // If the chain had no last segment, it must be empty; return a template for its first segment
    var maybeLastSegmentInChain = segmentService.readLastSegment(chain.getId());
    if (maybeLastSegmentInChain.isEmpty()) {
      var seg = new Segment();
      seg.setId(UUID.randomUUID());
      seg.setChainId(chain.getId());
      seg.setBeginAt(chain.getStartAt());
      seg.setOffset(0L);
      seg.setDelta(0);
      seg.setType(SegmentType.PENDING);
      seg.setState(SegmentState.PLANNED);
      return Optional.of(seg);
    }
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
        updateState(chain.getId(), ChainState.COMPLETE);
      }
      LOG.info("Chain[{}] is complete.", Chains.getIdentifier(chain));
      return Optional.empty();
    }

    // Build the template of the segment that follows the last known one
    var seg = new Segment();
    seg.setId(UUID.randomUUID());
    seg.setChainId(chain.getId());
    seg.setBeginAt(lastSegmentInChain.getEndAt());
    seg.setOffset(lastSegmentInChain.getOffset() + 1);
    seg.setDelta(lastSegmentInChain.getDelta());
    seg.setType(SegmentType.PENDING);
    seg.setState(SegmentState.PLANNED);
    return Optional.of(seg);
  }

  @Override
  public void destroy(UUID id) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException {
    try {
      for (Segment segment : segmentService.readMany(ImmutableList.of()))
        segmentService.destroy(segment.getId());
      store.deleteChain(id);

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public Chain revive(UUID priorChainId, String reason) throws ServiceFatalException, ServicePrivilegeException, ServiceExistenceException, ServiceValidationException {
    try {
      Chain prior = readOne(priorChainId);

      if (!REVIVE_FROM_STATES_ALLOWED.contains(prior.getState()))
        throw new ServicePrivilegeException(String.format("Can't revive a Chain unless it's in %s state",
          CSV.prettyFrom(REVIVE_FROM_STATES_ALLOWED, "or")));

      // save the ship key to re-use on new chain
      String shipKey = prior.getShipKey();

      // update the prior chain to failed state and null ship key
      prior.setState(ChainState.FAILED);
      prior.setShipKey(null);
      update(priorChainId, prior);

      // of new chain with original properties (implicitly created in draft state)
      var cloned = entityFactory.clone(prior);
      cloned.setId(UUID.randomUUID()); // new id
      cloned.setShipKey(shipKey);
      // [#170273871] Revived chain should always start now
      cloned.startAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
      // [#177191499] When chain is revived, reset its fabricatedAheadSeconds value
      cloned.fabricatedAheadSeconds(0.0);
      Chain created = create(cloned);

      // update new chain into ready, then fabricate, which begins the new work
      updateState(created.getId(), ChainState.READY);
      updateState(created.getId(), ChainState.FABRICATE);
      created.setState(ChainState.FABRICATE);

      // publish a notification reporting the event
      LOG.info("Revived Chain created {} from revived {} because {}", Chains.getIdentifier(created), created.getId(), reason);
      pubSub.publish(String.format("Revived Chain created %s create from revived %s because %s", Chains.getIdentifier(created), created.getId(), reason), MessageType.Info.toString());

      // return newly created chain
      return created;
    } catch (EntityException e) {
      throw new ServiceFatalException("Failed to clone prior chain", e);
    }
  }

  @Override
  public Collection<Chain> readAllFabricating() throws ServiceFatalException {
    try {
      return store.getAllChains().stream()
        .filter(chain -> ChainState.FABRICATE.equals(chain.getState()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ServiceFatalException(e);
    }
  }

  @Override
  public void destroyIfExistsForShipKey(String key) {
    try {
      var chain = store.getAllChains().stream()
        .filter(c -> Objects.equals(key, c.getShipKey()))
        .findAny();
      if (chain.isPresent())
        destroy(chain.get().getId());
    } catch (NexusException | ServiceFatalException | ServicePrivilegeException | ServiceExistenceException e) {
      LOG.error("Failed to destroy chain for ship key {}", key, e);
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
   Generate a Preview Chain ship key

   @return generated Preview Chain ship key
   */
  private String generatePreviewShipKey() {
    byte[] L = new byte[previewShipKeyLength];
    for (int i = 0; i < previewShipKeyLength; i++)
      L[i] = (byte) (secureRandom.nextInt(26) + 'a');
    return String.format("preview_%s", new String(L));
  }

  /**
   Require that the provided chain is the only one existing with this ship key

   @param chain to test ship key uniqueness of
   @throws ServiceValidationException if another Chain exists with this ship key
   @throws ServiceFatalException      on failure to determine
   */
  private void requireUniqueShipKey(Chain chain) throws ServiceValidationException, ServiceFatalException {
    if (Value.isSet(chain.getShipKey()))
      try {
        var existing = readOneByShipKey(chain.getShipKey());
        if (!Objects.equals(chain.getId(), existing.getId()))
          throw new ServiceValidationException(String.format("Chain already exists with ship key '%s'!", chain.getShipKey()));
      } catch (ServiceExistenceException ignored) {
        // OK if no other chain exists with this ship key
      }
  }

  /**
   Validate access and make other modifications to a chain before update

   @param chain     payload  to test and modify before update
   @param fromState to test for transition from
   @throws ServicePrivilegeException on insufficient privileges
   */
  private void beforeUpdate(Chain chain, ChainState fromState) throws ServicePrivilegeException, ServiceValidationException {
    // Conditions based on Chain state
    switch (fromState) {
      case DRAFT -> {
        onlyAllowTransitions(chain.getState(), ChainState.DRAFT, ChainState.READY);
        if (ChainState.READY.equals(chain.getState()) && Objects.isNull(chain.getTemplateId()))
          throw new ServiceValidationException("Chain must come from a Template");
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
        if (ChainType.PREVIEW.equals(chain.getType())) {
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
  private void onlyAllowTransitions(ChainState toState, ChainState... allowedStates) throws ServicePrivilegeException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (ChainState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (search == toState) {
        return;
      }
    }
    throw new ServicePrivilegeException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }

}
