// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.time.Instant;
import java.util.UUID;

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
public class Account extends Entity {
  private String name;

  /**
   Create an Account

   @return new Account
   */
  public static Account create() {
    return (Account) new Account().setId(UUID.randomUUID());
  }

  /**
   Create an Account

   @param name of Account
   @return new Account
   */
  public static Account create(String name) {
    return create()
      .setName(name);
  }

  /**
   Create an Account  with a specified ofd/updated at

   @param name             of Account
   @param createdUpdatedAt of account
   @return new Account
   */
  public static Account create(String name, Instant createdUpdatedAt) {
    return (Account) create(name)
      .setCreatedAtInstant(createdUpdatedAt)
      .setUpdatedAtInstant(createdUpdatedAt);
  }

  /**
   Get Name

   @return Name
   */
  public String getName() {
    return name;
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
  public void validate() throws ValueException {
    Value.require(name, "Account name");
  }
}
