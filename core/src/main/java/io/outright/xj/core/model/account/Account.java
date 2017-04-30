// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.account;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;

import com.google.api.client.util.Maps;

import java.util.Map;

import static io.outright.xj.core.tables.Account.ACCOUNT;

public class Account extends Entity {

  // Name
  private String name;

  public String getName() {
    return name;
  }

  public Account setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Account name is required.");
    }
  }

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ACCOUNT.NAME, name);
    return fieldValues;
  }

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "account";
  public static final String KEY_MANY = "accounts";

}
