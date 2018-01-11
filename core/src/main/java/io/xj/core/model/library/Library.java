// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.library;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
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
  private BigInteger accountId;

  public Library() {}

  public Library(long id) {
    this.id = BigInteger.valueOf(id);
  }

  public String getName() {
    return name;
  }

  public Library setName(String name) {
    this.name = name;
    return this;
  }

  public BigInteger getAccountId() {
    return accountId;
  }

  public Library setAccountId(BigInteger accountId) {
    this.accountId = accountId;
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

}
