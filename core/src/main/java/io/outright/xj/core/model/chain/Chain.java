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
   For use in maps.
   */
  public static final String KEY_ONE = "chain";
  public static final String KEY_MANY = "chains";

  /**
   Types
   */
  public final static String PREVIEW = "preview";
  public final static String PRODUCTION = "production";
  // list of all types
  public final static List<String> TYPES = ImmutableList.of(
    PREVIEW,
    PRODUCTION
  );

  /**
   State-machine states
   */
  public static final String DRAFT = "draft";
  public static final String READY = "ready";
  public static final String FABRICATING = "fabricating";
  public static final String COMPLETE = "complete";
  // list of all states
  public final static List<String> STATES = ImmutableList.of(
    DRAFT,
    READY,
    FABRICATING,
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

  // Type
  private String type;

  public String getType() {
    return type;
  }

  public Chain setType(String type) {
    this.type = Purify.LowerSlug(type);
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
      this.startAt = buildTimestampOf(startAt);
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
      this.stopAt = buildTimestampOf(stopAt);
    } catch (Exception e) {
      stopAtError = e.getMessage();
    }
    return this;
  }


  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    validateState(this.state);
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
//    if (this.type.equals(PREVIEW)) {
    // TODO: validate that stopAt - startAt is less than Config.chainPreviewLengthMax()
//    }
    if (this.type.equals(PRODUCTION)) {
      if (this.startAt == null) {
        throw new BusinessException("Start-at is required." + (startAtError != null ? " " + startAtError : ""));
      }
      if (this.stopAtError != null) {
        throw new BusinessException("Stop-at is not valid. " + stopAtError);
      }
    }
  }

  /**
   Validate a state

   @param state to validate
   @throws BusinessException if invalid
   */
  public static void validateState(String state) throws BusinessException {
    if (state == null || state.length() == 0) {
      throw new BusinessException("State is required.");
    }
    if (!STATES.contains(state)) {
      throw new BusinessException("'" + state + "' is not a valid state (" + CSV.join(STATES) + ").");
    }
  }

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN.ACCOUNT_ID, accountId);
    fieldValues.put(CHAIN.NAME, name);
    fieldValues.put(CHAIN.TYPE, type);
    fieldValues.put(CHAIN.STATE, state);
    fieldValues.put(CHAIN.START_AT, startAt);
    fieldValues.put(CHAIN.STOP_AT, stopAt);
    return fieldValues;
  }

}
