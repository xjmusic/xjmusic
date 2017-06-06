// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.library;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.tables.Library.LIBRARY;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Library extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "library";
  public static final String KEY_MANY = "libraries";
  // Name
  private String name;
  // Account
  private ULong accountId;

  public String getName() {
    return name;
  }

  public Library setName(String name) {
    this.name = name;
    return this;
  }

  public ULong getAccountId() {
    return accountId;
  }

  public Library setAccountId(BigInteger accountId) {
    this.accountId = ULong.valueOf(accountId);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
  }

  @Override
  public Library setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(LIBRARY.ID);
    name = record.get(LIBRARY.NAME);
    accountId = record.get(LIBRARY.ACCOUNT_ID);
    createdAt = record.get(LIBRARY.CREATED_AT);
    updatedAt = record.get(LIBRARY.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LIBRARY.NAME, name);
    fieldValues.put(LIBRARY.ACCOUNT_ID, accountId);
    return fieldValues;
  }

}
