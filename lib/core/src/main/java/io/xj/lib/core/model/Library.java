// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;

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
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("name")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Account.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(Instrument.class)
    .add(Program.class)
    .build();
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

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return RESOURCE_HAS_MANY;
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
  public void validate() throws CoreException {
    require(accountId, "Account ID");

    require(name, "Name");
  }

}
