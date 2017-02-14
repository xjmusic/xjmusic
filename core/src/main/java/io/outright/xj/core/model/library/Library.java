// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.library;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.tables.Library.LIBRARY;

public class Library extends Entity {

  // Name
  private String name;

  public String getName() {
    return name;
  }

  public Library setName(String name) {
    this.name = name;
    return this;
  }

  // Account
  private ULong accountId;

  public ULong getAccountId() {
    return accountId;
  }

  public Library setAccountId(BigInteger accountId) {
    this.accountId = ULong.valueOf(accountId);
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LIBRARY.NAME, name);
    fieldValues.put(LIBRARY.ACCOUNT_ID, accountId);
    return fieldValues;
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "library";
  public static final String KEY_MANY = "libraries";

}
