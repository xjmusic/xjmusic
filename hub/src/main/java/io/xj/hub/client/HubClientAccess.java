// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.xj.hub.enums.UserRoleType;
import io.xj.lib.entity.Entities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HubClientAccess {
  public static final String CONTEXT_KEY = "hub_access";
  static final UserRoleType[] topLevelRoles = {UserRoleType.Admin, UserRoleType.Internal};
  final Collection<UserRoleType> roleTypes = new ArrayList<>();
  final Collection<UUID> accountIds = new ArrayList<>();
  @Nullable
  String token = null;
  @Nullable
  UUID userId = null;
  @Nullable
  UUID userAuthId = null;

  /**
   * Construct an HubClientAccess model
   */
  public HubClientAccess() {
  }

  /**
   * of access with only role types, e.g. top level direct access
   *
   * @param userRoleTypes to grant
   */
  public HubClientAccess(Collection<UserRoleType> userRoleTypes) {
    roleTypes.addAll(userRoleTypes);
  }

  /**
   * Create an access control object for an internal process with top-level access
   *
   * @return access control
   */
  public static HubClientAccess internal() {
    // FUTURE how does Hub plan on authenticating a request made with this "credential?"
    return new HubClientAccess(List.of(UserRoleType.Internal));
  }

  /**
   * Create an access control object for an unauthenticated access
   *
   * @return access control
   */
  public static HubClientAccess unauthenticated() {
    return new HubClientAccess();
  }


  /**
   * Determine if user access roles match any of the given resource access roles.
   *
   * @param matchRoles of the resource to match.
   * @return whether user access roles match resource access roles.
   */
  @SafeVarargs
  public final <T> boolean isAllowed(T... matchRoles) {
    return Arrays.stream(matchRoles).anyMatch(matchRole -> roleTypes.stream().anyMatch(userRoleType -> userRoleType == UserRoleType.valueOf(matchRole.toString())));
  }

  /**
   * Get user ID of this access control
   *
   * @return id
   */
  public UUID getUserId() throws HubClientException {
    if (Objects.isNull(userId)) throw new HubClientException("HubAccess has no user");
    return userId;
  }

  /**
   * Set User Id
   *
   * @param userId to set
   * @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserId(@Nullable UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Get Accounts
   *
   * @return array of account id
   */
  public Collection<UUID> getAccountIds() {
    return Collections.unmodifiableCollection(accountIds);
  }

  /**
   * Set AccountIds
   *
   * @param accountIds to set
   * @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setAccountIds(Collection<UUID> accountIds) {
    this.accountIds.clear();
    this.accountIds.addAll(accountIds);
    return this;
  }

  /**
   * Set RoleTypes
   *
   * @param roleTypes to set
   * @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setRoleTypes(Collection<UserRoleType> roleTypes) {
    this.roleTypes.clear();
    this.roleTypes.addAll(roleTypes);
    return this;
  }

  /**
   * Get user account types as a CSV string
   *
   * @return user account types
   */
  @JsonIgnore
  public String getAccounts() {
    return Entities.csvOf(accountIds);
  }

  /**
   * Set user account types by CSV string
   *
   * @param accountsCsv of user account types
   * @return user account types
   */
  public HubClientAccess setAccounts(String accountsCsv) {
    setAccountIds(Entities.idsFromCSV(accountsCsv));
    return this;
  }

  /**
   * Set UserAuth ID
   *
   * @param userAuthId to set
   * @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setUserAuthId(@Nullable UUID userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }

  /**
   * Is Top Level?
   *
   * @return boolean
   */
  @JsonIgnore
  public Boolean isTopLevel() {
    return isAllowed(topLevelRoles);
  }

  /**
   * Validation
   * valid with no accounts, because User expects to login without having access to any accounts. https://www.pivotaltracker.com/story/show/154580129
   */
  @JsonIgnore
  public boolean isValid() {
    if (roleTypes.isEmpty()) return false;
    if (Objects.isNull(userAuthId)) return false;
    return !Objects.isNull(userId);
  }

  /**
   * Get the access token string
   *
   * @return access token string
   */
  @Nullable
  public String getToken() {
    return token;
  }

  /**
   * Set the access token string
   *
   * @param token to set
   * @return this HubClientAccess (for chaining setters)
   */
  public HubClientAccess setToken(@Nullable String token) {
    this.token = token;
    return this;
  }

}
