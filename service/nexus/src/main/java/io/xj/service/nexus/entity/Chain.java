// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.entity;

import com.google.common.collect.ImmutableMap;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.Entity;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Account;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 [#166743281] Chain handles all of its own binding + config entities
 */
public class Chain extends Entity {
  private static final long NOW_PLUS_SECONDS = 60;
  private UUID accountId;
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
   Get a new Chain

   @return new Chain
   */
  public static Chain create() {
    return new Chain().setId(UUID.randomUUID());
  }

  /**
   Get a new Chain

   @param state of Chain
   @return new Chain
   */
  public static Chain create(ChainState state) {
    return create()
      .setStateEnum(state);
  }

  /**
   Get a new Chain

   @param account  of Chain
   @param name     of Chain
   @param type     of Chain
   @param state    of Chain
   @param startAt  of Chain
   @param stopAt   of Chain
   @param embedKey of Chain
   @return new Chain
   */
  public static Chain create(Account account, String name, ChainType type, ChainState state, Instant startAt, @Nullable Instant stopAt, @Nullable String embedKey) {
    return create(state)
      .setAccountId(account.getId())
      .setTypeEnum(type)
      .setName(name)
      .setStartAtInstant(startAt)
      .setStopAtInstant(stopAt)
      .setEmbedKey(embedKey);
  }

  /**
   Format an embed key

   @param embedKey to format
   @return formatted embed key
   */
  public static String toEmbedKey(String embedKey) {
    return Text.toLowerScored(embedKey);
  }

  /**
   Is a value not present?

   @param value to test
   @return true if null or empty
   */
  public static boolean isEmpty(Object value) {
    return Objects.isNull(value) || String.valueOf(value).isEmpty();
  }

  /**
   get AccountId

   @return AccountId
   */
  public UUID getAccountId() {
    return accountId;
  }

  /**
   get EmbedKey

   @return EmbedKey
   */
  @Nullable
  public String getEmbedKey() {
    return embedKey;
  }

  /**
   get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  /**
   get StartAt

   @return StartAt
   */
  public Instant getStartAt() {
    return startAt;
  }

  /**
   get State

   @return State
   */
  public ChainState getState() {
    return state;
  }

  /**
   get StopAt

   @return StopAt
   */
  public Instant getStopAt() {
    return stopAt;
  }

  /**
   get Type

   @return Type
   */
  public ChainType getType() {
    return type;
  }

  /**
   Set account id

   @param accountId to set
   @return this Chain (for chaining setters)
   */
  public Chain setAccountId(UUID accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   set EmbedKey

   @param embedKey to set
   @return this Chain (for chaining setters)
   */
  public Chain setEmbedKey(String embedKey) {
    String key = toEmbedKey(embedKey);
    if (key.isEmpty()) {
      this.embedKey = null;
    } else {
      this.embedKey = key;
    }
    return this;
  }

  /**
   set Id

   @param id to set
   @return this Chain (for chaining setters)
   */
  public Chain setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   set Name

   @param name to set
   @return this Chain (for chaining setters)
   */
  public Chain setName(String name) {
    this.name = name;
    return this;
  }

  /**
   set StartAt

   @param value to set
   @return this Chain (for chaining setters)
   */
  public Chain setStartAt(String value) {
    if (Objects.equals("now", Text.toLowerSlug(value))) {
      return setStartAtNow();
    } else try {
      startAt = Instant.parse(value);
    } catch (Exception e) {
      startAtException = e;
    }
    return this;
  }

  /**
   set StartAt to now (plus constant # of seconds)

   @return this Chain (for chaining setters)
   */
  public Chain setStartAtNow() {
    startAt = Instant.now().plusSeconds(NOW_PLUS_SECONDS);
    return this;
  }

  /**
   set StartAtInstant

   @param startAtInstant to set
   @return this Chain (for chaining setters)
   */
  public Chain setStartAtInstant(Instant startAtInstant) {
    startAt = startAtInstant;
    return this;
  }

  /**
   set State

   @param state to set
   @return this Chain (for chaining setters)
   */
  public Chain setState(String state) {
    try {
      this.state = ChainState.validate(state);
    } catch (ValueException e) {
      stateException = e;
    }
    return this;
  }

  /**
   set StateEnum

   @param state to set
   @return this Chain (for chaining setters)
   */
  public Chain setStateEnum(ChainState state) {
    this.state = state;
    return this;
  }

  /**
   set Type

   @param type to set
   @return this Chain (for chaining setters)
   */
  public Chain setType(String type) {
    try {
      this.type = ChainType.validate(type);
    } catch (ValueException e) {
      typeException = e;
    }
    return this;
  }

  /**
   set TypeEnum

   @param type to set
   @return this Chain (for chaining setters)
   */
  public Chain setTypeEnum(ChainType type) {
    this.type = type;
    return this;
  }

  /**
   set StopAt

   @param value to set
   @return this Chain (for chaining setters)
   */
  public Chain setStopAt(String value) {
    if (Objects.nonNull(value) && !value.isEmpty())
      try {
        stopAt = Instant.parse(value);
      } catch (Exception e) {
        stopAtException = e;
      }
    return this;
  }

/*
  FUTURE address chain cloning
  public Chain setContentCloned(Chain of) {
    setConfigs(of.getConfigs());
    setBindings(of.getBindings());
    return this;
  }
*/

  /**
   set StopAtInstant

   @param stopAtInstant to set
   @return this Chain (for chaining setters)
   */
  public Chain setStopAtInstant(Instant stopAtInstant) {
    stopAt = stopAtInstant;
    return this;
  }

  @Override
  public String toString() {
    return Entities.toKeyValueString(Chain.class.getSimpleName(), ImmutableMap.<String, String>builder()
      .put("id", String.valueOf(id))
      .put("embedKey", Objects.nonNull(embedKey) ? Text.toSingleQuoted(embedKey) : "null")
      .put("accountId", String.valueOf(accountId))
      .put("name", Text.toSingleQuoted(name))
      .put("state", Text.toSingleQuoted(String.valueOf(state)))
      .put("type", Text.toSingleQuoted(String.valueOf(type)))
      .put("startAt", String.valueOf(startAt))
      .put("stopAt", String.valueOf(stopAt))
      .put("createdAt", String.valueOf(createdAt))
      .put("updatedAt", String.valueOf(updatedAt))
      .build());
  }

  @Override
  public void validate() throws ValueException {
    Value.require(accountId, "Account ID");
    Value.require(name, "Name");

    Value.requireNo(typeException, "Type");
    if (isEmpty(type)) type = ChainType.Preview;

    Value.requireNo(stateException, "State");
    if (isEmpty(state)) state = ChainState.Draft;

    Value.requireNo(startAtException, "Start-at");
    Value.requireNo(stopAtException, "Stop-at");
  }

  /**
   Whether this Chain is a Production-type Chain with Start-at before the specified time

   @param threshold of time to test whether Production-type Chain is before
   @return true if this Chain is a Production-type Chain with Start-at before the specified time
   */
  public boolean isProductionStartedBefore(Instant threshold) {
    return ChainType.Production.equals(getType()) && getStartAt().isBefore(threshold);
  }
}
