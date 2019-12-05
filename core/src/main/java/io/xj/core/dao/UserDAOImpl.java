// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.exception.CoreException;
import io.xj.core.model.AccountUser;
import io.xj.core.model.User;
import io.xj.core.model.UserAuth;
import io.xj.core.model.UserAuthToken;
import io.xj.core.model.UserAuthType;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.tables.records.UserAuthRecord;
import io.xj.core.tables.records.UserAuthTokenRecord;
import io.xj.core.tables.records.UserRecord;
import io.xj.core.tables.records.UserRoleRecord;
import io.xj.core.util.CSV;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectSelectStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.core.Tables.ACCOUNT_USER;
import static io.xj.core.Tables.USER;
import static io.xj.core.Tables.USER_AUTH;
import static io.xj.core.Tables.USER_AUTH_TOKEN;
import static io.xj.core.Tables.USER_ROLE;
import static org.jooq.impl.DSL.groupConcat;

/**
 NOTE: THIS IS AN IRREGULAR D.A.O.
 <p>
 Conceptually, because being a User is a dependency of all other DAOs.
 */
public class UserDAOImpl extends DAOImpl<User> implements UserDAO {
  private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

  private final AccessControlProvider accessControlProvider;

  @Inject
  public UserDAOImpl(
    SQLDatabaseProvider dbProvider,
    AccessControlProvider accessControlProvider
  ) {
    this.dbProvider = dbProvider;
    this.accessControlProvider = accessControlProvider;
  }

  /**
   This is used to select many User records
   with a virtual column containing a CSV of its role types

   @param db context
   @return jOOQ select step
   */
  private static SelectSelectStep<?> select(DSLContext db) {
    return db.select(
      USER.ID,
      USER.NAME,
      USER.AVATAR_URL,
      USER.EMAIL,
      USER.CREATED_AT,
      USER.UPDATED_AT,
      groupConcat(USER_ROLE.TYPE, ",").as("roles")
    );
  }

