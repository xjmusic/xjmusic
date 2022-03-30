// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.persistence;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.api.*;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.TemplateType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.MessageType;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
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
 Nexus Managers are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class ChainManagerImpl extends ManagerImpl<Chain> implements ChainManager {
  private static final Logger LOG = LoggerFactory.getLogger(ChainManagerImpl.class);
  private static final Set<ChainState> NOTIFY_ON_CHAIN_STATES = ImmutableSet.of(
    ChainState.FABRICATE,
    ChainState.FAILED
  );
  private final SegmentManager segmentManager;
  private final int previewLengthMaxHours;
  private final NotificationProvider pubSub;

  // https://www.pivotaltracker.com/story/show/176375238 Chains should N seconds into the future
  private final int chainStartInFutureSeconds;

  @Inject
  public ChainManagerImpl(
    Environment env,
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore,
    SegmentManager segmentManager,
    NotificationProvider notificationProvider
  ) {
    super(entityFactory, nexusEntityStore);
    this.segmentManager = segmentManager;
    this.pubSub = notificationProvider;

    previewLengthMaxHours = env.getFabricationPreviewLengthMaxHours();
    chainStartInFutureSeconds = env.getChainStartInFutureSeconds();
  }

  @Override
  public Chain bootstrap(
    TemplateType type,
    Chain entity
  ) throws ManagerFatalException, ManagerPrivilegeException, ManagerValidationException {
    try {

      // Chains are always bootstrapped in FABRICATED state and PRODUCTION type
      entity.setState(ChainState.FABRICATE);
      entity.setStartAt(Values.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
      requireUniqueShipKey(entity);
      require(String.format("%s-type", type.toString()), type.toString().equals(entity.getType().toString()));

      // Give model a fresh unique ID and Validate
      entity.setId(UUID.randomUUID());
      validate(entity);

      // return chain
      return store.put(entity);

    } catch (ValueException e) {
      throw new ManagerValidationException(e);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Chain create(Chain entity) throws ManagerFatalException, ManagerPrivilegeException, ManagerValidationException {
    try {

      // [#126] Chains are always created in DRAFT state
      entity.setState(ChainState.DRAFT);

      // Give model a fresh unique ID and Validate
      entity.setId(UUID.randomUUID());
      validate(entity);

      // Further logic based on Chain Type
      requireUniqueShipKey(entity);

      // store and return sanitized payload comprising only the valid Chain
      return store.put(entity);

    } catch (ValueException e) {
      throw new ManagerValidationException(e);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  private void validate(Chain chain) throws ValueException {
    Values.require(chain.getAccountId(), "Account ID");
    Values.require(chain.getName(), "Name");

    if (Values.isEmpty(chain.getType()))
      chain.setType(ChainType.PREVIEW);
    if (Values.isEmpty(chain.getState()))
      chain.setState(ChainState.DRAFT);
    if (Values.isSet(chain.getShipKey()))
      chain.setShipKey(Text.toShipKey(chain.getShipKey()));
  }

  @Override
  public Chain readOne(UUID id) throws ManagerExistenceException, ManagerFatalException {
    try {
      return store.getChain(id)
        .orElseThrow(() -> new ManagerExistenceException(Chain.class, id.toString()));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Chain readOneByShipKey(String rawShipKey) throws ManagerExistenceException, ManagerFatalException {
    try {
      String key = Text.toShipKey(rawShipKey);
      return store.getAllChains().stream()
        .filter(c -> Objects.equals(key, c.getShipKey()))
        .findFirst()
        .orElseThrow(() -> new ManagerExistenceException(Chain.class, rawShipKey));

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readMany(Collection<UUID> accountIds) throws ManagerFatalException {
    try {
      return store.getAllChains().stream()
        .filter(chain -> accountIds.contains(chain.getAccountId()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readManyInState(ChainState state) throws ManagerFatalException {
    try {
      return store.getAllChains().stream()
        .filter(chain -> state.equals(chain.getState()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Chain update(UUID id, Chain chain)
    throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException {
    try {
      // cache existing chain from-state
      var existing = readOne(id);
      ChainState fromState = existing.getState();

      // cannot change type of chain
      if (existing.getType() != chain.getType())
        throw new ManagerValidationException("Cannot modify Chain Type");

      // https://www.pivotaltracker.com/story/show/174153691 Cannot change stop-at time or Ship key of Preview chain
      if (ChainType.PREVIEW == existing.getType()) {
        chain.setStopAt(existing.getStopAt());
        chain.setShipKey(existing.getShipKey());
      }

      // override id (cannot be changed) from existing chain, and then validate
      chain.setId(id);
      validate(chain);

      // If we have a ship key, it must not belong to another chain
      requireUniqueShipKey(chain);

      // Final before-update validation, then store
      beforeUpdate(chain, fromState);

      // [#116] block update Chain state: cannot change chain startAt time after has segments
      if (Values.isSet(chain.getStartAt()))
        if (!existing.getStartAt().equals(chain.getStartAt()))
          if (!segmentManager.readMany(ImmutableList.of(chain.getId())).isEmpty())
            throw new ManagerValidationException("cannot change chain startAt time after it has segments");

      // Commit changes
      store.put(chain);
      return chain;

    } catch (ValueException e) {
      throw new ManagerValidationException(e);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public void updateState(UUID id, ChainState state)
    throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ManagerValidationException {
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
        pubSub.publish(MessageType.Info.toString(), String.format("Updated Chain %s to state %s", chain.getId(), chain.getState()));
      }

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Optional<Segment> buildNextSegmentOrCompleteTheChain(Chain chain, Instant segmentBeginBefore, Instant chainStopCompleteAfter) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException, ManagerValidationException {
    // Get the last segment in the chain
    // If the chain had no last segment, it must be empty; return a template for its first segment
    var maybeLastSegmentInChain = segmentManager.readLastSegment(chain.getId());
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
    if (Values.isSet(lastSegmentInChain.getEndAt()) &&
      Values.isSet(chain.getStopAt()) &&
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
  public void destroy(UUID id) throws ManagerFatalException, ManagerPrivilegeException, ManagerExistenceException {
    try {
      for (Segment segment : segmentManager.readMany(ImmutableList.of()))
        segmentManager.destroy(segment.getId());
      store.deleteChain(id);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readAllFabricating() throws ManagerFatalException {
    try {
      return store.getAllChains().stream()
        .filter(chain -> ChainState.FABRICATE.equals(chain.getState()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
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
    } catch (NexusException | ManagerFatalException | ManagerPrivilegeException | ManagerExistenceException e) {
      LOG.error("Failed to destroy chain for ship key {}", key, e);
    }
  }

  @Override
  public Collection<Chain> readAll() throws NexusException {
    return store.getAllChains();
  }

  @Override
  public Chain put(Chain entity) throws ManagerFatalException {
    try {
      return store.put(entity);

    } catch (NexusException e) {
      throw new ManagerFatalException(e);
    }
  }

  @Override
  public boolean existsForShipKey(String shipKey) {
    String key = Text.toShipKey(shipKey);
    try {
      return store.getAllChains().stream()
        .anyMatch(c -> key.equals(c.getShipKey()));
    } catch (NexusException e) {
      LOG.error("Failed to test if chain exists for ship key: {}", shipKey);
      return false;
    }
  }

  @Override
  public TemplateConfig getTemplateConfig(UUID chainId) throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, ValueException {
    return new TemplateConfig(readOne(chainId).getTemplateConfig());
  }

  /**
   Require that the provided chain is the only one existing with this ship key

   @param chain to test ship key uniqueness of
   @throws ManagerValidationException if another Chain exists with this ship key
   @throws ManagerFatalException      on failure to determine
   */
  private void requireUniqueShipKey(Chain chain) throws ManagerValidationException, ManagerFatalException {
    if (Values.isSet(chain.getShipKey()))
      try {
        var existing = readOneByShipKey(chain.getShipKey());
        if (!Objects.equals(chain.getId(), existing.getId()))
          throw new ManagerValidationException(String.format("Chain already exists with ship key '%s'!", chain.getShipKey()));
      } catch (ManagerExistenceException ignored) {
        // OK if no other chain exists with this ship key
      }
  }

  /**
   Validate access and make other modifications to a chain before update

   @param chain     payload  to test and modify before update
   @param fromState to test for transition from
   @throws ManagerPrivilegeException on insufficient privileges
   */
  private void beforeUpdate(Chain chain, ChainState fromState) throws ManagerPrivilegeException, ManagerValidationException {
    // Conditions based on Chain state
    switch (fromState) {
      case DRAFT -> {
        onlyAllowTransitions(chain.getState(), ChainState.DRAFT, ChainState.READY);
        if (ChainState.READY.equals(chain.getState()) && Objects.isNull(chain.getTemplateId()))
          throw new ManagerValidationException("Chain must come from a Template");
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
        chain.startAt(Values.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
        if (ChainType.PREVIEW.equals(chain.getType())) {
          chain.setStopAt(Values.formatIso8601UTC(
            Instant.parse(chain.getStartAt()).plus(previewLengthMaxHours, HOURS))); // https://www.pivotaltracker.com/story/show/174153691
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
  private void onlyAllowTransitions(ChainState toState, ChainState... allowedStates) throws ManagerPrivilegeException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (ChainState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (search == toState) {
        return;
      }
    }
    throw new ManagerPrivilegeException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }

}
