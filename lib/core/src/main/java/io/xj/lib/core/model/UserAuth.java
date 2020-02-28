// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;

import java.util.UUID;

/**
 POJO for persisting data in memory while performing business logic.
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class UserAuth extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("type")
    .add("externalAccessToken")
    .add("externalRefreshToken")
    .add("externalAccount")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(User.class)
    .build();
  private UserAuthType type;
  private String externalAccessToken;
  private String externalRefreshToken;
  private String externalAccount;
  private UUID userId;
  private Exception typeException;

  /**
   Create a new UserAuth@param id@param user               of UserAuth
   */
  public static UserAuth create() {
    return (UserAuth) new UserAuth().setId(UUID.randomUUID());
  }

  /**
   Create a new UserAuth@param id@param user               of UserAuth

   @param user of UserAuth
   @param type of UserAuth
   */
  public static UserAuth create(User user, UserAuthType type) {
    return create()
      .setUserId(user.getId())
      .setType(type.toString());
  }

  /**
   Create a new UserAuth@param id@param user               of UserAuth

   @param user                 of UserAuth
   @param type                 of UserAuth
   @param externalAccessToken  of UserAuth
   @param externalRefreshToken of UserAuth
   @param externalAccount      of UserAuth
   */
  public static UserAuth create(User user, UserAuthType type, String externalAccessToken, String externalRefreshToken, String externalAccount) {
    return create(user, type)
      .setExternalAccessToken(externalAccessToken)
      .setExternalRefreshToken(externalRefreshToken)
      .setExternalAccount(externalAccount);
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  /**
   get ExternalAccessToken

   @return ExternalAccessToken
   */
  public String getExternalAccessToken() {
    return externalAccessToken;
  }

  /**
   get ExternalAccount

   @return ExternalAccount
   */
  public String getExternalAccount() {
    return externalAccount;
  }

  /**
   get ExternalRefreshToken

   @return ExternalRefreshToken
   */
  public String getExternalRefreshToken() {
    return externalRefreshToken;
  }

  @Override
  public UUID getParentId() {
    return userId;
  }

  /**
   get Type

   @return Type
   */
  public UserAuthType getType() {
    return type;
  }

  /**
   get UserId

   @return UserId
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   set ExternalAccessToken

   @param externalAccessToken to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setExternalAccessToken(String externalAccessToken) {
    this.externalAccessToken = externalAccessToken;
    return this;
  }

  /**
   set ExternalAccount

   @param externalAccount to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setExternalAccount(String externalAccount) {
    this.externalAccount = externalAccount;
    return this;
  }

  /**
   set ExternalRefreshToken

   @param externalRefreshToken to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setExternalRefreshToken(String externalRefreshToken) {
    this.externalRefreshToken = externalRefreshToken;
    return this;
  }

  /**
   Set type

   @param type to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setType(String type) {
    try {
      this.type = UserAuthType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   set TypeEnum

   @param type to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setTypeEnum(UserAuthType type) {
    this.type = type;
    return this;
  }

  /**
   set UserId

   @param userId to set
   @return this UserAuth (for chaining methods)
   */
  public UserAuth setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    require(userId, "User ID");

    requireNo(typeException, "Type");
    require(type, "type");
  }
}