  /**
   of UserAuthToken record

   @param db          context
   @param userId      user record id
   @param userAuthId  userAuth record id
   @param accessToken for user access to this system
   @throws CoreException if anything goes wrong
   */
  private static void newUserAuthTokenRecord(DSLContext db, UUID userId, UUID userAuthId, String accessToken) throws CoreException {
    UserAuthTokenRecord userAccessToken = db.insertInto(USER_AUTH_TOKEN,
      USER_AUTH_TOKEN.USER_ID,
      USER_AUTH_TOKEN.USER_AUTH_ID,
      USER_AUTH_TOKEN.ACCESS_TOKEN
    ).values(
      userId,
      userAuthId,
      accessToken
    ).returning(
      USER_AUTH_TOKEN.ID,
      USER_AUTH_TOKEN.USER_ID,
      USER_AUTH_TOKEN.USER_AUTH_ID,
      USER_AUTH_TOKEN.ACCESS_TOKEN
    ).fetchOne();
    if (Objects.isNull(userAccessToken)) {
      throw new CoreException("Failed to create new UserAuthToken record.");
    }

    log.info("Created new UserAuthToken, id:{}, userId:{}, userAuthId:{}, accessToken:{}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
  }

  /**
   If no user_auth isNonNull for this account,
   of a new user and user_auth record
   (storing access_token and refresh_token),
   and return the user

   @param db        context of authentication request
   @param name      to call new user
   @param avatarUrl to display for new user
   @param email     to contact new user
   @return new User record, including actual id
   */
  private static UserRecord newUser(DSLContext db, String name, String avatarUrl, String email) throws CoreException {
    UserRecord user = db.insertInto(USER, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .values(name, avatarUrl, email)
      .returning(USER.ID, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .fetchOne();
    if (Objects.isNull(user)) {
      throw new CoreException("Failed to create new User record.");
    }

    log.info("Created new User, id:{}, name:{}, email:{}", user.getId(), user.getName(), user.getEmail());
    return user;
  }

  /**
   Select existing AccountUser memberships by User id.

   @param db     context of database access.
   @param userId of existing User.
   @return collection of AccountUserRecord.
   */
  private static Collection<AccountUser> fetchAccounts(DSLContext db, UUID userId) throws CoreException {
    return DAORecord.modelsFrom(AccountUser.class, db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.USER_ID.equal(userId))
      .fetch());
  }

  /**
   Select existing User roles by User id.

   @param db     context of database access.
   @param userId of existing User.
   @return collection of UserRoleRecord.
   */
  private static Collection<UserRole> fetchRoles(DSLContext db, UUID userId) throws CoreException {
    return DAORecord.modelsFrom(UserRole.class, db.selectFrom(USER_ROLE)
      .where(USER_ROLE.USER_ID.equal(userId))
      .fetch());
  }

  /**
   Select existing UserAuth by type + account
   <p>
   NOTE: DON'T REQUIRE ANY ACCESS

   @param db              context of authentication request
   @param authType        of external auth
   @param externalAccount identifier in external system
   @return UserAuth, or null
   */
  private static UserAuth readOneAuth(DSLContext db, UserAuthType authType, String externalAccount) throws CoreException {
    return DAORecord.modelFrom(UserAuth.class, db.selectFrom(USER_AUTH)
      .where(USER_AUTH.TYPE.equal(authType.toString()))
      .and(USER_AUTH.EXTERNAL_ACCOUNT.equal(externalAccount))
      .fetchOne());
  }

  /**
   Create a new default set of UserRoleRecord for an existing new User id.

   @param db     context of authentication request
   @param userId of new User.
   @return collection of new UserRole records, including actual id
   */
  private Collection<UserRole> newRoles(DSLContext db, UUID userId) throws CoreException {
    UserRoleRecord userRole1 = db.insertInto(USER_ROLE, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .values(userId, UserRoleType.User.toString())
      .returning(USER_ROLE.ID, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .fetchOne();
    if (Objects.isNull(userRole1)) {
      throw new CoreException("Failed to create new UserRole record.");
    }

    log.info("Created new UserRole, id:{}, userId:{}, type:{}", userRole1.getId(), userRole1.getUserId(), userRole1.getType());
    Collection<UserRole> roles = Lists.newArrayList();
    roles.add(DAORecord.modelFrom(UserRole.class, userRole1));
    return roles;
  }

  /**
   If user_auth isNonNull for this account,
   retrieve its user record and return the user

   @param db                   context of authentication request
   @param userId               of User that this auth record belongs to
   @param authType             of external auth
   @param account              identifier in external system
   @param externalAccessToken  for OAuth2 access
   @param externalRefreshToken for refreshing OAuth2 access
   @return new UserAuth record, including actual id
   */
  private UserAuth newUserAuth(DSLContext db, UUID userId, UserAuthType authType, String account, String externalAccessToken, String externalRefreshToken) throws CoreException {
    UserAuthRecord userAuth = db.insertInto(USER_AUTH, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .values(userId, authType.toString(), account, externalAccessToken, externalRefreshToken)
      .returning(USER_AUTH.ID, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .fetchOne();
    if (Objects.isNull(userAuth)) {
      throw new CoreException("Failed to create new UserAuth record.");
    }

    log.info("Created new UserAuth, id:{}, userId:{}, type:{}, account:{}", userAuth.getId(), userAuth.getUserId(), userAuth.getType(), userAuth.getExternalAccount());
    return DAORecord.modelFrom(UserAuth.class, userAuth);
  }

  @Override
  public String authenticate(UserAuthType authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);
      Collection<AccountUser> accounts;
      Collection<UserRole> roles;
      UserAuth userAuth;
      try {
        userAuth = readOneAuth(db, authType, account);
        accounts = fetchAccounts(db, userAuth.getUserId());
        roles = fetchRoles(db, userAuth.getUserId());
      } catch (CoreException ignored) {
        try {
          UserRecord user = newUser(db, name, avatarUrl, email);
          accounts = Lists.newArrayList();
          roles = newRoles(db, user.getId());
          userAuth = newUserAuth(db, user.getId(), authType, account, externalAccessToken, externalRefreshToken);
        } catch (Exception e) {
          throw new CoreException("SQL Exception", e);
        }
      }

      String accessToken = accessControlProvider.create(userAuth, accounts, roles);
      newUserAuthTokenRecord(db,
        userAuth.getUserId(),
        userAuth.getId(),
        accessToken
      );
      return accessToken;

    } catch (Exception e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public User create(Access access, User entity) throws CoreException {
    throw new CoreException("Not allowed to create a User record (must implement 'authenticate' method).");

  }

  @Override
  public User readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel()) {
        return DAORecord.modelFrom(User.class, select(DAORecord.DSL(connection))
          .from(USER_ROLE)
          .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
          .where(USER_ROLE.USER_ID.equal(id))
          .groupBy(USER_ROLE.USER_ID, USER.ID)
          .fetchOne());
      } else if (!access.getAccountIds().isEmpty()) {
        return DAORecord.modelFrom(User.class, select(DAORecord.DSL(connection))
          .from(USER_ROLE)
          .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
          .join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(USER_ROLE.USER_ID))
          .where(USER_ROLE.USER_ID.equal(id))
          .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .groupBy(USER_ROLE.USER_ID, USER.ID)
          .fetchOne());
      } else if (Objects.equals(access.getUserId(), id)) {
        return DAORecord.modelFrom(User.class, select(DAORecord.DSL(connection))
          .from(USER_ROLE)
          .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
          .where(USER_ROLE.USER_ID.equal(id))
          .groupBy(USER_ROLE.USER_ID, USER.ID)
          .fetchOne());
      } else {
        throw new CoreException("Not found");
      }
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  @Nullable
  public Collection<User> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel()) {
        return DAORecord.modelsFrom(User.class, select(DAORecord.DSL(connection))
          .from(USER_ROLE)
          .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
          .groupBy(USER_ROLE.USER_ID, USER.ID)
          .fetch());
      } else if (!access.getAccountIds().isEmpty()) {
        return DAORecord.modelsFrom(User.class, select(DAORecord.DSL(connection))
          .from(USER_ROLE)
          .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
          .join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(USER_ROLE.USER_ID))
          .where(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .groupBy(USER.ID)
          .fetch());
      } else {
        return DAORecord.modelsFrom(User.class, select(DAORecord.DSL(connection))
          .from(USER_ROLE)
          .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
          .where(USER_ROLE.USER_ID.eq(access.getUserId()))
          .groupBy(USER.ID)
          .fetch());
      }
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, User entity) throws CoreException {
    throw new CoreException("Not allowed to update User record.");
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    throw new CoreException("Not allowed to destroy User record.");
  }

  @Override
  public User newInstance() {
    return new User();
  }

  @Override
  public UserAuthToken readOneAuthToken(Access access, String accessToken) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);

      return DAORecord.modelFrom(UserAuthToken.class, DAORecord.DSL(connection).select(USER_AUTH_TOKEN.fields())
        .from(USER_AUTH_TOKEN)
        .where(USER_AUTH_TOKEN.ACCESS_TOKEN.equal(accessToken))
        .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public UserAuth readOneAuth(Access access, UUID userAuthId) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);

      return DAORecord.modelFrom(UserAuth.class, DAORecord.DSL(connection).select(USER_AUTH.fields())
        .from(USER_AUTH)
        .where(USER_AUTH.ID.equal(userAuthId))
        .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public UserRole readOneRole(Access access, UUID userId, UserRoleType type) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);

      return DAORecord.modelFrom(UserRole.class, DAORecord.DSL(connection).select(USER_ROLE.fields())
        .from(USER_ROLE)
        .where(USER_ROLE.USER_ID.eq(userId))
        .and(USER_ROLE.TYPE.eq(type.toString()))
        .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroyAllTokens(UUID userId) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      destroyAllTokens(DAORecord.DSL(connection), userId);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void updateUserRolesAndDestroyTokens(Access access, UUID userId, User entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      connection.setAutoCommit(false);
      requireTopLevel(access);
      entity.validate();

      // Prepare key entity
      Collection<String> newRoles = CSV.splitProperSlug(entity.getRoles());

      // First check all provided roles for validity.
      boolean foundValidRole = false;
      for (String checkRole : newRoles) {
        UserRoleType.validate(checkRole);
        foundValidRole = true;
      }
      require("Valid Role", foundValidRole);

      DSLContext db = DAORecord.DSL(connection);

      // Iterate through all possible role types; either delete that role or ensure that it exists, depending on whether its included in the list of updated roles
      for (UserRoleType type : UserRoleType.values()) {
        if (newRoles.contains(type.toString())) {
          if ( 0 >= db.selectCount()
            .from(USER_ROLE)
            .where(USER_ROLE.USER_ID.eq(userId))
            .and(USER_ROLE.TYPE.eq(type.toString()))
            .fetchOne(0, int.class)) {
            UserRoleRecord record = db.newRecord(USER_ROLE);
            record.setType(type.toString());
            record.setUserId(userId);
            record.store();
          }
        } else {
          db.deleteFrom(USER_ROLE)
            .where(USER_ROLE.USER_ID.eq(userId))
            .and(USER_ROLE.TYPE.eq(type.toString()))
            .execute();
        }
      }
      destroyAllTokens(db, userId);
      connection.commit();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  /**
   Destroy all access tokens for a specified User

   @param userId to destroy all access tokens for.
   */
  private void destroyAllTokens(DSLContext db, UUID userId) throws CoreException {
    Result<UserAuthTokenRecord> userAccessTokens = db.selectFrom(USER_AUTH_TOKEN)
      .where(USER_AUTH_TOKEN.USER_ID.eq(userId))
      .fetch();
    for (UserAuthTokenRecord userAccessToken : userAccessTokens) {
      destroyToken(db, userAccessToken);
    }
  }

  /**
   Destroy an access token, first in Redis, then (if successful) in SQL.

   @param db              context
   @param userAccessToken record of user access token to destroy
   */
  private void destroyToken(DSLContext db, UserAuthTokenRecord userAccessToken) throws CoreException {
    accessControlProvider.expire(userAccessToken.getAccessToken());
    db.deleteFrom(USER_AUTH_TOKEN)
      .where(USER_AUTH_TOKEN.ID.eq(userAccessToken.getId()))
      .execute();
    log.info("Deleted UserAuthToken, id:{}, userId:{}, userAuthId:{}, accessToken:{}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
  }

}
