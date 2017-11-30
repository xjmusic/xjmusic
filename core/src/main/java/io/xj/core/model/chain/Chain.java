// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.util.Text;
import io.xj.core.util.timestamp.TimestampUTC;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHAIN;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
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
  private ULong accountId;
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

  public ULong getAccountId() {
    return accountId;
  }

  public Chain setAccountId(BigInteger value) {
    accountId = ULong.valueOf(value);
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

  public ChainType getType() {
    return type;
  }

  public Chain setType(String value) {
    _type = value;
    return this;
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
        throw new BusinessException("Stop-at is not valid. " + stopAtError);
      }
    }
  }


  @Override
  public Chain setFromRecord(Record record) throws BusinessException {
    if (Objects.isNull(record)) {
      // null return is intended here, such that null input "passes through" the method
      return null;
    }
    id = record.get(CHAIN.ID);
    accountId = record.get(CHAIN.ACCOUNT_ID);
    name = record.get(CHAIN.NAME);
    type = ChainType.validate(record.get(CHAIN.TYPE));
    state = ChainState.validate(record.get(CHAIN.STATE));
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
    fieldValues.put(CHAIN.EMBED_KEY, embedKey);
    return fieldValues;
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
