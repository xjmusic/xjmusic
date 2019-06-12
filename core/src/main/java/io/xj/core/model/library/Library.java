// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.library;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.program.Program;

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
public class Library extends EntityImpl {
  private String name;
  private BigInteger accountId;

  /**
   get AccountId

   @return AccountId
   */
  public BigInteger getAccountId() {
    return accountId;
  }

  /**
   get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  @Override
  public BigInteger getParentId() {
    return accountId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Account.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(Instrument.class)
      .add(Program.class)
      .build();
  }

  /**
   set AccountId

   @param accountId to set
   @return this Library (for chaining methods)
   */
  public Library setAccountId(BigInteger accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   set Name

   @param name to set
   @return this Library (for chaining methods)
   */
  public Library setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Library validate() throws CoreException {
    if (Objects.isNull(name) || name.isEmpty())
      throw new CoreException("Name is required.");

    require(accountId, "Account ID");

    return this;
  }

}
