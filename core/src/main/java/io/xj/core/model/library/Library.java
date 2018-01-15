// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.library;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import java.math.BigInteger;
import java.util.Objects;

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
  public static final String KEY_ONE = "library";
  public static final String KEY_MANY = "libraries";
  private String name;
  private BigInteger accountId;

  public Library() {
  }

  public Library(long id) {
    this.id = BigInteger.valueOf(id);
  }

  public Library(BigInteger id) {
    this.id = id;
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
    if (Objects.isNull(name) || name.isEmpty()) {
      throw new BusinessException("Name is required.");
    }
    if (Objects.isNull(accountId)) {
      throw new BusinessException("Account ID is required.");
    }
  }

}
