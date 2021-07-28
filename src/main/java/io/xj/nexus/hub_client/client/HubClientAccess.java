// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.hub_client.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.UserRole;
import io.xj.lib.entity.Entities;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class HubClientAccess {
  public static final String CONTEXT_KEY = "hub_access";
  private static final UserRole.Type[] topLevelRoles = {UserRole.Type.Admin, UserRole.Type.Internal};
  private final Collection<UserRole.Type> roleTypes = Lists.newArrayList();
  private final Collection<String> accountIds = Lists.newArrayList();
  @Nullable
  private String token = null;
  @Nullable
  private String userId = null;
  @Nullable
  private String userAuthId = null;

  /**
   Construct an HubClientAccess model
   */
  public HubClientAccess() {
  }

  /**
   of access with only role types, e.g. top level direct access

   @param userRoleTypes to grant
   */
  public HubClientAccess(Collection<UserRole.Type> userRoleTypes) {
    roleTypes.addAll(userRoleTypes);
  }

  /**
   Create an access control object of request context
   Mirror of toContext()

   @param crc container request context
   @return access control
   */
  public static HubClientAccess fromContext(ContainerRequestContext crc) {
    HubClientAccess access = (HubClientAccess) crc.getProperty(CONTEXT_KEY);
    if (Objects.nonNull(access)) return access;
    else return unauthenticated();
  }

  /**
   Create an access control object for an internal process with top-level access

   @return access control
   */
  public static HubClientAccess internal() {
    // FUTURE how does Hub plan on authenticating a request made with this "credential?"
    return new HubClientAccess(ImmutableList.of(UserRole.Type.Internal));
  }

  /**
   Create an access control object for an unauthenticated access

   @return access control
   */
  public static HubClientAccess unauthenticated() {
    return new HubClientAccess();
  }

  /**
   Put this access to the container request context.
   Mirror of fromContext()

   @param context to put
   */
  public void toContext(ContainerRequestContext context) {
    context.setProperty(CONTEXT_KEY, this);
  }

  /**
   Check if access is permitted to a specified account

   @param accountId to check for access to
   @return true if access is permitted to the specified account
   */
  public boolean hasAccount(String accountId) {
    return accountIds.contains(accountId);
  }

  /**
   Determine if user access roles match any of the given resource access roles.

   @param matchRoles of the resource to match.
   @return whether user access roles match resource access roles.
   */
  @SafeVarargs
  public final <T> boolean isAllowed(T... matchRoles) {
    return Arrays.stream(matchRoles).anyMatch(matchRole -> roleTypes.stream().anyMatch(userRoleType -> userRoleType == UserRole.Type.valueOf(matchRole.toString())));
  }

  /**
   Get user ID of this access control

   @return id
   */
  public String getUserId() throws HubClientException {
    if (Objects.isNull(userId)) throw new HubClientException("HubAccess has no user");
    return userId;
  }

  /**
   Set User Id

   @param userId to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserId(@Nullable String userId) {
    this.userId = userId;
    return this;
  }

  /**
   Get Accounts

   @return array of account id
   */
  public Collection<String> getAccountIds() {
    return Collections.unmodifiableCollection(accountIds);
  }

  /**
   Set AccountIds

   @param accountIds to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setAccountIds(Collection<String> accountIds) {
    this.accountIds.clear();
    this.accountIds.addAll(accountIds);
    return this;
  }

  /**
   Get user role types

   @return user role types
   */
  public Collection<UserRole.Type> getRoleTypes() {
    return Collections.unmodifiableCollection(roleTypes);
  }

  /**
   Set RoleTypes

   @param roleTypes to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setRoleTypes(Collection<UserRole.Type> roleTypes) {
    this.roleTypes.clear();
    this.roleTypes.addAll(roleTypes);
    return this;
  }

  /**
   Get user account types as a CSV string

   @return user account types
   */
  @JsonIgnore
  public String getAccounts() {
    return Entities.csvOf(accountIds);
  }

  /**
   Set user account types by CSV string

   @param accountsCsv of user account types
   @return user account types
   */
  public HubClientAccess setAccounts(String accountsCsv) {
    setAccountIds(Entities.idsFromCSV(accountsCsv));
    return this;
  }

  /**
   Get user auth id

   @return user auth id
   */
  @Nullable
  public String getUserAuthId() {
    return userAuthId;
  }

  /**
   Set UserAuth Id

   @param userAuthId to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserAuthId(@Nullable String userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }

  /**
   Is Top Level?

   @return boolean
   */
  @JsonIgnore
  public Boolean isTopLevel() {
    return isAllowed(topLevelRoles);
  }

  /**
   Validation
   [#154580129] valid with no accounts, because User expects to login without having access to any accounts.
   */
  @JsonIgnore
  public boolean isValid() {
    if (roleTypes.isEmpty()) return false;
    if (Objects.isNull(userAuthId)) return false;
    return !Objects.isNull(userId);
  }

  /**
   Get the access token string

   @return access token string
   */
  @Nullable
  public String getToken() {
    return token;
  }

  /**
   Set the access token string

   @param token to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setToken(@Nullable String token) {
    this.token = token;
    return this;
  }

}
