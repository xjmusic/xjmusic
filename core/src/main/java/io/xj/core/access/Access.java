// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import com.google.api.client.json.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.User;
import io.xj.core.model.UserAuth;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Access {
  public static final String CONTEXT_KEY = "userAccess";
  private static final Logger log = LoggerFactory.getLogger(Access.class);
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static final String KEY_USER_ID = "userId";
  private static final String KEY_USER_AUTH_ID = "userAuthId";
  private static final String KEY_ACCOUNT_IDS = "accounts";
  private static final String KEY_ROLE_TYPES = "roles";
  private static final UserRoleType[] topLevelRoles = {UserRoleType.Admin, UserRoleType.Internal};
  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  private final Collection<UserRoleType> roleTypes;
  private final Collection<UUID> accountIds;
  @Nullable
  private final UUID userId;
  @Nullable
  private final UUID userAuthId;

  /**
   Construct an Access model of models retrieved of structured data persistence layer

   @param userAuth     model
   @param userAccounts models
   @param userRoles    models
   */
  public Access(
    UserAuth userAuth,
    Collection<AccountUser> userAccounts,
    Collection<UserRole> userRoles
  ) {
    userId = userAuth.getUserId();
    userAuthId = userAuth.getId();
    accountIds = accountIdsFromAccountUsers(userAccounts);
    roleTypes = roleTypesFromUserRoles(userRoles);
  }

  /**
   For parsing an incoming message, e.g. stored session in Redis

   @param data to parse
   */
  public Access(Map<String, String> data) {
    if (data.containsKey(KEY_USER_ID))
      userId = UUID.fromString(data.get(KEY_USER_ID));
    else
      userId = null;

    if (data.containsKey(KEY_USER_AUTH_ID))
      userAuthId = UUID.fromString(data.get(KEY_USER_AUTH_ID));
    else
      userAuthId = null;

    if (data.containsKey(KEY_ROLE_TYPES))
      roleTypes = roleTypesFromCSV(data.get(KEY_ROLE_TYPES));
    else
      roleTypes = Lists.newArrayList();

    if (data.containsKey(KEY_ACCOUNT_IDS))
      accountIds = idsFromCSV(data.get(KEY_ACCOUNT_IDS));
    else
      accountIds = Lists.newArrayList();
  }

  /**
   of access with only role types, e.g. top level direct access

   @param userRoleTypes to grant
   */
  public Access(Collection<UserRoleType> userRoleTypes) {
    userId = null;
    userAuthId = null;
    accountIds = Lists.newArrayList();
    roleTypes = Lists.newArrayList(userRoleTypes);
  }

  /**
   Create an access control object of request context
   Mirror of toContext()

   @param crc container request context
   @return access control
   */
  public static Access fromContext(ContainerRequestContext crc) {
    // YES it's a cast to a concrete class. Maybe future extract an interface for `Access` and implement Guice, but that seems monstrous given the 99% use case of the Access class does not trespass here.
    Access access = (Access) crc.getProperty(CONTEXT_KEY);
    if (Objects.nonNull(access)) return access;
    else return unauthenticated();
  }

  /**
   Create an access control object for an internal worker with top-level access

   @return access control
   */
  public static Access internal() {
    return new Access(ImmutableList.of(UserRoleType.Internal));
  }

  /**
   Create an access control object for an unauthenticated access

   @return access control
   */
  public static Access unauthenticated() {
    return new Access(Maps.newHashMap());
  }

  /**
   extract collection of role types of collection of user roles

   @param userRoles to get types of
   @return collection of role types
   */
  private static Collection<UserRoleType> roleTypesFromUserRoles(Collection<UserRole> userRoles) {
    Collection<UserRoleType> result = Lists.newArrayList();

    if (Objects.nonNull(userRoles) && !userRoles.isEmpty()) {
      result = userRoles.stream().map(UserRole::getType).collect(Collectors.toList());
    }

    return result;
  }

  /**
   extract a collection of ids of a string CSV

   @param csv to parse
   @return collection of ids
   */
  private static Collection<UserRoleType> roleTypesFromCSV(String csv) {
    Collection<UserRoleType> result = Lists.newArrayList();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      result = CSV.split(csv).stream().map(type -> UserRoleType.valueOf(Text.toProperSlug(type))).collect(Collectors.toList());
    }

    return result;
  }

  /**
   extract collection of account ids of collection of account users

   @param accountUsers to get account ids of
   @return collection of account ids
   */
  private static Collection<UUID> accountIdsFromAccountUsers(Collection<AccountUser> accountUsers) {
    Collection<UUID> result = Lists.newArrayList();

    if (Objects.nonNull(accountUsers) && !accountUsers.isEmpty()) {
      result = accountUsers.stream().map(AccountUser::getAccountId).collect(Collectors.toList());
    }

    return result;
  }

  /**
   extract a collection of ids of a string CSV

   @param csv to parse
   @return collection of ids
   */
  private static Collection<UUID> idsFromCSV(String csv) {
    Collection<UUID> result = Lists.newArrayList();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      result = CSV.split(csv).stream().map(UUID::fromString).collect(Collectors.toList());
    }

    return result;
  }

  /**
   Create a new Access control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static Access create(User user, UserAuth userAuth, ImmutableList<Account> accounts, String rolesCSV) {
    return new Access(ImmutableMap.of(
      "userAuthId", userAuth.getId().toString(),
      "userId", user.getId().toString(),
      "accounts", CSV.fromIdsOf(accounts),
      "roles", rolesCSV
    ));
  }

  /**
   Create a new Access control object

   @param user     for access
   @param rolesCSV for access
   @return access control object
   */
  public static Access create(User user, String rolesCSV) {
    return new Access(ImmutableMap.of(
      "userId", user.getId().toString(),
      "roles", rolesCSV
    ));
  }

  /**
   Create a new Access control object

   @param user     for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static Access create(User user, ImmutableList<Account> accounts, String rolesCSV) {
    return new Access(ImmutableMap.of(
      "userId", user.getId().toString(),
      "accounts", CSV.fromIdsOf(accounts),
      "roles", rolesCSV
    ));
  }

  /**
   Create a new Access control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @return access control object
   */
  public static Access create(User user, UserAuth userAuth, ImmutableList<Account> accounts) {
    return new Access(ImmutableMap.of(
      "userAuthId", userAuth.getId().toString(),
      "userId", user.getId().toString(),
      "accounts", CSV.fromIdsOf(accounts)
    ));
  }

  /**
   Create a new Access control object

   @param user     for access
   @param accounts for access
   @return access control object
   */
  public static Access create(User user, ImmutableList<Account> accounts) {
    return new Access(ImmutableMap.of(
      "userId", user.getId().toString(),
      "accounts", CSV.fromIdsOf(accounts)
    ));
  }

  /**
   Create a new Access control object

   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static Access create(ImmutableList<Account> accounts, String rolesCSV) {
    return new Access(ImmutableMap.of(
      "accounts", CSV.fromIdsOf(accounts),
      "roles", rolesCSV
    ));
  }

  /**
   Create a new Access control object

   @param rolesCSV for access
   @return access control object
   */
  public static Access create(String rolesCSV) {
    return new Access(ImmutableMap.of(
      "roles", rolesCSV
    ));
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
  public <T> boolean isAllowed(T... matchRoles) {
    // YES there may be heap pollution. FUTURE: better Java programmer figure out better solution :)
    return Arrays.stream(matchRoles).anyMatch(matchRole -> roleTypes.stream().anyMatch(userRoleType -> userRoleType == UserRoleType.valueOf(matchRole.toString())));
  }

  /**
   Get user ID of this access control

   @return id
   */
  public UUID getUserId() throws CoreException {
    if (Objects.isNull(userId)) throw new CoreException("Access has no user");
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
    if (Objects.isNull(roleTypes) || roleTypes.isEmpty()) return false;
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
  public String toJSON() {
    try {
      return jsonFactory.toString(toMap());
    } catch (IOException e) {
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

    if (Objects.nonNull(roleTypes))
      result.put(KEY_ROLE_TYPES, CSV.fromStringsOf(roleTypes));

    if (Objects.nonNull(accountIds))
      result.put(KEY_ACCOUNT_IDS, CSV.fromStringsOf(accountIds));

    return result;
  }
}
