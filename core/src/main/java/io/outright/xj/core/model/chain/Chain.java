// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.tables.Chain.CHAIN;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
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
  public static final String FAILED = "failed";
  // list of all states
  public final static List<String> STATES = ImmutableList.of(
    DRAFT,
    READY,
    FABRICATING,
    COMPLETE,
    FAILED
  );

  /**
   Fields
   */
  private ULong accountId;
  private String name;
  private String state;
  private String type;
  private Timestamp startAt;
  private String startAtError;
  private Timestamp stopAt;
  private String stopAtError;

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

  public ULong getAccountId() {
    return accountId;
  }

  public Chain setAccountId(BigInteger accountId) {
    this.accountId = ULong.valueOf(accountId);
    return this;
  }

  public String getName() {
    return name;
  }

  public Chain setName(String name) {
    this.name = name;
    return this;
  }

  public String getState() {
    return state;
  }

  public Chain setState(String state) {
    this.state = Text.LowerSlug(state);
    return this;
  }

  public String getType() {
    return type;
  }

  public Chain setType(String type) {
    this.type = Text.LowerSlug(type);
    return this;
  }

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

  @Override
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

  @Override
  public Chain setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHAIN.ID);
    accountId = record.get(CHAIN.ACCOUNT_ID);
    name = record.get(CHAIN.NAME);
    type = record.get(CHAIN.TYPE);
    state = record.get(CHAIN.STATE);
    startAt = record.get(CHAIN.START_AT);
    stopAt = record.get(CHAIN.STOP_AT);
    createdAt = record.get(CHAIN.CREATED_AT);
    updatedAt = record.get(CHAIN.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
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
