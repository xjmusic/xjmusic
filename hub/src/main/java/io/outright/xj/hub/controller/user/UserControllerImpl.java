// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.user;

import io.outright.xj.core.app.access.AccessControlModuleProvider;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.AccountUserRoleRecord;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.util.CSV.CSV;
import io.outright.xj.hub.model.user.EditUser;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.outright.xj.core.Tables.ACCOUNT_USER_ROLE;
import static io.outright.xj.core.Tables.USER;
import static io.outright.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.outright.xj.core.Tables.USER_AUTH;
import static io.outright.xj.core.Tables.USER_ROLE;
import static org.jooq.impl.DSL.groupConcat;

public class UserControllerImpl implements UserController {
  private static Logger log = LoggerFactory.getLogger(UserControllerImpl.class);
  private SQLDatabaseProvider dbProvider;
  private AccessControlModuleProvider accessControlModuleProvider;

  @Inject
  public UserControllerImpl(
    SQLDatabaseProvider dbProvider,
    AccessControlModuleProvider accessControlModuleProvider
  ) {
    this.dbProvider = dbProvider;
    this.accessControlModuleProvider = accessControlModuleProvider;
  }

  @Override
  public String authenticate(String authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws AccessException, ConfigException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    Collection<AccountUserRoleRecord> accounts;
    Collection<UserRoleRecord> roles;
    UserAuthRecord userAuth = fetchOneUserAuth(db, authType, account);
    if (userAuth != null) {
      accounts = fetchAccounts(db, userAuth.getUserId());
      roles = fetchRoles(db, userAuth.getUserId());
    } else {
      try {
        UserRecord user = newUser(db, name, avatarUrl, email);
        accounts = new ArrayList<>();
        roles = newRoles(db, user.getId());
        userAuth = newUserAuth(db, user.getId(), authType, account, externalAccessToken, externalRefreshToken);
      } catch (DatabaseException e) {
        dbProvider.rollbackAndClose(conn);
        throw new AccessException(e);
      }
    }

    String accessToken = accessControlModuleProvider.create(userAuth, accounts, roles);
    try {
      newUserAccessTokenRecord(db,
        userAuth.getUserId(),
        userAuth.getId(),
        accessToken
      );
    } catch (DatabaseException e) {
      throw new AccessException(e);
    }

    try {
      dbProvider.commitAndClose(conn);
    } catch (DatabaseException e) {
      throw new AccessException(e);
    }

    return accessToken;
  }

