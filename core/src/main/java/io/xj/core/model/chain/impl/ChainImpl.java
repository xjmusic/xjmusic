//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.impl.SuperEntityImpl;
import io.xj.core.model.payload.Payload;
import io.xj.core.transport.CSV;
import io.xj.core.transport.GsonProvider;
import io.xj.core.util.Text;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#166743281] Chain handles all of its own binding + config entities
 <p>
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainImpl extends SuperEntityImpl implements Chain {
  private final Map<UUID, ChainConfig> configMap = Maps.newHashMap();
  private final Map<UUID, ChainBinding> bindingMap = Maps.newHashMap();
  private final GsonProvider gsonProvider;
  private BigInteger accountId;
  private String name;
  private ChainState state;
  private ChainType type;
  private Instant startAt;
  private Exception startAtException;
  private Instant stopAt;
  private Exception stopAtException;
  @Nullable
  private String embedKey;
  private Exception stateException;
  private Exception typeException;

  /**
   Constructor with Chain id
   */
  @AssistedInject
  public ChainImpl(
    @Assisted("id") BigInteger id,
    GsonProvider gsonProvider
  ) {
    this.id = id;
    this.gsonProvider = gsonProvider;
  }

  /**
   Constructor with no id
   */
  @AssistedInject
  public ChainImpl(
    GsonProvider gsonProvider
  ) {
    this.gsonProvider = gsonProvider;
  }


  @Override
  public ChainBinding add(ChainBinding chainBinding) {
    try {
      requireId("before adding Chain Binding");
      chainBinding.setChainId(getId());
      return SubEntity.add(bindingMap, chainBinding);
    } catch (CoreException e) {
      add(e);
      return chainBinding;
    }
  }

  @Override
  public ChainConfig add(ChainConfig chainConfig) {
    try {
      requireId("before adding Chain Config");
      chainConfig.setChainId(getId());
      return SubEntity.add(configMap, chainConfig);
    } catch (CoreException e) {
      add(e);
      return chainConfig;
    }
  }

  @Override
  public Chain consume(Payload payload) throws CoreException {
    super.consume(payload);
    syncSubEntities(payload, configMap, ChainConfig.class);
    syncSubEntities(payload, bindingMap, ChainBinding.class);
    return this;
  }

  @Override
  public BigInteger getAccountId() {
    return accountId;
  }

  @Override
  public Collection<SubEntity> getAllSubEntities() {
    Collection<SubEntity> out = Lists.newArrayList();
    out.addAll(getConfigs());
    out.addAll(getBindings());
    return out;
  }

  @Override
  public ChainBinding getBinding(UUID id) throws CoreException {
    if (!bindingMap.containsKey(id))
      throw new CoreException(String.format("Found no Binding id=%s", id));
    return bindingMap.get(id);
  }

  @Override
  public Collection<ChainBinding> getBindings() {
    return bindingMap.values();
  }

  @Override
  public ChainConfig getConfig(UUID id) throws CoreException {
    if (!configMap.containsKey(id))
      throw new CoreException(String.format("Found no Config id=%s", id));
    return configMap.get(id);
  }

  @Override
  public Collection<ChainConfig> getConfigs() {
    return configMap.values();
  }

  @Override
  public ChainContent getContent() {
    return ChainContent.of(this);
  }

  @Override
  @Nullable
  public String getEmbedKey() {
    return embedKey;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public BigInteger getParentId() {
    return accountId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .add("state")
      .add("type")
      .add("startAt")
      .add("stopAt")
      .add("embedKey")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Account.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(ChainBinding.class)
      .add(ChainConfig.class)
      .build();
  }

  @Override
  public Instant getStartAt() {
    return startAt;
  }

  @Override
  public ChainState getState() {
    return state;
  }

  @Override
  public Instant getStopAt() {
    return stopAt;
  }

  @Override
  public ChainType getType() {
    return type;
  }

  @Override
  public Chain setAccountId(BigInteger accountId) {
    this.accountId = accountId;
    return this;
  }

  @Override
  public Chain setBindings(Collection<ChainBinding> bindings) {
    bindingMap.clear();
    for (ChainBinding binding : bindings) {
      add(binding);
    }
    return this;
  }

  @Override
  public Chain setConfigs(Collection<ChainConfig> configs) {
    configMap.clear();
    for (ChainConfig config : configs) {
      add(config);
    }
    return this;
  }

  @Override
  public Chain setEmbedKey(String embedKey) {
    String key = Text.toLowerScored(embedKey);
    if (key.isEmpty()) {
      this.embedKey = null;
    } else {
      this.embedKey = key;
    }
    return this;
  }

  @Override
  public Chain setId(BigInteger id) {
    this.id = id;
    return this;
  }

  @Override
  public Chain setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Chain setStartAt(String value) {
    if (Objects.equals("now", Text.toLowerSlug(value))) {
      startAt = Instant.now();
    } else try {
      startAt = Instant.parse(value);
    } catch (Exception e) {
      startAtException = e;
    }
    return this;
  }

  @Override
  public Chain setStartAtInstant(Instant startAtInstant) {
    startAt = startAtInstant;
    return this;
  }

  @Override
  public Chain setState(String state) {
    try {
      this.state = ChainState.validate(state);
    } catch (CoreException e) {
      stateException = e;
    }
    return this;
  }

  @Override
  public Chain setStateEnum(ChainState state) {
    this.state = state;
    return this;
  }

  @Override
  public Chain setType(String type) {
    try {
      this.type = ChainType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  @Override
  public Chain setTypeEnum(ChainType type) {
    this.type = type;
    return this;
  }


  @Override
  public Chain setStopAt(String value) {
    if (Objects.nonNull(value) && !value.isEmpty())
      try {
        stopAt = Instant.parse(value);
      } catch (Exception e) {
        stopAtException = e;
      }
    return this;
  }

  @Override
  public Chain setStopAtInstant(Instant stopAtInstant) {
    stopAt = stopAtInstant;
    return this;
  }

  @Override
  public String toString() {
    return Entity.keyValueString("Chain", ImmutableMap.<String, String>builder()
      .put("id", String.valueOf(id))
      .put("accountId", String.valueOf(accountId))
      .put("name", Text.toSingleQuoted(name))
      .put("state", Text.toSingleQuoted(String.valueOf(state)))
      .put("type", Text.toSingleQuoted(String.valueOf(type)))
      .put("startAt", String.valueOf(startAt))
      .put("stopAt", String.valueOf(stopAt))
      .put("bindings", CSV.join(bindingMap.values().stream().map(ChainBinding::toString).collect(Collectors.toList())))
      .put("configs", CSV.join(configMap.values().stream().map(ChainConfig::toString).collect(Collectors.toList())))
      .put("createdAt", String.valueOf(createdAt))
      .put("updatedAt", String.valueOf(updatedAt))
      .build());
  }

  @Override
  public Chain setContent(String json) {
    ChainContent content = gsonProvider.gson().fromJson(json, ChainContent.class);
    setBindings(content.getBindings());
    setConfigs(content.getConfigs());
    return this;
  }

  @Override
  public Chain setContentCloned(Chain from) {
    setConfigs(from.getConfigs());
    setBindings(from.getBindings());
    return this;
  }

  @Override
  public Chain validateContent() throws CoreException {
    SubEntity.validate(this.getAllSubEntities());

    return this;
  }

  @Override
  public Chain validate() throws CoreException {
    require(accountId, "Account ID");
    require(name, "Name");

    requireNo(typeException, "Type");
    if (isEmpty(type)) type = ChainType.Preview;

    requireNo(stateException, "State");
    if (isEmpty(state)) state = ChainState.Draft;

    requireNo(startAtException, "Start-at");
    require(startAt, "Start-at time");

    requireNo(stopAtException, "Stop-at");
    if (ChainType.Production != type)
      require(stopAt, "Stop-at time (for non-production chain)");

    if (ChainState.Ready == state || ChainState.Fabricate == state)
      if (bindingMap.isEmpty())
        throw new CoreException(String.format("Chain must be bound to %s in order to enter Fabricate state.", "at least one Library, Sequence, or Instrument"));

    return validateContent();
  }

}
