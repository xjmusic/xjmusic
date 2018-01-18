// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.timestamp.TimestampUTC;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.sql.Timestamp;
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
public class Chain extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chain";
  public static final String KEY_MANY = "chains";

  /**
   Fields
   */
  private BigInteger accountId;
  private String name;
  private String _state; // hold value before validation
  private ChainState state;
  private String _type; // hold value before validation
  private ChainType type;
  private Timestamp startAt;
  private String startAtError;
  private Timestamp stopAt;
  private String stopAtError;
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

  public void setStateEnum(ChainState state) {
    this.state = state;
  }

  public ChainType getType() {
    return type;
  }

  public Chain setType(String value) {
    _type = value;
    return this;
  }

  public void setTypeEnum(ChainType type) {
    this.type = type;
  }

  public Timestamp getStartAt() {
    return startAt;
  }

  public Chain setStartAt(String value) {
    try {
      startAt = TimestampUTC.valueOf(value);
    } catch (Exception e) {
      startAtError = e.getMessage();
    }
    return this;
  }

  public void setStartAtTimestamp(Timestamp startAtTimestamp) {
    startAt = startAtTimestamp;
  }

  public Timestamp getStopAt() {
    return stopAt;
  }

  public Chain setStopAt(String value) {
    try {
      stopAt = TimestampUTC.valueOf(value);
    } catch (Exception e) {
      stopAtError = e.getMessage();
    }
    return this;
  }

  public void setStopAtTimestamp(Timestamp stopAtTimestamp) {
    stopAt = stopAtTimestamp;
  }

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
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    state = ChainState.validate(_state);

    // throws its own BusinessException on failure
    type = ChainType.validate(_type);

    if (null == accountId) {
      throw new BusinessException("Account ID is required.");
    }
    if (null == type || type.toString().isEmpty()) {
      throw new BusinessException("Type is required.");
    }
    if (null == name || name.isEmpty()) {
      throw new BusinessException("Name is required.");
    }
    if (Objects.equals(type, ChainType.Production)) {
      if (null == startAt) {
        throw new BusinessException("Start-at is required." + (Objects.nonNull(startAtError) ? (" " + startAtError) : ""));
      }
      if (null != stopAtError) {
        throw new BusinessException("Stop-at is not isValid. " + stopAtError);
      }
    }
  }

  @Override
  public String toString() {
    return "Chain{" +
      "accountId=" + accountId +
      ", name='" + name + '\'' +
      ", state='" + state + '\'' +
      ", type='" + type + '\'' +
      ", startAt=" + startAt +
      ", startAtError='" + startAtError + '\'' +
      ", stopAt=" + stopAt +
      ", stopAtError='" + stopAtError + '\'' +
      ", id=" + id +
      ", createdAt=" + createdAt +
      ", updatedAt=" + updatedAt +
      '}';
  }
}
