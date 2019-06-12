// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.library.Library;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
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
public class Account extends EntityImpl {
  private String name;

  /**
   Get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  @Override
  public BigInteger getParentId() {
    return BigInteger.ZERO; // has no parent!
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(Library.class)
      .add(AccountUser.class)
      .add(Chain.class)
      .build();
  }

  /**
   Set Name

   @param name to set
   @return this Account (for chaining methods)
   */
  public Account setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Account validate() throws CoreException {
    require(name, "Account name");
    return this;
  }
}
