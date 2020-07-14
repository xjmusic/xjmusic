// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.lib.entity.Entity;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.entity.User;
import io.xj.service.hub.entity.UserAuth;
import io.xj.service.hub.entity.UserRoleType;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.*;

public class HubClientAccess {
  public static final String CONTEXT_KEY = "hub_access";
  private static final UserRoleType[] topLevelRoles = {UserRoleType.Admin, UserRoleType.Internal};
  private final Collection<UserRoleType> roleTypes = Lists.newArrayList();
  private final Collection<UUID> accountIds = Lists.newArrayList();
  @Nullable
  private String token = null;
  @Nullable
  private UUID userId = null;
  @Nullable
  private UUID userAuthId = null;

  /**
   Construct an HubClientAccess model
   */
  public HubClientAccess() {
  }

  /**
   of access with only role types, e.g. top level direct access

   @param userRoleTypes to grant
   */
  public HubClientAccess(Collection<UserRoleType> userRoleTypes) {
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
    // TODO how does Hub plan on authenticating a request made with this "credential?"
    return new HubClientAccess(ImmutableList.of(UserRoleType.Internal));
  }

  /**
   Create an access control object for an unauthenticated access

   @return access control
   */
  public static HubClientAccess unauthenticated() {
    return new HubClientAccess();
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess create(User user, UserAuth userAuth, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entity.idsOf(accounts))
      .setRoleTypes(UserRoleType.fromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess create(User user, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setRoleTypes(UserRoleType.fromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess create(User user, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(Entity.idsOf(accounts))
      .setRoleTypes(UserRoleType.fromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess create(User user, UserAuth userAuth, ImmutableList<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entity.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess create(User user, ImmutableList<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(Entity.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess create(ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setAccountIds(Entity.idsOf(accounts))
      .setRoleTypes(UserRoleType.fromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess create(String rolesCSV) {
    return new HubClientAccess().setRoleTypes(UserRoleType.fromCsv(rolesCSV));
  }

  /**
   Set RoleTypes

   @param roleTypes to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setRoleTypes(Collection<UserRoleType> roleTypes) {
    this.roleTypes.clear();
    this.roleTypes.addAll(roleTypes);
    return this;
  }

  /**
   Set AccountIds

   @param accountIds to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setAccountIds(Collection<UUID> accountIds) {
    this.accountIds.clear();
    this.accountIds.addAll(accountIds);
    return this;
  }

  /**
   Set User Id

   @param userId to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserId(@Nullable UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   Set UserAuth Id

   @param userAuthId to set
   @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserAuthId(@Nullable UUID userAuthId) {
    this.userAuthId = userAuthId;
    return this;
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
  public boolean hasAccount(UUID accountId) {
    return accountIds.contains(accountId);
  }

  /**
   Determine if user access roles match any of the given resource access roles.

   @param matchRoles of the resource to match.
   @return whether user access roles match resource access roles.
   */
  @SafeVarargs
  public final <T> boolean isAllowed(T... matchRoles) {
    return Arrays.stream(matchRoles).anyMatch(matchRole -> roleTypes.stream().anyMatch(userRoleType -> userRoleType == UserRoleType.valueOf(matchRole.toString())));
  }

  /**
   Get user ID of this access control

   @return id
   */
  public UUID getUserId() throws HubClientException {
    if (Objects.isNull(userId)) throw new HubClientException("HubAccess has no user");
    return userId;
  }

  /**
   Get Accounts

   @return array of account id
   */
  public Collection<UUID> getAccountIds() {
    return Collections.unmodifiableCollection(accountIds);
  }

  /**
   Get user role types

   @return user role types
   */
  public Collection<UserRoleType> getRoleTypes() {
    return Collections.unmodifiableCollection(roleTypes);
  }

  /**
   Get user role types as a CSV string

   @return user role types
   */
  public String getRoles() {
    return UserRoleType.csvOf(roleTypes);
  }

  /**
   Set user role types by CSV string

   @param rolesCsv of user role types
   @return user role types
   */
  public HubClientAccess setRoles(String rolesCsv) {
    setRoleTypes(UserRoleType.fromCsv(rolesCsv));
    return this;
  }

  /**
   Get user account types as a CSV string

   @return user account types
   */
  @JsonIgnore
  public String getAccounts() {
    return Entity.csvOf(accountIds);
  }

  /**
   Set user account types by CSV string

   @param accountsCsv of user account types
   @return user account types
   */
  public HubClientAccess setAccounts(String accountsCsv) {
    setAccountIds(Entity.idsFromCSV(accountsCsv));
    return this;
  }

  /**
   Get user auth id

   @return user auth id
   */
  @Nullable
  public UUID getUserAuthId() {
    return userAuthId;
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
