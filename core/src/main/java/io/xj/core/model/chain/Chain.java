// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.util.Text;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Chain extends EntityImpl {
  public static final String KEY_ONE = "chain";
  public static final String KEY_MANY = "chains";
  private BigInteger accountId;
  private String name;
  private String _state; // hold value before validation
  private ChainState state;
  private String _type; // hold value before validation
  private ChainType type;
  private Instant startAt;
  private String startAtError;
  private Instant stopAt;
  private String stopAtError;
  @Nullable
  private String embedKey;

  public Chain() {
  }

  public Chain(BigInteger id) {
    this.id = id;
  }

  public BigInteger getAccountId() {
    return accountId;
  }

  public Chain setAccountId(BigInteger value) {
    accountId = value;
    return this;
  }

  public String getName() {
    return name;
  }

  public Chain setName(String value) {
    name = value;
    return this;
  }

  public ChainState getState() {
    return state;
  }

  public Chain setState(String value) {
    _state = value;
    return this;
  }

  public Chain setStateEnum(ChainState state) {
    this.state = state;
    return this;
  }

  public ChainType getType() {
    return type;
  }

  public Chain setType(String value) {
    _type = value;
    return this;
  }

  public Chain setTypeEnum(ChainType type) {
    this.type = type;
    return this;
  }

  public Instant getStartAt() {
    return startAt;
  }

  /**
   Set Chain start-at time, or set it to "now" for the current time.

   @param value "now" for current time, or ISO-8601 timestamp
   @return this Chain (for chaining setters)
   */
  public Chain setStartAt(String value) {
    if (Objects.equals("now", Text.toLowerSlug(value))) {
      startAt = Instant.now();
    } else try {
      startAt = Instant.parse(value);
    } catch (Exception e) {
      startAtError = e.getMessage();
    }
    return this;
  }

  public void setStartAtInstant(Instant startAtInstant) {
    startAt = startAtInstant;
  }

  public Instant getStopAt() {
    return stopAt;
  }

  public Chain setStopAt(String value) {
    if (Objects.nonNull(value) && !value.isEmpty())
      try {
        stopAt = Instant.parse(value);
      } catch (Exception e) {
        stopAtError = e.getMessage();
      }
    return this;
  }

  public void setStopAtInstant(Instant stopAtInstant) {
    stopAt = stopAtInstant;
  }

  @Nullable
  public String getEmbedKey() {
    return embedKey;
  }

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
  public BigInteger getParentId() {
    return accountId;
  }

  @Override
  public void validate() throws CoreException {
    // throws its own CoreException on failure
    if (!Objects.isNull(_state)) {
      state = ChainState.validate(_state);
    }

    // throws its own CoreException on failure
    if (!Objects.isNull(_type)) {
      type = ChainType.validate(_type);
    }

    if (null == accountId) {
      throw new CoreException("Account ID is required.");
    }
    if (null == type || type.toString().isEmpty()) {
      type = ChainType.Preview;
    }
    if (null == state || state.toString().isEmpty()) {
      state = ChainState.Draft;
    }
    if (null == name || name.isEmpty()) {
      throw new CoreException("Name is required.");
    }
    if (ChainType.Production == type) {
      if (null == startAt) {
        throw new CoreException("Start-at is required." + (Objects.nonNull(startAtError) ? (" " + startAtError) : ""));
      }
      if (Objects.nonNull(stopAtError)) {
        throw new CoreException("Stop-at is not isValid. " + stopAtError);
      }
    }
  }

  @Override
  public String toString() {
    return "Chain{" +
      "accountId=" + accountId +
      ", name=" + Text.singleQuoted(name) +
      ", state=" + Text.singleQuoted(String.valueOf(state)) +
      ", type=" + Text.singleQuoted(String.valueOf(type)) +
      ", startAt=" + startAt +
      ", startAtError=" + Text.singleQuoted(startAtError) +
      ", stopAt=" + stopAt +
      ", stopAtError=" + Text.singleQuoted(stopAtError) +
      ", id=" + id +
      ", createdAt=" + createdAt +
      ", updatedAt=" + updatedAt +
      "}";
  }

  /**
   Copy a chain to a revived chain
   set new chain to start now and go until forever

   @return revived of chain
   */
  public Chain revived() throws CoreException {
    validate();
    if (ChainState.Fabricate != state) {
      throw new CoreException("Only a Fabricate-state Chain can be revived.");
    }
    if (ChainType.Production != type) {
      throw new CoreException("Only a Production-type Chain can be revived.");
    }
    Chain copy = new Chain();
    copy.setAccountId(accountId);
    copy.setEmbedKey(embedKey);
    copy.setName(name);
    copy.setStartAtInstant(Instant.now());
    copy.setStateEnum(state);
    copy.setTypeEnum(type);
    return copy;
  }
}
