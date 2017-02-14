// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.tables.Chain.CHAIN;

public class Chain extends Entity {
  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "chain";
  public static final String KEY_MANY = "chains";
  public static final String KEY_START_AT = "startAt";

  /**
   * State-machine states
   */
  public static final String DRAFT = "draft";
  public static final String READY = "ready";
  public static final String PRODUCTION = "production";
  public static final String COMPLETE = "complete";
  // list of all states
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
  public void validate() throws BusinessException {
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
      throw new BusinessException("'" + this.state + "' is not a valid state (" + CSV.join(allStates) + ").");
    }
    if (this.startAt == null) {
      throw new BusinessException("Start-at is required." + (startAtError != null ? " " + startAtError : ""));
    }
    if (this.stopAtError != null) {
      throw new BusinessException("Stop-at is not valid. " + stopAtError);
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN.ACCOUNT_ID, accountId);
    fieldValues.put(CHAIN.NAME, name);
    fieldValues.put(CHAIN.STATE, state);
    fieldValues.put(CHAIN.START_AT, startAt);
    fieldValues.put(CHAIN.STOP_AT, stopAt);
    return fieldValues;
  }

}
