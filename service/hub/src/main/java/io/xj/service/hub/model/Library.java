// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Entity;

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
public class Library extends Entity {


  private String name;
  private UUID accountId;

  /**
   Create a new Library

   @return new Library
   */
  public static Library create() {
    return (Library) new Library().setId(UUID.randomUUID());
  }

  /**
   Create a new Library

   @param account of Library
   @param name    of Library
   @param at      created/updated of Library
   @return new Library
   */
  public static Library create(Account account, String name, Instant at) {
    return (Library) create(account, name)
      .setCreatedAtInstant(at)
      .setUpdatedAtInstant(at);
  }

  /**
   Create a new Library

   @param account of Library
   @param name    of Library
   @return new Library
   */
  public static Library create(Account account, String name) {
    return create()
      .setAccountId(account.getId())
      .setName(name);
  }

  /**
   get AccountId

   @return AccountId
   */
  public UUID getAccountId() {
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
  public UUID getParentId() {
    return accountId;
  }

  /**
   set AccountId

   @param accountId to set
   @return this Library (for chaining methods)
   */
  public Library setAccountId(UUID accountId) {
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
  public void validate() throws ValueException {
    Value.require(accountId, "Account ID");

    Value.require(name, "Name");
  }

}
