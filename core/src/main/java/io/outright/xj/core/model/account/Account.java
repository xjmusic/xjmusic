// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.account;

import io.outright.xj.core.app.exception.BusinessException;

import org.jooq.Field;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static io.outright.xj.core.Tables.IDEA;
import static io.outright.xj.core.tables.Account.ACCOUNT;

public class Account {

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
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Account name is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(ACCOUNT.NAME, name)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "name:" + this.name +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "account";
  public static final String KEY_MANY = "accounts";

}
