// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.newrelic.api.agent.Trace;
import io.xj.AccountUser;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserAuthToken;
import io.xj.UserRole;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlProvider;
import io.xj.service.hub.access.HubAccessException;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import io.xj.service.hub.tables.records.UserAuthRecord;
import io.xj.service.hub.tables.records.UserAuthTokenRecord;
import io.xj.service.hub.tables.records.UserRecord;
import io.xj.service.hub.tables.records.UserRoleRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectSelectStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.service.hub.Tables.ACCOUNT_USER;
import static io.xj.service.hub.Tables.USER;
import static io.xj.service.hub.Tables.USER_AUTH;
import static io.xj.service.hub.Tables.USER_AUTH_TOKEN;
import static io.xj.service.hub.Tables.USER_ROLE;
import static org.jooq.impl.DSL.groupConcatDistinct;

/**
 NOTE: THIS IS AN IRREGULAR D.A.O.
 <p>
 Conceptually, because being a User is a dependency of all other DAOs.
 */
public class UserDAOImpl extends DAOImpl<User> implements UserDAO {
  private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);
  private final HubAccessControlProvider hubAccessControlProvider;

  @Inject
  public UserDAOImpl(
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    HubAccessControlProvider hubAccessControlProvider
  ) {
    super(payloadFactory, entityFactory);
    this.dbProvider = dbProvider;
    this.hubAccessControlProvider = hubAccessControlProvider;
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
      groupConcatDistinct(USER_ROLE.TYPE).separator(",").as("roles")
    );
  }

  /**
   of UserAuthToken record

   @param db          context
   @param userId      user record id
   @param userAuthId  userAuth record id
   @param accessToken for user access to this system
   @throws DAOException if anything goes wrong
   */
  private static void newUserAuthTokenRecord(DSLContext db, String userId, String userAuthId, String accessToken) throws DAOException {
    UserAuthTokenRecord userAccessToken = db.insertInto(USER_AUTH_TOKEN,
      USER_AUTH_TOKEN.USER_ID,
      USER_AUTH_TOKEN.USER_AUTH_ID,
      USER_AUTH_TOKEN.ACCESS_TOKEN
    ).values(
      UUID.fromString(userId),
      UUID.fromString(userAuthId),
      accessToken
    ).returning(
      USER_AUTH_TOKEN.ID,
      USER_AUTH_TOKEN.USER_ID,
      USER_AUTH_TOKEN.USER_AUTH_ID,
      USER_AUTH_TOKEN.ACCESS_TOKEN
    ).fetchOne();
    if (Objects.isNull(userAccessToken)) {
      throw new DAOException("Failed to create new UserAuthToken record.");
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
  private static UserRecord newUser(DSLContext db, String name, String avatarUrl, String email) throws DAOException {
    UserRecord user = db.insertInto(USER, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .values(name, avatarUrl, email)
      .returning(USER.ID, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
      .fetchOne();
    if (Objects.isNull(user)) {
      throw new DAOException("Failed to create new User record.");
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
  private Collection<AccountUser> fetchAccounts(DSLContext db, String userId) throws DAOException {
    return modelsFrom(AccountUser.class, db.selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.USER_ID.equal(UUID.fromString(userId)))
      .fetch());
  }

  /**
   Select existing User roles by User id.

   @param db     context of database access.
   @param userId of existing User.
   @return collection of UserRoleRecord.
   */
  private Collection<UserRole> fetchRoles(DSLContext db, String userId) throws DAOException {
    return modelsFrom(UserRole.class, db.selectFrom(USER_ROLE)
      .where(USER_ROLE.USER_ID.equal(UUID.fromString(userId)))
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
  private UserAuth readOneAuth(DSLContext db, UserAuth.Type authType, String externalAccount) throws DAOException {
    return modelFrom(UserAuth.class, db.selectFrom(USER_AUTH)
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
  private Collection<UserRole> newRoles(DSLContext db, String userId) throws DAOException {
    UserRoleRecord userRole1 = db.insertInto(USER_ROLE, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .values(UUID.fromString(userId), UserRole.Type.User.toString())
      .returning(USER_ROLE.ID, USER_ROLE.USER_ID, USER_ROLE.TYPE)
      .fetchOne();
    if (Objects.isNull(userRole1)) {
      throw new DAOException("Failed to create new UserRole record.");
    }

    log.info("Created new UserRole, id:{}, userId:{}, type:{}", userRole1.getId(), userRole1.getUserId(), userRole1.getType());
    Collection<UserRole> roles = Lists.newArrayList();
    roles.add(modelFrom(UserRole.class, userRole1));
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
  private UserAuth newUserAuth(DSLContext db, String userId, UserAuth.Type authType, String account, String externalAccessToken, String externalRefreshToken) throws DAOException {
    UserAuthRecord userAuth = db.insertInto(USER_AUTH, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .values(UUID.fromString(userId), authType.toString(), account, externalAccessToken, externalRefreshToken)
      .returning(USER_AUTH.ID, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
      .fetchOne();
    if (Objects.isNull(userAuth)) {
      throw new DAOException("Failed to create new UserAuth record.");
    }

    log.info("Created new UserAuth, id:{}, userId:{}, type:{}, account:{}", userAuth.getId(), userAuth.getUserId(), userAuth.getType(), userAuth.getExternalAccount());
    return modelFrom(UserAuth.class, userAuth);
  }

  @Override
  @Trace
  public String authenticate(UserAuth.Type authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    Collection<AccountUser> accounts;
    Collection<UserRole> roles;
    UserAuth userAuth;

    // attempt to find existing user
    try {
      userAuth = readOneAuth(db, authType, account);
      accounts = fetchAccounts(db, userAuth.getUserId());
      roles = fetchRoles(db, userAuth.getUserId());
    }

    // no user exists; create one
    catch (DAOException ignored) {
      try {
        UserRecord user = newUser(db, name, avatarUrl, email);
        accounts = Lists.newArrayList();
        roles = newRoles(db, user.getId().toString());
        userAuth = newUserAuth(db, user.getId().toString(), authType, account, externalAccessToken, externalRefreshToken);
      } catch (Exception e) {
        throw new DAOException("SQL Exception", e);
      }
    }

    try {
      String accessToken = hubAccessControlProvider.create(userAuth, accounts, roles);
      newUserAuthTokenRecord(db,
        userAuth.getUserId(),
        userAuth.getId(),
        accessToken);
      return accessToken;

    } catch (HubAccessException e) {
      throw new DAOException("Cannot authenticate!", e);
    }
  }

  @Override
  @Trace
  public User create(HubAccess hubAccess, User entity) throws DAOException {
    throw new DAOException("Not allowed to create a User record (must implement 'authenticate' method).");

  }

  @Override
  @Trace
  public User readOne(HubAccess hubAccess, String id) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    if (hubAccess.isTopLevel()) {
      return modelFrom(User.class, select(db)
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .where(USER_ROLE.USER_ID.equal(UUID.fromString(id)))
        .groupBy(USER_ROLE.USER_ID, USER.ID)
        .fetchOne());
    } else if (!hubAccess.getAccountIds().isEmpty()) {
      return modelFrom(User.class, select(db)
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(USER_ROLE.USER_ID))
        .where(USER_ROLE.USER_ID.equal(UUID.fromString(id)))
        .and(ACCOUNT_USER.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .groupBy(USER_ROLE.USER_ID, USER.ID)
        .fetchOne());
    } else {
      if (Objects.equals(hubAccess.getUserId(), id)) {
        return modelFrom(User.class, select(db)
          .from(USER_ROLE)
          .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
          .where(USER_ROLE.USER_ID.equal(UUID.fromString(id)))
          .groupBy(USER_ROLE.USER_ID, USER.ID)
          .fetchOne());
      } else {
        throw new DAOException("Not found");
      }
    }
  }

  @Override
  @Trace
  @Nullable
  public Collection<User> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
    DSLContext db = dbProvider.getDSL();
    if (hubAccess.isTopLevel()) {
      return modelsFrom(User.class, select(db)
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .groupBy(USER_ROLE.USER_ID, USER.ID)
        .fetch());

    } else if (!hubAccess.getAccountIds().isEmpty()) {
      return modelsFrom(User.class, select(db)
        .from(USER_ROLE)
        .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
        .join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(USER_ROLE.USER_ID))
        .where(ACCOUNT_USER.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .groupBy(USER.ID)
        .fetch());

    } else return modelsFrom(User.class, select(db)
      .from(USER_ROLE)
      .join(USER).on(USER.ID.eq(USER_ROLE.USER_ID))
      .where(USER_ROLE.USER_ID.eq(UUID.fromString(Objects.requireNonNull(hubAccess.getUserId()))))
      .groupBy(USER.ID)
      .fetch());
  }

  @Override
  @Trace
  public void update(HubAccess access, String id, User entity) throws DAOException {
    try {
      updateUserRolesAndDestroyTokens(access, id, entity);
    } catch (ValueException e) {
      throw new DAOException("Cannot update User record.", e);
    }
  }

  @Override
  @Trace
  public void destroy(HubAccess hubAccess, String id) throws DAOException {
    throw new DAOException("Not allowed to destroy User record.");
  }

  @Override
  @Trace
  public User newInstance() {
    return User.getDefaultInstance();
  }

  @Override
  @Trace
  public UserAuthToken readOneAuthToken(HubAccess hubAccess, String accessToken) throws DAOException {
    requireTopLevel(hubAccess);

    return modelFrom(UserAuthToken.class, dbProvider.getDSL().select(USER_AUTH_TOKEN.fields())
      .from(USER_AUTH_TOKEN)
      .where(USER_AUTH_TOKEN.ACCESS_TOKEN.equal(accessToken))
      .fetchOne());
  }

  @Override
  @Trace
  public UserAuth readOneAuth(HubAccess hubAccess, String userAuthId) throws DAOException {
    requireTopLevel(hubAccess);

    return modelFrom(UserAuth.class, dbProvider.getDSL().select(USER_AUTH.fields())
      .from(USER_AUTH)
      .where(USER_AUTH.ID.equal(UUID.fromString(userAuthId)))
      .fetchOne());
  }

  @Override
  @Trace
  public UserRole readOneRole(HubAccess hubAccess, String userId, UserRole.Type type) throws DAOException {
    requireTopLevel(hubAccess);

    return modelFrom(UserRole.class, dbProvider.getDSL().select(USER_ROLE.fields())
      .from(USER_ROLE)
      .where(USER_ROLE.USER_ID.eq(UUID.fromString(userId)))
      .and(USER_ROLE.TYPE.eq(type.toString()))
      .fetchOne());
  }

  @Override
  @Trace
  public void destroyAllTokens(String userId) throws DAOException {
    destroyAllTokens(dbProvider.getDSL(), userId);
  }

  @Override
  @Trace
  public void updateUserRolesAndDestroyTokens(HubAccess hubAccess, String userId, User entity) throws DAOException, ValueException {
    // FUTURE figure out how to make this all a rollback-able transaction in the new getDataSource() context: dataSource.commit(); and dataSource.setAutoCommit(false);
    requireTopLevel(hubAccess);
    validate(entity);// Prepare key entity
    Collection<String> newRoles = CSV.splitProperSlug(entity.getRoles());

    // First check all provided roles for validity.
    boolean foundValidRole = false;
    for (String checkRole : newRoles)
      try {
        UserRole.Type.valueOf(UserRole.Type.class, checkRole);
        foundValidRole = true;
      } catch (NullPointerException | IllegalArgumentException e) {
        throw new ValueException(e);
      }
    require("Valid Role", foundValidRole);

    DSLContext db = dbProvider.getDSL();

    // Iterate through all possible role types; either delete that role or ensure that it exists, depending on whether its included in the list of updated roles
    for (UserRole.Type type : UserRole.Type.values()) {
      if (newRoles.contains(type.toString())) {
        if (0 >= db.selectCount()
          .from(USER_ROLE)
          .where(USER_ROLE.USER_ID.eq(UUID.fromString(userId)))
          .and(USER_ROLE.TYPE.eq(type.toString()))
          .fetchOne(0, int.class)) {
          UserRoleRecord record = db.newRecord(USER_ROLE);
          record.setType(type.toString());
          record.setUserId(UUID.fromString(userId));
          record.store();
        }
      } else {
        db.deleteFrom(USER_ROLE)
          .where(USER_ROLE.USER_ID.eq(UUID.fromString(userId)))
          .and(USER_ROLE.TYPE.eq(type.toString()))
          .execute();
      }
    }
    destroyAllTokens(db, userId);
  }

  /**
   Destroy all access tokens for a specified User

   @param userId to destroy all access tokens for.
   */
  private void destroyAllTokens(DSLContext db, String userId) throws DAOException {
    Result<UserAuthTokenRecord> userAccessTokens = db.selectFrom(USER_AUTH_TOKEN)
      .where(USER_AUTH_TOKEN.USER_ID.eq(UUID.fromString(userId)))
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
  private void destroyToken(DSLContext db, UserAuthTokenRecord userAccessToken) throws DAOException {
    try {
      hubAccessControlProvider.expire(userAccessToken.getAccessToken());
      db.deleteFrom(USER_AUTH_TOKEN)
        .where(USER_AUTH_TOKEN.ID.eq(userAccessToken.getId()))
        .execute();
      log.info("Deleted UserAuthToken, id:{}, userId:{}, userAuthId:{}, accessToken:{}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
    } catch (HubAccessException e) {
      throw new DAOException("Cannot destroy token!", e);
    }
  }

  /**
   Validate a User record

   @param record to validate
   @throws DAOException if invalid
   */
  public void validate(User record) throws DAOException {
    try {
      Value.require(record.getRoles(), "User roles");

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
