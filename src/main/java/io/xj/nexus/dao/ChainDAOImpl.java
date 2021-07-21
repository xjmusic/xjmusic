// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Segment;
import io.xj.UserRole;
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
 of the logic around adding segments to chains and updating chain state to complete.
 <p>
 Nexus DAOs are Singletons unless some other requirement changes that-- 'cuz here be cyclic dependencies...
 */
@Singleton
public class ChainDAOImpl extends DAOImpl<Chain> implements ChainDAO {
  private static final Logger LOG = LoggerFactory.getLogger(ChainDAOImpl.class);
  private static final Set<Chain.State> NOTIFY_ON_CHAIN_STATES = ImmutableSet.of(
    Chain.State.Fabricate,
    Chain.State.Failed
  );
  private static final Set<Chain.State> REVIVE_FROM_STATES_ALLOWED = ImmutableSet.of(
    Chain.State.Fabricate,
    Chain.State.Complete,
    Chain.State.Failed
  );
  private final ChainBindingDAO chainBindingDAO;
  private final Config config;
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
    ChainBindingDAO chainBindingDAO,
    NotificationProvider notificationProvider
  ) {
    super(entityFactory, nexusEntityStore);
    this.config = config;
    this.segmentDAO = segmentDAO;
    this.pubSub = notificationProvider;

    previewLengthMaxHours = config.getInt("fabrication.previewLengthMaxHours");
    previewEmbedKeyLength = config.getInt("fabrication.previewEmbedKeyLength");
    chainStartInFutureSeconds = config.getInt("nexus.chainStartInFutureSeconds");
    this.chainBindingDAO = chainBindingDAO;
  }

  @Override
  public Chain bootstrap(
    HubClientAccess access,
    Chain entity,
    Collection<ChainBinding> bindings
  ) throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    try {
      Chain.Builder builder = entity.toBuilder();

      // Chains are always bootstrapped in FABRICATED state and PRODUCTION type
      builder.setState(Chain.State.Fabricate);
      builder.setType(Chain.Type.Production);
      builder.setStartAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
      requireAccount(access, builder.getAccountId(), UserRole.Type.Engineer);
      requireUniqueEmbedKey(access, builder);

      // Give model a fresh unique ID and Validate
      builder.setId(UUID.randomUUID().toString());
      validate(builder);

      // store and valid Chain
      Chain chain = store.put(builder.build());

      // Create all chain bindings of bootstrap chain
      for (ChainBinding chainBinding : bindings)
        chainBindingDAO.create(access,
          chainBinding.toBuilder()
            .setChainId(builder.getId())
            .build());

      // return chain
      return chain;

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Chain create(HubClientAccess access, Chain entity) throws DAOFatalException, DAOPrivilegeException, DAOValidationException {
    try {
      Chain.Builder chain = entity.toBuilder();

      // [#126] Chains are always created in DRAFT state
      chain.setState(Chain.State.Draft);

      // logic based on Chain Type
      switch (chain.getType()) {

        case Production:
          requireAccount(access, chain.getAccountId(), UserRole.Type.Engineer);
          requireUniqueEmbedKey(access, chain);
          break;

        case Preview:
          requireAccount(access, chain.getAccountId(), UserRole.Type.Artist);
          chain.setEmbedKey(generatePreviewEmbedKey());
          break;
      }

      // Give model a fresh unique ID and Validate
      chain.setId(UUID.randomUUID().toString());
      validate(chain);

      // store and return sanitized payload comprising only the valid Chain
      return store.put(chain.build());

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  private void validate(Chain.Builder builder) throws ValueException {
    Value.require(builder.getAccountId(), "Account ID");
    Value.require(builder.getName(), "Name");

    if (Value.isEmpty(builder.getType()))
      builder.setType(Chain.Type.Preview);
    if (Value.isEmpty(builder.getState()))
      builder.setState(Chain.State.Draft);
    if (Value.isSet(builder.getEmbedKey()))
      builder.setEmbedKey(Text.toEmbedKey(builder.getEmbedKey()));

    if (Objects.isNull(builder.getConfig())) builder.setConfig("");

    // [#175347578] validate TypeSafe chain config
    // [#177355683] Artist saves Chain config, validate & combine with defaults.
    builder.setConfig(new ChainConfig(builder.build(), config).toString());
  }

  @Override
  public Chain readOne(HubClientAccess access, String id) throws DAOPrivilegeException, DAOExistenceException, DAOFatalException {
    try {
      var chain = store.getChain(id)
        .orElseThrow(() -> new DAOExistenceException(Chain.class, id));
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
  public Collection<Chain> readMany(HubClientAccess access, Collection<String> accountIds) throws DAOFatalException, DAOPrivilegeException {
    try {
      for (String accountId : accountIds) requireAccount(access, accountId);
      return store.getAllChains().stream()
        .filter(chain -> accountIds.contains(chain.getAccountId()))
        .collect(Collectors.toList());

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public Collection<Chain> readManyInState(HubClientAccess access, Chain.State state) throws DAOFatalException, DAOPrivilegeException {
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
  public Chain update(HubClientAccess access, String id, Chain entity)
    throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException {
    try {
      // cache existing chain from-state
      var existing = readOne(access, id);
      Chain.State fromState = existing.getState();
      Chain.Builder builder = entity.toBuilder();

      // cannot change type of chain
      if (existing.getType() != builder.getType())
        throw new DAOValidationException("Cannot modify Chain Type");

      // [#174153691] Cannot change stop-at time or Embed Key of Preview chain
      if (Chain.Type.Preview == existing.getType())
        builder
          .setStopAt(existing.getStopAt())
          .setEmbedKey(existing.getEmbedKey());

      // override id (cannot be changed) from existing chain, and then validate
      builder.setId(id);
      validate(builder);

      // If we have an embed key, it must not belong to another chain
      requireUniqueEmbedKey(access, builder);

      // Final before-update validation, then store
      beforeUpdate(access, builder, fromState);

      // [#116] block update Chain state: cannot change chain startAt time after has segments
      if (Value.isSet(builder.getStartAt()))
        if (!existing.getStartAt().equals(builder.getStartAt()))
          if (!segmentDAO.readMany(access, ImmutableList.of(builder.getId())).isEmpty())
            throw new DAOValidationException("cannot change chain startAt time after it has segments");

      // Commit changes
      var record = builder.build();
      store.put(record);
      return record;

    } catch (ValueException e) {
      throw new DAOValidationException(e);

    } catch (NexusException e) {
      throw new DAOFatalException(e);
    }
  }

  @Override
  public void updateState(HubClientAccess access, String id, Chain.State state)
    throws DAOFatalException, DAOExistenceException, DAOPrivilegeException, DAOValidationException {
    try {
      Chain.Builder chain = readOne(access, id).toBuilder();
      Chain.State fromState = chain.getState();

      // update to chain state only
      chain.setState(state);

      // all standard before-update tests
      beforeUpdate(access, chain, fromState);

      // commit changes and publish notification
      store.put(chain.build());
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
      return Optional.of(Segment.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain.getId())
        .setBeginAt(chain.getStartAt())
        .setOffset(0L)
        .setOffsetMain(0)
        .setType(Segment.Type.Pending)
        .setState(Segment.State.Planned)
        .build());
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
        && Segment.State.Dubbed.equals(lastSegmentInChain.getState())) {
        updateState(access, chain.getId(), Chain.State.Complete);
      }
      LOG.info("Chain[{}] is complete.", Chains.getIdentifier(chain));
      return Optional.empty();
    }

    // Build the template of the segment that follows the last known one
    Segment pilotTemplate = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setBeginAt(lastSegmentInChain.getEndAt())
      .setOffset(lastSegmentInChain.getOffset() + 1)
      .setOffsetMain(lastSegmentInChain.getOffsetMain() + 1)
      .setType(Segment.Type.Pending)
      .setState(Segment.State.Planned)
      .build();
    return Optional.of(pilotTemplate);
  }

  @Override
  public void destroy(HubClientAccess access, String id) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException {
    try {
      var chain = store.getChain(id)
        .orElseThrow(() -> new DAOExistenceException(Chain.class, id));
      requireAccount(access, chain);

      for (ChainBinding chainBinding : chainBindingDAO.readMany(access, ImmutableList.of()))
        chainBindingDAO.destroy(access, chainBinding.getId());

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
      case Production:
        requireAccount(access, chain.getAccountId(), UserRole.Type.Engineer);
        break;

      case Preview:
        requireAccount(access, chain.getAccountId(), UserRole.Type.Engineer, UserRole.Type.Artist);
        break;
    }
  }

  @Override
  public Chain revive(HubClientAccess access, String priorChainId, String reason) throws DAOFatalException, DAOPrivilegeException, DAOExistenceException, DAOValidationException {
    Chain.Builder builder = readOne(access, priorChainId).toBuilder();

    if (!REVIVE_FROM_STATES_ALLOWED.contains(builder.getState()))
      throw new DAOPrivilegeException(String.format("Can't revive a Chain unless it's in %s state",
        CSV.prettyFrom(REVIVE_FROM_STATES_ALLOWED, "or")));

    // save the embed key to re-use on new chain
    String embedKey = builder.getEmbedKey();

    // update the prior chain to failed state and null embed key
    builder.setState(Chain.State.Failed);
    builder.clearEmbedKey();
    update(access, priorChainId, builder.build());

    // of new chain with original properties (implicitly created in draft state)
    builder.setId(UUID.randomUUID().toString()); // new id
    builder.setEmbedKey(embedKey);
    // [#170273871] Revived chain should always start now
    builder.setStartAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
    // [#177191499] When chain is revived, reset its fabricatedAheadSeconds value
    builder.setFabricatedAheadSeconds(0);
    var created = create(access, builder.build());

    // Re-create all chain bindings of original chain
    for (ChainBinding chainBinding : chainBindingDAO.readMany(access, ImmutableList.of(priorChainId)))
      chainBindingDAO.create(access,
        chainBinding.toBuilder()
          .setChainId(created.getId())
          .build());

    // update new chain into ready, then fabricate, which begins the new work
    updateState(access, created.getId(), Chain.State.Ready);
    updateState(access, created.getId(), Chain.State.Fabricate);
    created = created.toBuilder().setState(Chain.State.Fabricate).build();

    // publish a notification reporting the event
    LOG.info("Revived Chain created {} from prior {} because {}", Chains.getIdentifier(created), builder.getId(), reason);
    pubSub.publish(String.format("Revived Chain created %s create from prior %s because %s", Chains.getIdentifier(created), builder.getId(), reason), MessageType.Info.toString());

    // return newly created chain
    return created;
  }

  @Override
  public Chain newInstance() {
    try {
      return entityFactory.getInstance(Chain.class);
    } catch (EntityException ignored) {
      return Chain.getDefaultInstance();
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
  private void requireUniqueEmbedKey(HubClientAccess access, Chain.Builder chain) throws DAOValidationException, DAOFatalException {
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
   @throws DAOFatalException     on impossible transition
   @throws DAOPrivilegeException on insufficient privileges
   */
  private void beforeUpdate(HubClientAccess access, Chain.Builder chain, Chain.State fromState) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException, DAOValidationException {
    // Conditions based on Chain type
    requireAccount(access, chain.build());

    // Conditions based on Chain state
    switch (fromState) {
      case Draft:
        onlyAllowTransitions(chain.getState(), Chain.State.Draft, Chain.State.Ready);
        // block update of Chain away from draft unless Chain is bound to at least one Library, Sequence, or Instrument
        if (Chain.State.Ready.equals(chain.getState()))
          if (chainBindingDAO.readMany(access, ImmutableList.of(chain.getId())).isEmpty())
            throw new DAOValidationException("Chain must be bound to at least one Library, Sequence, or Instrument");
        break;

      case Ready:
        onlyAllowTransitions(chain.getState(), Chain.State.Draft, Chain.State.Ready, Chain.State.Fabricate);
        break;

      case Fabricate:
        onlyAllowTransitions(chain.getState(), Chain.State.Fabricate, Chain.State.Failed, Chain.State.Complete);
        break;

      case Complete:
        onlyAllowTransitions(chain.getState(), Chain.State.Complete);
        break;

      case Failed:
        onlyAllowTransitions(chain.getState(), Chain.State.Failed);
        break;
    }

    // If state is changing, there may be field updates based on the new state
    if (fromState != chain.getState()) switch (chain.getState()) {
      case Draft:
      case Ready:
      case Fabricate:
        chain.setStartAt(Value.formatIso8601UTC(Instant.now().plusSeconds(chainStartInFutureSeconds)));
        if (Chain.Type.Preview.equals(chain.getType()))
          chain.setStopAt(Value.formatIso8601UTC(
            Instant.parse(chain.getStartAt()).plus(previewLengthMaxHours, HOURS))); // [#174153691]
      case Complete:
      case Failed:
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
  private void onlyAllowTransitions(Chain.State toState, Chain.State... allowedStates) throws DAOPrivilegeException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (Chain.State search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (search == toState) {
        return;
      }
    }
    throw new DAOPrivilegeException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }

}
