// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControlProvider;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.UserDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.UserWrapper;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.outright.xj.core.Tables.ACCOUNT_USER;
import static io.outright.xj.core.Tables.USER;
import static io.outright.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.outright.xj.core.Tables.USER_AUTH;
import static io.outright.xj.core.Tables.USER_ROLE;
import static org.jooq.impl.DSL.groupConcat;

/**
 NOTE: THIS IS AN IRREGULAR D.A.O.
 <p>
 Conceptually, because being a User is a dependency of all other DAOs.
 */
public class UserDAOImpl extends DAOImpl implements UserDAO {
  private static Logger log = LoggerFactory.getLogger(UserDAOImpl.class);
  private AccessControlProvider accessControlProvider;

  @Inject
  public UserDAOImpl(
    SQLDatabaseProvider dbProvider,
    AccessControlProvider accessControlProvider
  ) {
    this.dbProvider = dbProvider;
    this.accessControlProvider = accessControlProvider;
  }

  @Override
  public String authenticate(String authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws AccessException, DatabaseException {
    SQLConnection tx = dbProvider.getConnection(true);

    Collection<AccountUserRecord> accounts;
    Collection<UserRoleRecord> roles;
    UserAuthRecord userAuth = fetchOneUserAuth(tx.getContext(), authType, account);
    if (userAuth != null) {
      accounts = fetchAccounts(tx.getContext(), userAuth.getUserId());
      roles = fetchRoles(tx.getContext(), userAuth.getUserId());
    } else {
      try {
        UserRecord user = newUser(tx.getContext(), name, avatarUrl, email);
        accounts = new ArrayList<>();
        roles = newRoles(tx.getContext(), user.getId());
        userAuth = newUserAuth(tx.getContext(), user.getId(), authType, account, externalAccessToken, externalRefreshToken);
      } catch (Exception e) {
        throw tx.failure(new AccessException(e));
      }
    }

    String accessToken = accessControlProvider.create(userAuth, accounts, roles);
    try {
      newUserAccessTokenRecord(tx.getContext(),
        userAuth.getUserId(),
        userAuth.getId(),
        accessToken
      );
    } catch (Exception e) {
      throw tx.failure(new AccessException(e));
    }

    try {
      return tx.success(accessToken);
    } catch (DatabaseException e) {
      throw tx.failure(new AccessException(e));
    }
  }

  @Override
  public JSONObject readOne(AccessControl access, ULong userId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, userId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  public JSONArray readAll(AccessControl access) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroyAllTokens(ULong userId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      destroyAllTokens(tx.getContext(), userId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateUserRolesAndDestroyTokens(AccessControl access, ULong userId, UserWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();

    try {
      requireTopLevel(access);
      updateUserRoles(tx.getContext(), userId, data);
      destroyAllTokens(tx.getContext(), userId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   New UserAccessToken record

   @param db          context
   @param userId      user record id
   @param userAuthId  userAuth record id
   @param accessToken for user access to this system
   @return record of newly create UserAccessToken record
   @throws Exception if anything goes wrong
   */
  private UserAccessTokenRecord newUserAccessTokenRecord(DSLContext db, ULong userId, ULong userAuthId, String accessToken) throws Exception {
    UserAccessTokenRecord userAccessToken = db.insertInto(USER_ACCESS_TOKEN,
      USER_ACCESS_TOKEN.USER_ID,
      USER_ACCESS_TOKEN.USER_AUTH_ID,
      USER_ACCESS_TOKEN.ACCESS_TOKEN
    ).values(
      userId,
      userAuthId,
      accessToken
    ).returning(
      USER_ACCESS_TOKEN.ID,
      USER_ACCESS_TOKEN.USER_ID,
      USER_ACCESS_TOKEN.USER_AUTH_ID,
      USER_ACCESS_TOKEN.ACCESS_TOKEN
    ).fetchOne();
    if (userAccessToken == null) {
      throw new DatabaseException("Failed to create new UserAccessToken record.");
    }

    log.info("Created new UserAccessToken, id:{}, userId:{}, userAuthId:{}, accessToken:{}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
    return userAccessToken;
  }

  /**
   If no user_auth exists for this account,
   create a new user and user_auth record
   (storing access_token and refresh_token),
   and return the user

   @param db        context of authentication request
   @param name      to call new user
   @param avatarUrl to display for new user
   @param email     to contact new user
   @return new User record, including actual id
   */
  private UserRecord newUser(DSLContext db, String name, String avatarUrl, String email) throws Exception {
    UserRecord user = db.insertInto(USER, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .values(name, avatarUrl, email)
      .returning(USER.ID, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .fetchOne();
    if (user == null) {
      throw new DatabaseException("Failed to create new User record.");
    }

    log.info("Created new User, id:{}, name:{}, email:{}", user.getId(), user.getName(), user.getEmail());
    return user;
  }

  /**
   Create a new default set of UserRoleRecord for an existing new User id.

   @param db     context of authentication request
   @param userId of new User.
   @return collection of new UserRole records, including actual id
   */
  private Collection<UserRoleRecord> newRoles(DSLContext db, ULong userId) throws Exception {
    UserRoleRecord userRole1 = db.insertInto(USER_ROLE, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .values(userId, Role.USER)
      .returning(USER_ROLE.ID, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .fetchOne();
    if (userRole1 == null) {
      throw new DatabaseException("Failed to create new UserRole record.");
    }

    log.info("Created new UserRole, id:{}, userId:{}, type:{}", userRole1.getId(), userRole1.getUserId(), userRole1.getType());
    Collection<UserRoleRecord> roles = new ArrayList<>();
    roles.add(userRole1);
    return roles;
  }

  /**
   If user_auth exists for this account,
   retrieve its user record and return the user

   @param db                   context of authentication request
   @param userId               of User that this auth record belongs to
   @param authType             of external auth
   @param account              identifier in external system
   @param externalAccessToken  for OAuth2 access
   @param externalRefreshToken for refreshing OAuth2 access
   @return new UserAuth record, including actual id
   */
  private UserAuthRecord newUserAuth(DSLContext db, ULong userId, String authType, String account, String externalAccessToken, String externalRefreshToken) throws Exception {
    UserAuthRecord userAuth = db.insertInto(USER_AUTH, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .values(userId, authType, account, externalAccessToken, externalRefreshToken)
      .returning(USER_AUTH.ID, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .fetchOne();
    if (userAuth == null) {
      throw new DatabaseException("Failed to create new UserAuth record.");
    }

    log.info("Created new UserAuth, id:{}, userId:{}, type:{}, account:{}", userAuth.getId(), userAuth.getUserId(), userAuth.getType(), userAuth.getExternalAccount());
    return userAuth;
  }

  /**
   Select existing UserAuth by type + account

   @param db       context of authentication request
   @param authType of external auth
   @param account  identifier in external system
   @return UserAuthRecord, or null
   */
  private UserAuthRecord fetchOneUserAuth(DSLContext db, String authType, String account) {
    return db.selectFrom(USER_AUTH)
      .where(USER_AUTH.TYPE.equal(authType))
      .and(USER_AUTH.EXTERNAL_ACCOUNT.equal(account))
      .fetchOne();
  }

  /**
   Select existing Account-User memberships by User id.

   @param db     context of database access.
   @param userId of existing User.
   @return collection of AccountUserRecord.
   */
  private Collection<AccountUserRecord> fetchAccounts(DSLContext db, ULong userId) {
    return db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.USER_ID.equal(userId))
      .fetch();
  }

  /**
   Select existing User roles by User id.

   @param db     context of database access.
   @param userId of existing User.
   @return collection of UserRoleRecord.
   */
  private Collection<UserRoleRecord> fetchRoles(DSLContext db, ULong userId) {
    return db.selectFrom(USER_ROLE)
      .where(USER_ROLE.USER_ID.equal(userId))
      .fetch();
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param userId to read
   @return record
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong userId) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.select(
        USER.ID,
        USER.NAME,
        USER.AVATAR_URL,
        USER.EMAIL,
        USER_ROLE.USER_ID,
        groupConcat(USER_ROLE.TYPE, ",").as(Role.KEY_MANY)
      )
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .where(USER_ROLE.USER_ID.equal(userId))
        .groupBy(USER_ROLE.USER_ID)
        .fetchOne());
    } else if (access.getAccounts().length > 0) {
      return JSON.objectFromRecord(db.select(
        USER.ID,
        USER.NAME,
        USER.AVATAR_URL,
        USER.EMAIL,
        USER_ROLE.USER_ID,
        groupConcat(USER_ROLE.TYPE, ",").as(Role.KEY_MANY)
      )
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(USER_ROLE.USER_ID))
        .where(USER_ROLE.USER_ID.equal(userId))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
        .groupBy(USER_ROLE.USER_ID)
        .fetchOne());
    } else if (access.getUserId().equals(userId)) {
      return JSON.objectFromRecord(db.select(
        USER.ID,
        USER.NAME,
        USER.AVATAR_URL,
        USER.EMAIL,
        USER_ROLE.USER_ID,
        groupConcat(USER_ROLE.TYPE, ",").as(Role.KEY_MANY)
      )
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .where(USER_ROLE.USER_ID.equal(userId))
        .groupBy(USER_ROLE.USER_ID)
        .fetchOne());
    } else {
      return null;
    }
  }

  /**
   Read all records

   @param db     context
   @param access control
   @return array of records
   @throws SQLException on database failure
   */
  private JSONArray readAll(DSLContext db, AccessControl access) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(
        USER.ID,
        USER.NAME,
        USER.AVATAR_URL,
        USER.EMAIL,
        USER_ROLE.USER_ID,
        groupConcat(USER_ROLE.TYPE, ",").as(Role.KEY_MANY)
      )
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .groupBy(USER_ROLE.USER_ID)
        .fetchResultSet());
    } else if (access.getAccounts().length > 0) {
      return JSON.arrayFromResultSet(db.select(
        USER.ID,
        USER.NAME,
        USER.AVATAR_URL,
        USER.EMAIL,
        USER_ROLE.USER_ID,
        groupConcat(USER_ROLE.TYPE, ",").as(Role.KEY_MANY)
      )
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(USER_ROLE.USER_ID))
        .where(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccounts()))
        .groupBy(USER.ID)
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(
        USER.ID,
        USER.NAME,
        USER.AVATAR_URL,
        USER.EMAIL,
        USER_ROLE.USER_ID,
        groupConcat(USER_ROLE.TYPE, ",").as(Role.KEY_MANY)
      )
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .where(USER_ROLE.USER_ID.eq(access.getUserId()))
        .groupBy(USER.ID)
        .fetchResultSet());
    }
  }

  /**
   Update all roles for a specified User, by providing a list of roles to grant;
   all other roles will be denied to this User.

   @param db     context.
   @param userId specific User to update.
   */
  private void updateUserRoles(DSLContext db, ULong userId, UserWrapper data) throws BusinessException {
    data.validate();

    // Prepare key data
    String userIdString = String.valueOf(userId);
    List<String> newRoles = CSV.split(data.getUser().getRoles());

    // First check all provided roles for validity.
    for (String checkRole : newRoles) {
      if (!Role.isValid(checkRole)) {
        throw new BusinessException("'" + checkRole + "' is not a valid role. Try one of: " + Role.TYPES_CSV);
      }
    }

    // Iterate through all rows; each will produce either an INSERT WHERE NOT EXISTS or a DELETE IF EXISTS.
    for (String role : Role.TYPES) {
      if (newRoles.contains(role)) {
        db.execute("INSERT INTO `user_role` (`user_id`, `type`) " +
          "SELECT " + userIdString + ", \"" + role + "\" FROM dual " +
          "WHERE NOT EXISTS (" +
          "SELECT `id` FROM `user_role` " +
          "WHERE `user_id`=" + userIdString + " " +
          "AND `type`=\"" + role + "\"" +
          ");");
      } else {
        db.deleteFrom(USER_ROLE)
          .where(USER_ROLE.USER_ID.eq(userId))
          .and(USER_ROLE.TYPE.eq(role))
          .execute();
      }
    }
  }

  /**
   Destroy all access tokens for a specified User

   @param userId to destroy all access tokens for.
   */
  private void destroyAllTokens(DSLContext db, ULong userId) throws Exception {
    Result<UserAccessTokenRecord> userAccessTokens = db.selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.USER_ID.eq(userId))
      .fetch();
    for (UserAccessTokenRecord userAccessToken : userAccessTokens) {
      destroyToken(db, userAccessToken);
    }
  }

  /**
   Destroy an access token, first in Redis, then (if successful) in SQL.

   @param db              context
   @param userAccessToken record of user access token to destroy
   */
  private void destroyToken(DSLContext db, UserAccessTokenRecord userAccessToken) throws Exception {
    accessControlProvider.expire(userAccessToken.getAccessToken());
    db.deleteFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.ID.eq(userAccessToken.getId()))
      .execute();
    log.info("Deleted UserAccessToken, id:{}, userId:{}, userAuthId:{}, accessToken:{}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
  }
}
