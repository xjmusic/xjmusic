// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.user;

import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.access.UserAccessProvider;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;

import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import static io.outright.xj.core.Tables.ACCOUNT_USER;
import static io.outright.xj.core.Tables.USER;
import static io.outright.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.outright.xj.core.Tables.USER_AUTH;
import static io.outright.xj.core.Tables.USER_ROLE;

public class UserControllerImpl implements UserController {
  private static Logger log = LoggerFactory.getLogger(UserControllerImpl.class);
  private SQLDatabaseProvider dbProvider;
  private UserAccessProvider userAccessProvider;

  @Inject
  public UserControllerImpl(
    SQLDatabaseProvider dbProvider,
    UserAccessProvider userAccessProvider
  ) {
    this.dbProvider = dbProvider;
    this.userAccessProvider = userAccessProvider;
  }

  public String authenticate(String authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws AccessException, ConfigException {
    Connection conn = this.dbProvider.getConnection();
    DSLContext db = DSL.using(conn, SQLDialect.MYSQL);
    Collection<AccountUserRecord> accounts;
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
        try {
          dbProvider.rollback(conn);
        } catch (DatabaseException e1) {
          throw new AccessException(e1);
        }
        throw new AccessException(e);
      }
    }

    String accessToken = userAccessProvider.create(userAuth, accounts, roles);
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
  public UserRecord fetchOneUser(ULong userId) {
    Connection conn;
    try {
      conn = this.dbProvider.getConnection();
    } catch (ConfigException e) {
      log.warn("Database exception", e);
      return null;
    }
    DSLContext db = DSL.using(conn, SQLDialect.MYSQL);
    return db.selectFrom(USER)
      .where(USER.ID.equal(userId))
      .fetchOne();
  }

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
   * @return collection of AccountUserRecord.
   */
  private Collection<AccountUserRecord> fetchAccounts(DSLContext db, ULong userId) {
    return db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.USER_ID.equal(userId))
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
