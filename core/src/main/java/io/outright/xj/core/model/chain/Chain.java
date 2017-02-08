// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.util.CSV.CSV;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.tables.Chain.CHAIN;

public class Chain {
  public static final String DRAFT = "draft";
  public static final String READY = "ready";
  public static final String PRODUCTION = "production";
  public static final String COMPLETE = "complete";

  private final static List<String> allStates = ImmutableList.of(
    DRAFT,
    READY,
    PRODUCTION,
    COMPLETE
  );

  // Account
  private ULong accountId;
  public ULong getAccountId() {
    return accountId;
  }
  public Chain setAccountId(BigInteger accountId) {
    this.accountId = ULong.valueOf(accountId);
    return this;
  }

  // Name
  private String name;
  public String getName() {
    return name;
  }
  public Chain setName(String name) {
    this.name = name;
    return this;
  }

  // State
  private String state;
  public String getState() {
    return state;
  }
  public Chain setState(String state) {
    this.state = Purify.LowerSlug(state);
    return this;
  }

  // StartAt
  private Timestamp startAt;
  private String startAtError;
  public Timestamp getStartAt() {
    return startAt;
  }
  public Chain setStartAt(String startAt) {
    try {
      this.startAt = Timestamp.valueOf(startAt);
    } catch (Exception e) {
      startAtError = e.getMessage();
    }
    return this;
  }

  // StopAt
  private Timestamp stopAt;
  private String stopAtError;
  public Timestamp getStopAt() {
    return stopAt;
  }
  public Chain setStopAt(String stopAt) {
    try {
      this.stopAt = Timestamp.valueOf(stopAt);
    } catch (Exception e) {
      stopAtError = e.getMessage();
    }
    return this;
  }


  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.state == null || this.state.length() == 0) {
      throw new BusinessException("State is required.");
    }
    if (!allStates.contains(this.state)) {
      throw new BusinessException("'" + this.state + "' is not a valid state (" + CSV.join(allStates) +").");
    }
    if (this.startAt == null) {
      throw new BusinessException("Start-at is required." + (startAtError != null ? " " + startAtError : ""));
    }
    if (this.stopAt == null) {
      throw new BusinessException("Stop-at is required." + (stopAtError != null ? " " + stopAtError : ""));
    }
  }

  /**
   * Model info jOOQ-field : Value map
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(CHAIN.ACCOUNT_ID, accountId)
      .put(CHAIN.NAME, name)
      .put(CHAIN.STATE, state)
      .put(CHAIN.START_AT, startAt)
      .put(CHAIN.STOP_AT, stopAt)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "accountId:" + this.accountId +
      ", name:" + this.name +
      ", state:" + this.state +
      ", startAt:" + this.startAt +
      ", stopAt:" + this.stopAt +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "chain";
  public static final String KEY_MANY = "chains";

}
