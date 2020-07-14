// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.api.client.json.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.lib.entity.Entity;
import io.xj.lib.util.CSV;
import io.xj.service.hub.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.*;

public class HubAccess {
  public static final String CONTEXT_KEY = "userAccess";
  private static final Logger log = LoggerFactory.getLogger(HubAccess.class);
  private static final String KEY_USER_ID = "userId";
  private static final String KEY_USER_AUTH_ID = "userAuthId";
  private static final String KEY_ACCOUNT_IDS = "accounts";
  private static final String KEY_ROLE_TYPES = "roles";
  private static final UserRoleType[] topLevelRoles = {UserRoleType.Admin, UserRoleType.Internal};
  private final Collection<UserRoleType> roleTypes = Lists.newArrayList();
  private final Collection<UUID> accountIds = Lists.newArrayList();

  @Nullable
  private UUID userId;

  @Nullable
  private UUID userAuthId;

  /**
   Construct an empty HubAccess model of models retrieved of structured data persistence layer
   */
  public HubAccess() {
  }

  /**
   For parsing an incoming message, e.g. stored session in Redis

   @param data to parse
   */
  public HubAccess(Map<String, String> data) {
    if (data.containsKey(KEY_USER_ID))
      userId = UUID.fromString(data.get(KEY_USER_ID));
    else
      userId = null;

    if (data.containsKey(KEY_USER_AUTH_ID))
      userAuthId = UUID.fromString(data.get(KEY_USER_AUTH_ID));
    else
      userAuthId = null;

    if (data.containsKey(KEY_ROLE_TYPES))
      setRoleTypes(UserRoleType.fromCsv(data.get(KEY_ROLE_TYPES)));
    else
      setRoleTypes(Lists.newArrayList());

    if (data.containsKey(KEY_ACCOUNT_IDS))
      setAccountIds(Entity.idsFromCSV(data.get(KEY_ACCOUNT_IDS)));
    else
      setAccountIds(Lists.newArrayList());
  }

  /**
   of access with only role types, e.g. top level direct access

   @param userRoleTypes to grant
   */
  public HubAccess(Collection<UserRoleType> userRoleTypes) {
    userId = null;
    userAuthId = null;
    setAccountIds(Lists.newArrayList());
    setRoleTypes(Lists.newArrayList(userRoleTypes));
  }

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
    return new HubAccess(ImmutableList.of(UserRoleType.Internal));
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
      .setAccountIds(Entity.idsOf(accounts))
      .setRoleTypes(UserRoleType.fromCsv(rolesCSV));
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
      .setRoleTypes(UserRoleType.fromCsv(rolesCSV));
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
  public static HubAccess create(User user, UserAuth userAuth, ImmutableList<Account> accounts) {
    return new HubAccess()
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
  public static HubAccess create(User user, ImmutableList<Account> accounts) {
    return new HubAccess()
      .setUserId(user.getId())
      .setAccountIds(Entity.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(ImmutableList<Account> accounts, String rolesCSV) {
    return new HubAccess()
      .setAccountIds(Entity.idsOf(accounts))
      .setRoleTypes(UserRoleType.fromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param rolesCSV for access
   @return access control object
   */
  public static HubAccess create(String rolesCSV) {
    return new HubAccess().setRoleTypes(UserRoleType.fromCsv(rolesCSV));
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
      .setAccountIds(AccountUser.accountIdsFromAccountUsers(accountUsers))
      .setRoleTypes(UserRole.typesOf(userRoles));
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
  public UUID getUserId() throws HubAccessException {
    if (Objects.isNull(userId)) throw new HubAccessException("HubAccess has no user");
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
  public Boolean isTopLevel() {
    return isAllowed(topLevelRoles);
  }

  /**
   Validation
   [#154580129] valid with no accounts, because User expects to login without having access to any accounts.
   */
  public boolean isValid() {
    if (isTopLevel()) return true;
    if (roleTypes.isEmpty()) return false;
    if (Objects.isNull(userAuthId)) return false;
    return !Objects.isNull(userId);
  }

  /**
   Has access to account id?

   @param accountId to check
   @return true if has access
   */
  public Boolean hasAccount(UUID accountId) {
    if (null != accountId) {
      return accountIds.stream().anyMatch(matchAccountId -> Objects.equals(accountId, matchAccountId));
    }
    return false;
  }

  /**
   Get a representation of this access control

   @return JSON
   */
  public String toJSON(JsonFactory jsonFactory) {
    try {
      return jsonFactory.toString(toMap());
    } catch (Exception e) {
      log.error("failed JSON serialization", e);
      return "{}";
    }
  }

  /**
   Inner map
   */
  public Map<String, String> toMap() {
    Map<String, String> result = Maps.newHashMap();

    if (Objects.nonNull(userId))
      result.put(KEY_USER_ID, userId.toString());

    if (Objects.nonNull(userAuthId))
      result.put(KEY_USER_AUTH_ID, userAuthId.toString());

    result.put(KEY_ROLE_TYPES, CSV.fromStringsOf(roleTypes));

    result.put(KEY_ACCOUNT_IDS, CSV.fromStringsOf(accountIds));

    return result;
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
   Set User Id

   @param userId to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setUserId(@Nullable UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   Set UserAuth Id

   @param userAuthId to set
   @return this HubAccess (for chaining setters)
   */
  public HubAccess setUserAuthId(@Nullable UUID userAuthId) {
    this.userAuthId = userAuthId;
    return this;
  }
}
