// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access.impl;

import io.xj.core.CoreModule;
import io.xj.core.model.account.Account;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.user.User;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_role.UserRole;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import com.google.api.client.json.JsonFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class Access {
  public static final String CONTEXT_KEY = "userAccess";
  private static final Logger log = LoggerFactory.getLogger(Access.class);
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static final String KEY_USER_ID = "userId";
  private static final String KEY_USER_AUTH_ID = "userAuthId";
  private static final String KEY_ACCOUNT_IDS = Account.KEY_MANY;
  private static final String KEY_ROLE_TYPES = User.KEY_ROLES;
  private static final UserRoleType[] topLevelRoles = {UserRoleType.Admin, UserRoleType.Internal};
  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  private Collection<UserRoleType> roleTypes;
  private Collection<BigInteger> accountIds;
  private BigInteger userId;
  private BigInteger userAuthId;

  /**
   Construct an Access model from models retrieved from structured data persistence layer

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
  public static Access from(Map<String, String> data) {
    Access result = new Access();


    if (data.containsKey(KEY_USER_ID)) {
      result.setUserId(new BigInteger(data.get(KEY_USER_ID)));
    }
    if (data.containsKey(KEY_USER_AUTH_ID)) {
      result.setUserAuthId(new BigInteger(data.get(KEY_USER_AUTH_ID)));
    }
    if (data.containsKey(KEY_ROLE_TYPES)) {
      result.setRoleTypes(roleTypesFromCSV(data.get(KEY_ROLE_TYPES)));
    }
    if (data.containsKey(KEY_ACCOUNT_IDS)) {
      result.setAccountIds(idsFromCSV(data.get(KEY_ACCOUNT_IDS)));
    }

    return result;
  }

  /**
   Supports the static Access.from(Map) method
   */
  private Access() {
  }

  /**
   Create an access control object from request context

   @param crc container request context
   @return access control
   */
  public static Access fromContext(ContainerRequestContext crc) {
    return (Access) crc.getProperty(CONTEXT_KEY);
  }

  /**
   Create an access control object for an internal worker with top-level access

   @return access control
   */
  public static Access internal() {
    Access result = new Access();
    result.setRoleTypes(Lists.newArrayList(UserRoleType.Internal));
    return result;
  }

  /**
   Determine if user access roles match any of the given resource access roles.

   @param matchRoles of the resource to match.
   @return whether user access roles match resource access roles.
   */
  public boolean isAllowed(UserRoleType... matchRoles) {
    // inefficient?

    for (UserRoleType matchRole : matchRoles) {
      for (UserRoleType userRoleType : roleTypes) {
        if (userRoleType == matchRole) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   Determine if user access roles match any of the given resource access roles.

   @param matchRoles of the resource to match.
   @return whether user access roles match resource access roles.
   */
  public boolean isAllowed(String... matchRoles) {
    // inefficient?

    for (String matchRole : matchRoles) {
      for (UserRoleType userRoleType : roleTypes) {
        if (userRoleType == UserRoleType.valueOf(matchRole)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   Get user ID of this access control

   @return id
   */
  public BigInteger getUserId() {
    return userId;
  }

  /**
   Get Accounts

   @return array of account id
   */
  public Collection<BigInteger> getAccountIds() {
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
  public BigInteger getUserAuthId() {
    return userAuthId;
  }

  /**
   PACKAGE PRIVATE set role types

   @param roleTypes to set
   */
  void setRoleTypes(Iterable<UserRoleType> roleTypes) {
    this.roleTypes = Lists.newArrayList(roleTypes);
  }

  /**
   PACKAGE PRIVATE set account ids

   @param accountIds to set
   */
  void setAccountIds(Iterable<BigInteger> accountIds) {
    this.accountIds = Lists.newLinkedList(accountIds);
  }

  /**
   PACKAGE PRIVATE set user id

   @param userId to set
   */
  void setUserId(BigInteger userId) {
    this.userId = userId;
  }

  /**
   PACKAGE PRIVATE set user auth id

   @param userAuthId to set
   */
  void setUserAuthId(BigInteger userAuthId) {
    this.userAuthId = userAuthId;
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
   */
  public boolean isValid() {
    return
      Objects.nonNull(userId) &&
        Objects.nonNull(userAuthId) &&
        Objects.nonNull(roleTypes) &&
        Objects.nonNull(accountIds);
  }

  /**
   Has access to account id?

   @param accountId to check
   @return true if has access
   */
  public Boolean hasAccount(BigInteger accountId) {
    if (null != accountId) {
      for (BigInteger matchAccountId : accountIds) {
        if (accountId.equals(matchAccountId)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   write a collection of ids to a CSV string

   @param ids to write
   @return CSV of ids
   */
  private static String csvFromIds(Collection<BigInteger> ids) {
    if (Objects.isNull(ids) || ids.isEmpty()) {
      return "";
    }
    Iterator<BigInteger> it = ids.iterator();
    StringBuilder result = new StringBuilder(it.next().toString());
    while (it.hasNext()) {
      result.append(",").append(it.next());
    }
    return result.toString();
  }

  /**
   write a collection of types to a CSV string

   @param types to write
   @return CSV of types
   */
  private static String csvFromRoleTypes(Collection<UserRoleType> types) {
    if (Objects.isNull(types) || types.isEmpty()) {
      return "";
    }
    Iterator<UserRoleType> it = types.iterator();
    StringBuilder result = new StringBuilder(it.next().toString());
    while (it.hasNext()) {
      result.append(",").append(it.next());
    }
    return result.toString();
  }

  /**
   extract collection of role types from collection of user roles

   @param userRoles to get types from
   @return collection of role types
   */
  private static Collection<UserRoleType> roleTypesFromUserRoles(Collection<UserRole> userRoles) {
    Collection<UserRoleType> result = Lists.newArrayList();

    if (Objects.nonNull(userRoles) && !userRoles.isEmpty()) {
      userRoles.forEach((userRole) -> result.add(userRole.getType()));
    }

    return result;
  }

  /**
   extract a collection of ids from a string CSV

   @param csv to parse
   @return collection of ids
   */
  private static Collection<UserRoleType> roleTypesFromCSV(String csv) {
    Collection<UserRoleType> result = Lists.newArrayList();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      CSV.split(csv).forEach((type) -> result.add(UserRoleType.valueOf(Text.toProperSlug(type))));
    }

    return result;
  }

  /**
   extract collection of account ids from collection of account users

   @param accountUsers to get account ids from
   @return collection of account ids
   */
  private static Collection<BigInteger> accountIdsFromAccountUsers(Collection<AccountUser> accountUsers) {
    Collection<BigInteger> result = Lists.newArrayList();

    if (Objects.nonNull(accountUsers) && !accountUsers.isEmpty()) {
      accountUsers.forEach((accountUser) -> result.add(accountUser.getAccountId()));
    }

    return result;
  }

  /**
   extract a collection of ids from a string CSV

   @param csv to parse
   @return collection of ids
   */
  private static Collection<BigInteger> idsFromCSV(String csv) {
    Collection<BigInteger> result = Lists.newArrayList();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      CSV.split(csv).forEach((id) -> result.add(new BigInteger((id))));
    }

    return result;
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
      result.put(KEY_ROLE_TYPES, csvFromRoleTypes(roleTypes));

    if (Objects.nonNull(accountIds))
      result.put(KEY_ACCOUNT_IDS, csvFromIds(accountIds));

    return result;
  }

}
