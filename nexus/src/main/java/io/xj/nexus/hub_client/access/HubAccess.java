// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.hub_client.access;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.util.CsvUtils;
import io.xj.lib.entity.EntityUtils;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class HubAccess {
  public static final String CONTEXT_KEY = "userAccess";
  static final UserRoleType[] topLevelRoles = {UserRoleType.Admin, UserRoleType.Internal};

  @JsonProperty("roleTypes")
  final Collection<UserRoleType> roleTypes = new ArrayList<>();

  @JsonProperty("accountIds")
  final Collection<UUID> accountIds = new ArrayList<>();

  @Nullable
  @JsonProperty("userId")
  UUID userId;

  @Nullable
  @JsonProperty("userAuthId")
  UUID userAuthId;

  /**
   Create an access control object for an internal process with top-level access

   @return access control
   */
  public static HubAccess internal() {
    return new HubAccess().setRoleTypes(List.of(UserRoleType.Internal));
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
  public static HubAccess create(User user, UserAuth userAuth, List<Account> accounts, String rolesCSV) {
    return new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(EntityUtils.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(User user, List<Account> accounts, String rolesCSV) {
    return new HubAccess()
      .setUserId(user.getId())
      .setAccountIds(EntityUtils.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
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
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @return access control object
   */
  public static HubAccess create(User user, List<Account> accounts) {
    return new HubAccess()
      .setUserId(user.getId())
      .setAccountIds(EntityUtils.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(user.getRoles()));
  }

  /**
   Create a new HubAccess control object

   @param user       for access
   @param userAuth   for access
   @param accountIds for access
   @return access control object
   */
  public static HubAccess create(User user, UserAuth userAuth, List<UUID> accountIds) {
    return new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setRoleTypes(Users.userRoleTypesFromCsv(user.getRoles()))
      .setAccountIds(accountIds);
  }

  /**
   Create a new HubAccess control object

   @param user       for access
   @param userAuthId for access
   @param accounts   for access
   @return access control object
   */
  public static HubAccess create(User user, UUID userAuthId, List<Account> accounts) {
    return new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuthId)
      .setAccountIds(EntityUtils.idsOf(accounts))
      .setRoleTypes(CsvUtils.split(user.getRoles()).stream().map(UserRoleType::valueOf).collect(Collectors.toList()));
  }

  /**
   Create a new HubAccess control object

   @param userId     for access
   @param userAuthId for access
   @param accounts   for access
   @param rolesCSV   for access
   @return access control object
   */
  public static HubAccess create(UUID userId, UUID userAuthId, List<Account> accounts, String rolesCSV) {
    return new HubAccess()
      .setUserId(userId)
      .setUserAuthId(userAuthId)
      .setAccountIds(EntityUtils.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(String rolesCSV) {
    return new HubAccess().setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object with the given user auth, account users, and user roles

   @param userAuth     to use for access control
   @param accountUsers to use for access control
   @param userRoles    to use for access control
   @return new HubAccess
   */
  public static HubAccess create(UserAuth userAuth, Collection<AccountUser> accountUsers, String userRoles) {
    return new HubAccess()
      .setUserId(userAuth.getUserId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(accountUsers.stream()
        .map(AccountUser::getAccountId)
        .collect(Collectors.toList()))
      .setRoleTypes(CsvUtils.split(userRoles).stream()
        .map(UserRoleType::valueOf)
        .collect(Collectors.toList()));
  }

  /**
   Determine if user access roles match any of the given resource access roles.

   @param matchRoles of the resource to match.
   @return whether user access roles match resource access roles.
   */
  @SafeVarargs
  @JsonIgnore
  public final <T> boolean isAnyAllowed(T... matchRoles) {
    return Arrays.stream(matchRoles)
      .anyMatch(matchRole -> roleTypes.stream()
        .anyMatch(userRoleType -> userRoleType == UserRoleType.valueOf(matchRole.toString())));
  }

  /**
   Get user ID of this access control

   @return id
   */
  @Nullable
  public UUID getUserId() {
    return userId;
  }

  /**
   Set User Id

   @param userId to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setUserId(@Nullable UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   Get Accounts

   @return array of account id
   */
  public Collection<UUID> getAccountIds() {
    return Collections.unmodifiableCollection(accountIds);
  }

  /**
   Set AccountIds

   @param accountIds to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setAccountIds(Collection<UUID> accountIds) {
    this.accountIds.clear();
    this.accountIds.addAll(accountIds);
    return this;
  }

  /**
   Get user role types

   @return user role types
   */
  public Collection<UserRoleType> getRoleTypes() {
    return Collections.unmodifiableCollection(roleTypes);
  }

  /**
   Set RoleTypes

   @param roleTypes to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setRoleTypes(Collection<UserRoleType> roleTypes) {
    this.roleTypes.clear();
    this.roleTypes.addAll(roleTypes);
    return this;
  }

  /**
   Is Top Level?

   @return boolean
   */
  @JsonIgnore
  public Boolean isTopLevel() {
    return isAnyAllowed(topLevelRoles);
  }

  /**
   Validation
   valid with no accounts, because User expects to log in without having access to any accounts. https://www.pivotaltracker.com/story/show/154580129
   */
  @JsonIgnore
  public boolean isValid() {
    if (isTopLevel()) return true;
    if (roleTypes.isEmpty()) return false;
    if (Objects.isNull(userAuthId)) return false;
    return !Objects.isNull(userId);
  }

  /**
   Set User auth id

   @param userAuthId to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setUserAuthId(@Nullable UUID userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }

  public UUID getUserAuthId() {
    return userAuthId;
  }
}
