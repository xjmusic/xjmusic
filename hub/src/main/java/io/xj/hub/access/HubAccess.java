// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserRole;
import io.xj.lib.entity.Entities;
import io.xj.hub.dao.UserDAO;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class HubAccess {
  public static final String CONTEXT_KEY = "userAccess";
  private static final UserRole.Type[] topLevelRoles = {UserRole.Type.Admin, UserRole.Type.Internal};

  @JsonProperty("roleTypes")
  private final Collection<UserRole.Type> roleTypes = Lists.newArrayList();

  @JsonProperty("accountIds")
  private final Collection<String> accountIds = Lists.newArrayList();

  @Nullable
  @JsonProperty("userId")
  private String userId;

  @Nullable
  @JsonProperty("userAuthId")
  private String userAuthId;

  /**
   Create an access control object of request context
   Mirror of toContext()

   @param crc container request context
   @return access control
   */
  public static HubAccess fromContext(ContainerRequestContext crc) {
    HubAccess hubAccess = (HubAccess) crc.getProperty(CONTEXT_KEY);
    if (Objects.nonNull(hubAccess)) return hubAccess;
    else return unauthenticated();
  }

  /**
   Create an access control object for an internal process with top-level access

   @return access control
   */
  public static HubAccess internal() {
    return new HubAccess().setRoleTypes(ImmutableList.of(UserRole.Type.Internal));
  }

  /**
   Create an access control object for an unauthenticated access

   @return access control
   */
  public static HubAccess unauthenticated() {
    return new HubAccess();
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(User user, UserAuth userAuth, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(User user, String rolesCSV) {
    return new HubAccess()
      .setUserId(user.getId())
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(User user, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubAccess()
      .setUserId(user.getId())
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @return access control object
   */
  public static HubAccess create(User user, UserAuth userAuth, ImmutableList<Account> accounts) {
    return new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entities.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @return access control object
   */
  public static HubAccess create(User user, ImmutableList<Account> accounts) {
    return new HubAccess()
      .setUserId(user.getId())
      .setAccountIds(Entities.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(ImmutableList<Account> accounts, String rolesCSV) {
    return new HubAccess()
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(String rolesCSV) {
    return new HubAccess().setRoleTypes(UserDAO.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object with the given user auth, account users, and user roles

   @param userAuth     to use for access control
   @param accountUsers to use for access control
   @param userRoles    to use for access control
   @return new HubAccess
   */
  public static HubAccess create(UserAuth userAuth, Collection<AccountUser> accountUsers, Collection<UserRole> userRoles) {
    return new HubAccess()
      .setUserId(userAuth.getUserId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(accountUsers.stream()
        .map(AccountUser::getAccountId)
        .collect(Collectors.toList()))
      .setRoleTypes(userRoles.stream()
        .map(UserRole::getType)
        .collect(Collectors.toList()));
  }

  /**
   Put this access to the container request context.
   Mirror of fromContext()

   @param context to put
   */
  @JsonIgnore
  public void toContext(ContainerRequestContext context) {
    context.setProperty(CONTEXT_KEY, this);
  }

  /**
   Determine if user access roles match any of the given resource access roles.

   @param matchRoles of the resource to match.
   @return whether user access roles match resource access roles.
   */
  @SafeVarargs
  @JsonIgnore
  public final <T> boolean isAllowed(T... matchRoles) {
    return Arrays.stream(matchRoles).anyMatch(matchRole -> roleTypes.stream().anyMatch(userRoleType -> userRoleType == UserRole.Type.valueOf(matchRole.toString())));
  }

  /**
   Get user ID of this access control

   @return id
   */
  @Nullable
  public String getUserId() {
    return userId;
  }

  /**
   Get Accounts

   @return array of account id
   */
  public Collection<String> getAccountIds() {
    return Collections.unmodifiableCollection(accountIds);
  }

  /**
   Get user role types

   @return user role types
   */
  public Collection<UserRole.Type> getRoleTypes() {
    return Collections.unmodifiableCollection(roleTypes);
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
    if (isTopLevel()) return true;
    if (roleTypes.isEmpty()) return false;
    if (Objects.isNull(userAuthId)) return false;
    return !Objects.isNull(userId);
  }

  /**
   Set RoleTypes

   @param roleTypes to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setRoleTypes(Collection<UserRole.Type> roleTypes) {
    this.roleTypes.clear();
    this.roleTypes.addAll(roleTypes);
    return this;
  }

  /**
   Set AccountIds

   @param accountIds to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setAccountIds(Collection<String> accountIds) {
    this.accountIds.clear();
    this.accountIds.addAll(accountIds);
    return this;
  }

  /**
   Set User Id

   @param userId to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setUserId(@Nullable String userId) {
    this.userId = userId;
    return this;
  }

  /**
   Set UserAuth Id

   @param userAuthId to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setUserAuthId(@Nullable String userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }
}
