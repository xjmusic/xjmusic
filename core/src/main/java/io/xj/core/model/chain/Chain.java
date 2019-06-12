//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.chain;

import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.SuperEntity;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 [#166743281] Chain handles all of its own binding + config entities
 */
public interface Chain extends SuperEntity {

  /**
   Add a ChainBinding
   + If there are any exceptions, store them in the SuperEntity errors

   @param chainBinding to add
   @return newly added ChainBinding with generated id
   */
  ChainBinding add(ChainBinding chainBinding);

  /**
   Add a ChainConfig
   + If there are any exceptions, store them in the SuperEntity errors

   @param chainConfig to add
   @return newly added ChainConfig with generated id
   */
  ChainConfig add(ChainConfig chainConfig);

  /**
   Get Account Id

   @return account id
   */
  BigInteger getAccountId();

  /**
   Get all entities

   @return collection of entities
   */
  Collection<SubEntity> getAllSubEntities();

  /**
   Get a ChainBinding by id

   @param id of chain binding
   @return chain binding
   @throws CoreException if none is found with specified id
   */
  ChainBinding getBinding(UUID id) throws CoreException;

  /**
   Get all Chain Bindings

   @return Chain Bindings
   */
  Collection<ChainBinding> getBindings();
  
  /**
   Get a ChainConfig by id

   @param id of chain config
   @return chain config
   @throws CoreException if none is found with specified id
   */
  ChainConfig getConfig(UUID id) throws CoreException;

  /**
   Get all Chain Configs

   @return Chain Configs
   */
  Collection<ChainConfig> getConfigs();

  /**
   Get embed key  (optional value, may return null)

   @return embed key, or null if none is set
   */
  @Nullable
  String getEmbedKey();

  /**
   Get name

   @return name
   */
  String getName();

  /**
   Get start-at instant

   @return start-at instant
   */
  Instant getStartAt();

  /**
   Get state

   @return state
   */
  ChainState getState();

  /**
   Get stop-at instant

   @return stop-at instant
   */
  Instant getStopAt();

  /**
   Get type

   @return type
   */
  ChainType getType();

  /**
   Set Account Id

   @param accountId to set
   @return this Chain (for chaining setters)
   */
  Chain setAccountId(BigInteger accountId);

  /**
   Set all Bindings
   + If there are any exceptions, store them in the SuperEntity errors

   @param bindings to set
   @return this Chain (for chaining setters)
   */
  Chain setBindings(Collection<ChainBinding> bindings);

  /**
   Set all Configs
   + If there are any exceptions, store them in the SuperEntity errors

   @param configs to set
   @return this Chain (for chaining setters)
   */
  Chain setConfigs(Collection<ChainConfig> configs);

  /**
   [#166743281] Chain handles all of its own binding + config entities

   @return Chain for chaining methods
   */
  Chain setContent(String json) throws CoreException;

  /**
   Set all content of chain, cloned from another source chain, with all new UUID, preserving relationships.

   @param from chain
   @return this Chain (for chaining methods)
   */
  Chain setContentCloned(Chain from) throws CoreException;

  /**
   Set Embed Key

   @param embedKey to set
   @return this Chain (for chaining setters)
   */
  Chain setEmbedKey(String embedKey);

  @Override
  Chain setId(BigInteger id);

  /**
   Set Name

   @param name to set
   @return this Chain (for chaining setters)
   */
  Chain setName(String name);

  /**
   Set Chain start-at time, or set it to "now" for the current time.

   @param value "now" for current time, or ISO-8601 timestamp
   @return this Chain (for chaining setters)
   */
  Chain setStartAt(String value);

  /**
   Set chain start-at at instant

   @param startAtInstant to set
   @return this Chain (for chaining setters)
   */
  Chain setStartAtInstant(Instant startAtInstant);

  /**
   Set state of chain

   @param state to set
   @return this Chain (for chaining setters)
   */
  Chain setState(String state);

  /**
   Set state of chain, by enum

   @param state enum to set
   @return this Chain (for chaining setters)
   */
  Chain setStateEnum(ChainState state);

  /**
   Set state of chain

   @param type to set
   @return this Chain (for chaining setters)
   */
  Chain setType(String type);

  /**
   Set state of chain, by enum

   @param type enum to set
   @return this Chain (for chaining setters)
   */
  Chain setTypeEnum(ChainType type);

  /**
   Set Stop-at value

   @param value to set
   @return this Chain (for chaining setters)
   */
  Chain setStopAt(String value);

  /**
   Set Stop-at instant

   @param stopAtInstant to set
   @return this Chain (for chaining setters)
   */
  Chain setStopAtInstant(Instant stopAtInstant);
}