  @Override
  @Nullable
  public Record fetchUserAndRoles(ULong userId) throws ConfigException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    return db.select(
      USER.ID,
      USER.NAME,
      USER.AVATAR_URL,
      USER.EMAIL,
      USER_ROLE.USER_ID,
      groupConcat(USER_ROLE.TYPE,",").as(Role.KEY_MANY)
    )
      .from(USER_ROLE)
      .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
      .where(USER_ROLE.USER_ID.equal(userId))
      .groupBy(USER_ROLE.USER_ID)
      .fetchOne();
  }

  @Nullable
  public ResultSet fetchUsersAndRoles() throws ConfigException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    return db.select(
      USER.ID,
      USER.NAME,
      USER.AVATAR_URL,
      USER.EMAIL,
      USER_ROLE.USER_ID,
      groupConcat(USER_ROLE.TYPE,",").as(Role.KEY_MANY)
    )
      .from(USER_ROLE)
      .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
      .groupBy(USER_ROLE.USER_ID)
      .fetchResultSet();
  }

  @Override
  public void destroyAllTokens(ULong userId) throws DatabaseException, ConfigException {
    Connection tx = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(tx);

    try {
      destroyAllTokens(db, userId);
      dbProvider.commitAndClose(tx);
    } catch (Exception  e) {
      dbProvider.rollbackAndClose(tx);
      throw e;
    }
  }

  @Override
  public void updateUserRolesAndDestroyTokens(ULong userId, EditUser editUser) throws DatabaseException, ConfigException, BusinessException {
    Connection conn = dbProvider.getConnectionTransaction();
    DSLContext db = dbProvider.getContext(conn);

    try {
      updateUserRoles(db, userId, CSV.split(editUser.getUserRoles()));
      destroyAllTokens(db, userId);
      dbProvider.commitAndClose(conn);
    } catch (Exception e) {
      dbProvider.rollbackAndClose(conn);
      throw e;
    }
  }

  /**
   * Update all roles for a specified User, by providing a list of roles to grant;
   * all other roles will be denied to this User.
   *
   * @param db context.
   * @param userId specific User to update.
   * @param newRoles list to grant; all other roles will be denied to this User.
   */
  private void updateUserRoles(DSLContext db, ULong userId, List<String> newRoles) throws BusinessException {
    String userIdString = String.valueOf(userId);

    // First check all provided roles for validity.
    for (String checkRole: newRoles) {
      if (!Role.isValid(checkRole)) {
        throw new BusinessException("'"+checkRole+"' is not a valid role.");
      }
    }

    // Iterate through all rows; each will produce either an INSERT WHERE NOT EXISTS or a DELETE IF EXISTS.
    for (String role: Role.ALL) {
      if (newRoles.contains(role)) {
        db.execute("INSERT INTO `user_role` (`user_id`, `type`) " +
          "SELECT " + userIdString  + ", \"" + role + "\" FROM dual " +
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
   * Destroy all access tokens for a specified User
   *
   * @param userId to destroy all access tokens for.
   */
  private void destroyAllTokens(DSLContext db, ULong userId) throws DatabaseException {
    Result<UserAccessTokenRecord> userAccessTokens = db.selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.USER_ID.eq(userId))
      .fetch();
    for (UserAccessTokenRecord userAccessToken : userAccessTokens) {
      destroyToken(db, userAccessToken);
    }
  }

  /**
   * Destroy an access token, first in Redis, then (if successful) in SQL.
   * @param db context
   * @param userAccessToken record of user access token to destroy
   */
  private void destroyToken(DSLContext db, UserAccessTokenRecord userAccessToken) throws DatabaseException {
      accessControlModuleProvider.expire(userAccessToken.getAccessToken());
      db.deleteFrom(USER_ACCESS_TOKEN)
        .where(USER_ACCESS_TOKEN.ID.eq(userAccessToken.getId()))
        .execute();
      log.info("Deleted UserAccessToken, id:{}, userId:{}, userAuthId:{}, accessToken:{}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
  }

  /**
   * New UserAccessToken record
   * @param db context
   * @param userId user record id
   * @param userAuthId userAuth record id
   * @param accessToken for user access to this system
   * @return record of newly create UserAccessToken record
   * @throws DatabaseException if anything goes wrong
   */
  private UserAccessTokenRecord newUserAccessTokenRecord(DSLContext db, ULong userId, ULong userAuthId, String accessToken) throws DatabaseException {
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
    if (userAccessToken==null) {
      throw new DatabaseException("Failed to create new UserAccessToken record.");
    }

    log.info("Created new UserAccessToken, id:{}, userId:{}, userAuthId:{}, accessToken:{}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
    return userAccessToken;
  }

  /**
   * Select existing UserAuth by type + account
   *
   * @param db context of authentication request
   * @param authType of external auth
   * @param account identifier in external system
   * @return UserAuthRecord, or null
   */
  private UserAuthRecord fetchOneUserAuth(DSLContext db, String authType, String account) {
    return db.selectFrom(USER_AUTH)
      .where(USER_AUTH.TYPE.equal(authType))
      .and(USER_AUTH.EXTERNAL_ACCOUNT.equal(account))
      .fetchOne();
  }

  /**
   * Select existing Account-User memberships by User id.
   *
   * @param db context of database access.
   * @param userId of existing User.
   * @return collection of AccountUserRoleRecord.
   */
  private Collection<AccountUserRoleRecord> fetchAccounts(DSLContext db, ULong userId) {
    return db.selectFrom(ACCOUNT_USER_ROLE)
      .where(ACCOUNT_USER_ROLE.USER_ID.equal(userId))
      .fetch();
  }

  /**
   * Select existing User roles by User id.
   *
   * @param db context of database access.
   * @param userId of existing User.
   * @return collection of UserRoleRecord.
   */
  private Collection<UserRoleRecord> fetchRoles(DSLContext db, ULong userId) {
    return db.selectFrom(USER_ROLE)
      .where(USER_ROLE.USER_ID.equal(userId))
      .fetch();
  }

  /**
   * If no user_auth exists for this account,
   * create a new user and user_auth record
   * (storing access_token and refresh_token),
   * and return the user
   *
   * @param db context of authentication request
   * @param name to call new user
   * @param avatarUrl to display for new user
   * @param email to contact new user
   * @return new User record, including actual id
   */
  private UserRecord newUser(DSLContext db, String name, String avatarUrl, String email) throws DatabaseException {
    UserRecord user = db.insertInto(USER, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .values(name, avatarUrl, email)
      .returning(USER.ID, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .fetchOne();
    if (user==null) {
      throw new DatabaseException("Failed to create new User record.");
    }

    log.info("Created new User, id:{}, name:{}, email:{}", user.getId(), user.getName(), user.getEmail());
    return user;
  }

  /**
   * Create a new default set of UserRoleRecord for an existing new User id.
   *
   * @param db context of authentication request
   * @param userId of new User.
   * @return collection of new UserRole records, including actual id
   */
  private Collection<UserRoleRecord> newRoles(DSLContext db, ULong userId) throws DatabaseException {
    UserRoleRecord userRole1 = db.insertInto(USER_ROLE, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .values(userId, Role.USER)
      .returning(USER_ROLE.ID, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .fetchOne();
    if (userRole1==null) {
      throw new DatabaseException("Failed to create new UserRole record.");
    }

    log.info("Created new UserRole, id:{}, userId:{}, type:{}", userRole1.getId(), userRole1.getUserId(), userRole1.getType());
    Collection<UserRoleRecord> roles = new ArrayList<>();
    roles.add(userRole1);
    return roles;
  }

  /**
   * If user_auth exists for this account,
   * retrieve its user record and return the user
   *
   * @param db context of authentication request
   * @param userId of User that this auth record belongs to
   * @param authType of external auth
   * @param account identifier in external system
   * @param externalAccessToken for OAuth2 access
   * @param externalRefreshToken for refreshing OAuth2 access
   * @return new UserAuth record, including actual id
   */
  private UserAuthRecord newUserAuth(DSLContext db, ULong userId, String authType, String account, String externalAccessToken, String externalRefreshToken) throws DatabaseException {
    UserAuthRecord userAuth = db.insertInto(USER_AUTH, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .values(userId, authType, account, externalAccessToken, externalRefreshToken)
      .returning(USER_AUTH.ID, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .fetchOne();
    if (userAuth==null) {
      throw new DatabaseException("Failed to create new UserAuth record.");
    }

    log.info("Created new UserAuth, id:{}, userId:{}, type:{}, account:{}", userAuth.getId(), userAuth.getUserId(), userAuth.getType(), userAuth.getExternalAccount());
    return userAuth;
  }

}
