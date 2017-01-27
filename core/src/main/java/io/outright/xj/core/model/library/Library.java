// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.library;

import io.outright.xj.core.app.exception.BusinessException;

import org.jooq.types.ULong;

import java.math.BigInteger;

public class Library {

  // Name
  private String name;
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  // Account
  private ULong accountId;
  public ULong getAccountId() {
    return accountId;
  }
  public void setAccountId(BigInteger accountId) {
    this.accountId = ULong.valueOf(accountId);
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Library name is required.");
    }
    if (this.accountId == null) {
      throw new BusinessException("Account ID is required.");
    }
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
  public static final String KEY_ONE = "library";
  public static final String KEY_MANY = "libraries";

}
