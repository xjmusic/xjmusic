// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import java.util.Set;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.access.HubAccessTokenGenerator;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.persistence.HubPersistenceException;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.kv.HubKvStoreProvider;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;
import io.xj.hub.tables.records.UserAuthRecord;
import io.xj.hub.tables.records.UserAuthTokenRecord;
import io.xj.hub.tables.records.UserRecord;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.CSV;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectSelectStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.jetbrains.annotations.Nullable;
import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.hub.Tables.ACCOUNT_USER;
import static io.xj.hub.Tables.USER;
import static io.xj.hub.Tables.USER_AUTH;
import static io.xj.hub.Tables.USER_AUTH_TOKEN;

@Service
public class UserManagerImpl extends HubPersistenceServiceImpl implements UserManager {
  static final Logger LOG = LoggerFactory.getLogger(UserManagerImpl.class);
  final HubKvStoreProvider hubKvStoreProvider;
  final HubAccessTokenGenerator hubAccessTokenGenerator;
  final GoogleProvider googleProvider;
  final String sessionNamespace;
  final String tokenName;
  final String tokenDomain;
  final String tokenPath;
  final int tokenMaxAge;
  final Set<String> internalTokens;
  static final Logger log = LoggerFactory.getLogger(UserManagerImpl.class);

  @Autowired
  public UserManagerImpl(
    EntityFactory entityFactory,
    GoogleProvider googleProvider,
    HubAccessTokenGenerator hubAccessTokenGenerator,
    HubSqlStoreProvider sqlStoreProvider,
    HubKvStoreProvider hubKvStoreProvider,
    @Value("${session.namespace}")
    String sessionNamespace,
    @Value("${access.token.name}")
    String tokenName,
    @Value("${access.token.domain}")
    String tokenDomain,
    @Value("${access.token.path}")
    String tokenPath,
    @Value("${access.token.max.age}")
    int tokenMaxAge,
    @Value("${ingest.token.value}")
    String ingestTokenValue
  ) {
    super(entityFactory, sqlStoreProvider);
    this.hubKvStoreProvider = hubKvStoreProvider;
    this.hubAccessTokenGenerator = hubAccessTokenGenerator;
    this.googleProvider = googleProvider;
    this.sessionNamespace = sessionNamespace;
    this.tokenName = tokenName;
    this.tokenDomain = tokenDomain;
    this.tokenPath = tokenPath;
    this.tokenMaxAge = tokenMaxAge;

    internalTokens = Set.of(ingestTokenValue);
  }

  /**
   * This is used to select many User records
   * with a virtual column containing a CSV of its role types
   *
   * @param db context
   * @return jOOQ select step
   */
  static SelectSelectStep<?> selectUser(DSLContext db) {
    return db.select(
      USER.ID,
      USER.NAME,
      USER.AVATAR_URL,
      USER.EMAIL,
      USER.ROLES
    );
  }

  /**
   * of UserAuthToken record
   *
   * @param db          context
   * @param userId      user record id
   * @param userAuthId  userAuth record id
   * @param accessToken for user access to this system
   * @throws ManagerException if anything goes wrong
   */
  static void newUserAuthTokenRecord(DSLContext db, UUID userId, UUID userAuthId, String accessToken) throws ManagerException {
    try (var i = db.insertInto(USER_AUTH_TOKEN,
      USER_AUTH_TOKEN.USER_ID,
      USER_AUTH_TOKEN.USER_AUTH_ID,
      USER_AUTH_TOKEN.ACCESS_TOKEN
    )) {
      UserAuthTokenRecord userAccessToken = i.values(
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
        throw new ManagerException("Failed to create new UserAuthToken record.");
      }

      log.info("Created new userAuthTokenId={}, userId={}, userAuthId={}, accessToken={}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
    }
  }

  /**
   * If no user_auth isNonNull for this account,
   * of a new user and user_auth record
   * (storing access_token and refresh_token),
   * and return the user
   *
   * @param db        context of authentication request
   * @param name      to call new user
   * @param avatarUrl to display for new user
   * @param email     to contact new user
   * @return new User record, including actual id
   */
  User newUser(DSLContext db, String name, String avatarUrl, String email) throws ManagerException {
    try (var i = db.insertInto(USER, USER.NAME, USER.AVATAR_URL, USER.EMAIL)) {
      UserRecord user = i
        .values(name, avatarUrl, email)
        .returning(USER.ID, USER.NAME, USER.AVATAR_URL, USER.EMAIL)
        .fetchOne();
      if (Objects.isNull(user)) {
        throw new ManagerException("Failed to create new User record.");
      }

      log.info("Created new User, id={}, name={}, email={}", user.getId(), user.getName(), user.getEmail());
      return modelFrom(user);
    }
  }

  /**
   * Select existing AccountUser memberships by User id.
   *
   * @param db     context of database access.
   * @param userId of existing User.
   * @return collection of AccountUserRecord.
   */
  Collection<AccountUser> fetchAccounts(DSLContext db, UUID userId) throws ManagerException {
    try (var selectAccountUser = db.selectFrom(ACCOUNT_USER)) {
      return modelsFrom(AccountUser.class, selectAccountUser
        .where(ACCOUNT_USER.USER_ID.equal(userId))
        .fetch());
    }
  }

  /**
   * Select existing user by id
   *
   * @param db context
   * @param id of user
   * @return user
   * @throws ManagerException on failure
   */
  User fetchUser(DSLContext db, UUID id) throws ManagerException {
    try (var selectUser = selectUser(db)) {
      return modelFrom(User.class, selectUser
        .from(USER)
        .where(USER.ID.eq(id))
        .fetchOne());
    }
  }

  /**
   * Select existing UserAuth by type + account
   * <p>
   * NOTE: DON'T REQUIRE ANY ACCESS
   *
   * @param db              context of authentication request
   * @param authType        of external auth
   * @param externalAccount identifier in external system
   * @return UserAuth, or null
   */
  UserAuth readOneAuth(DSLContext db, UserAuthType authType, String externalAccount) throws ManagerException {
    try (var selectUserAuth = db.selectFrom(USER_AUTH)) {
      return modelFrom(UserAuth.class, selectUserAuth
        .where(USER_AUTH.TYPE.equal(authType))
        .and(USER_AUTH.EXTERNAL_ACCOUNT.equal(externalAccount))
        .fetchOne());
    }
  }

  /**
   * If user_auth isNonNull for this account,
   * retrieve its user record and return the user
   *
   * @param db                   context of authentication request
   * @param userId               of User that this auth record belongs to
   * @param authType             of external auth
   * @param account              identifier in external system
   * @param externalAccessToken  for OAuth2 access
   * @param externalRefreshToken for refreshing OAuth2 access
   * @return new UserAuth record, including actual id
   */
  UserAuth newUserAuth(DSLContext db, String userId, UserAuthType authType, String account, String externalAccessToken, String externalRefreshToken) throws ManagerException {
    try (var i = db.insertInto(USER_AUTH, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)) {
      UserAuthRecord userAuth = i
        .values(UUID.fromString(userId), authType, account, externalAccessToken, externalRefreshToken)
        .returning(USER_AUTH.ID, USER_AUTH.USER_ID, USER_AUTH.TYPE, USER_AUTH.EXTERNAL_ACCOUNT, USER_AUTH.EXTERNAL_ACCESS_TOKEN, USER_AUTH.EXTERNAL_REFRESH_TOKEN)
        .fetchOne();
      if (Objects.isNull(userAuth)) {
        throw new ManagerException("Failed to create new UserAuth record.");
      }

      log.info("Created new UserAuth, id={}, userId={}, type={}, account={}", userAuth.getId(), userAuth.getUserId(), userAuth.getType(), userAuth.getExternalAccount());
      return modelFrom(UserAuth.class, userAuth);
    }
  }

  @Override
  public String authenticate(UserAuthType authType, String account, String externalAccessToken, String externalRefreshToken, String name, String avatarUrl, String email) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    Collection<AccountUser> accounts;
    UserAuth userAuth;
    User user;

    // attempt to find existing user
    try {
      userAuth = readOneAuth(db, authType, account);
      user = fetchUser(db, userAuth.getUserId());
      accounts = fetchAccounts(db, userAuth.getUserId());

    } catch (ManagerException ignored) {
      // no user exists; create one
      try {
        user = newUser(db, name, avatarUrl, email);
        accounts = new ArrayList<>();
        userAuth = newUserAuth(db, user.getId().toString(), authType, account, externalAccessToken, externalRefreshToken);
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    }

    try {
      String accessToken = create(user, userAuth, accounts);
      newUserAuthTokenRecord(db,
        userAuth.getUserId(),
        userAuth.getId(),
        accessToken);
      return accessToken;

    } catch (HubAccessException e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public User create(HubAccess access, User entity) throws ManagerException {
    throw new ManagerException("Not allowed to create a User record (must implement 'authenticate' method).");

  }

  @Override
  public User readOne(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    if (access.isTopLevel()) {
      try (var selectUser = selectUser(db)) {
        return modelFrom(User.class, selectUser
          .from(USER)
          .where(USER.ID.eq(id))
          .fetchOne());
      }
    } else if (!access.getAccountIds().isEmpty()) {
      try (var selectUser = db.select(USER.fields());
           var joinAccountUser = selectUser.from(USER).join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(id))) {
        return modelFrom(User.class, joinAccountUser
          .where(USER.ID.eq(id))
          .and(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .limit(1)
          .fetchOne());
      }
    } else {
      if (Objects.equals(access.getUserId(), id)) {
        try (var selectUser = selectUser(db)) {
          return modelFrom(User.class, selectUser
            .from(USER)
            .where(USER.ID.eq(id))
            .fetchOne());
        }
      } else {
        throw new ManagerException("Not found");
      }
    }
  }

  @Override
  @Nullable
  public Collection<User> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    if (access.isTopLevel()) {
      try (var selectUser = selectUser(db)) {
        return modelsFrom(User.class, selectUser
          .from(USER)
          .fetch());
      }

    } else if (!access.getAccountIds().isEmpty()) {
      try (var selectUser = selectUser(db); var o = selectUser
        .from(USER)
        .join(ACCOUNT_USER).on(ACCOUNT_USER.USER_ID.eq(USER.ID))) {
        return modelsFrom(User.class, o
          .where(ACCOUNT_USER.ACCOUNT_ID.in(access.getAccountIds()))
          .groupBy(USER.ID)
          .fetch());
      }
    } else try (var selectUser = selectUser(db)) {
      return modelsFrom(User.class, selectUser
        .from(USER)
        .where(USER.ID.eq(Objects.requireNonNull(access.getUserId())))
        .groupBy(USER.ID)
        .fetch());
    }
  }

  @Override
  public User update(HubAccess access, UUID id, User entity) throws ManagerException {
    // FUTURE figure out how to make this all a rollback-able transaction in the new getDataSource() context: dataSource.commit(); and dataSource.setAutoCommit(false);
    requireTopLevel(access);
    validate(entity);// Prepare key entity
    Collection<String> rawRoles = CSV.splitProperSlug(entity.getRoles());

    // First check all provided roles for validity.
    Set<UserRoleType> validRoles = new HashSet<>();
    for (String checkRole : rawRoles)
      try {
        validRoles.add(UserRoleType.valueOf(checkRole));
      } catch (NullPointerException | IllegalArgumentException e) {
        throw new ManagerException(e);
      }
    requireAny("At least one valid role", !validRoles.isEmpty());
    entity.setRoles(validRoles.stream()
      .sorted(Enum::compareTo)
      .map(UserRoleType::toString)
      .collect(Collectors.joining(", ")));

    // Update the user record, and destroy all tokens
    DSLContext db = sqlStoreProvider.getDSL();
    executeUpdate(db, USER, id, entity);
    destroyAllTokens(db, id);
    return entity;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    throw new ManagerException("Not allowed to destroy User record.");
  }

  @Override
  public User newInstance() {
    return new User();
  }

  @Override
  public UserAuthToken readOneAuthToken(HubAccess access, String accessToken) throws ManagerException {
    requireTopLevel(access);

    try (var selectUserAuthToken = sqlStoreProvider.getDSL().select(USER_AUTH_TOKEN.fields())) {
      return modelFrom(UserAuthToken.class, selectUserAuthToken
        .from(USER_AUTH_TOKEN)
        .where(USER_AUTH_TOKEN.ACCESS_TOKEN.equal(accessToken))
        .fetchOne());
    }
  }

  @Override
  public UserAuth readOneAuth(HubAccess access, UUID userAuthId) throws ManagerException {
    requireTopLevel(access);

    try (var selectUserAuth = sqlStoreProvider.getDSL().select(USER_AUTH.fields())) {
      return modelFrom(UserAuth.class, selectUserAuth
        .from(USER_AUTH)
        .where(USER_AUTH.ID.equal(userAuthId))
        .fetchOne());
    }
  }

  @Override
  public void destroyAllTokens(UUID userId) throws ManagerException {
    destroyAllTokens(sqlStoreProvider.getDSL(), userId);
  }

  /**
   * Destroy all access tokens for a specified User
   *
   * @param userId to destroy all access tokens for.
   */
  void destroyAllTokens(DSLContext db, UUID userId) throws ManagerException {
    try (var selectUserAuthToken = db.selectFrom(USER_AUTH_TOKEN)) {
      Result<UserAuthTokenRecord> userAccessTokens = selectUserAuthToken
        .where(USER_AUTH_TOKEN.USER_ID.eq(userId))
        .fetch();
      for (UserAuthTokenRecord userAccessToken : userAccessTokens) {
        destroyToken(db, userAccessToken);
      }
    }
  }

  /**
   * Destroy an access token
   *
   * @param db              context
   * @param userAccessToken record of user access token to destroy
   */
  void destroyToken(DSLContext db, UserAuthTokenRecord userAccessToken) throws ManagerException {
    try (var d = db.deleteFrom(USER_AUTH_TOKEN)) {
      expire(userAccessToken.getAccessToken());
      d
        .where(USER_AUTH_TOKEN.ID.eq(userAccessToken.getId()))
        .execute();
      log.info("Deleted UserAuthToken, id={}, userId={}, userAuthId={}, accessToken={}", userAccessToken.getId(), userAccessToken.getUserId(), userAccessToken.getUserAuthId(), userAccessToken.getAccessToken());
    } catch (HubAccessException e) {
      throw new ManagerException(e);
    }
  }

  /**
   * Validate a User record
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public void validate(User record) throws ManagerException {
    try {
      ValueUtils.require(record.getRoles(), "User roles");

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public String create(User user, UserAuth userAuth, Collection<AccountUser> accountUsers) throws HubAccessException {
    String accessToken = hubAccessTokenGenerator.generate();
    HubAccess access = HubAccess.create(user, userAuth, accountUsers.stream().map(AccountUser::getAccountId).collect(Collectors.toList()));
    try {
      hubKvStoreProvider.set(computeKey(accessToken), access);
    } catch (HubPersistenceException e) {
      throw new HubAccessException(e);
    }
    return accessToken;
  }


  @Override
  public void expire(String token) throws HubAccessException {
    try {
      hubKvStoreProvider.del(computeKey(token));
    } catch (Exception e) {
      throw new HubAccessException(e);
    }
  }

  @Override
  public HubAccess get(String token) throws HubAccessException {
    if (internalTokens.contains(token)) return HubAccess.internal();


    try {
      HubAccess access = hubKvStoreProvider.get(HubAccess.class, computeKey(token));
      if (Objects.isNull(access)) throw new HubAccessException("Token does not exist!");
      return access;
    } catch (Exception e) {
      throw new HubAccessException(e);
    }
  }

  @Override
  public Cookie newCookie(String accessToken) {
    var cookie = new Cookie(tokenName, accessToken);
    cookie.setMaxAge(tokenMaxAge);
    cookie.setPath(tokenPath);
    cookie.setDomain(tokenDomain);
    return cookie;
  }

  @Override
  public Cookie newExpiredCookie() {
    var cookie = new Cookie(tokenName, "expired");
    cookie.setMaxAge(0);
    cookie.setPath(tokenPath);
    cookie.setDomain(tokenDomain);
    return cookie;
  }

  @Override
  public String authenticate(String accessCode) throws Exception {
    String externalAccessToken;
    String externalRefreshToken;

    try {
      GoogleTokenResponse tokenResponse = googleProvider.getTokenFromCode(accessCode);
      externalAccessToken = tokenResponse.getAccessToken();
      externalRefreshToken = tokenResponse.getRefreshToken();
    } catch (Exception e) {
      LOG.error("Authentication failed: {}", e.getMessage());
      throw new HubAccessException("Authentication failed", e);
    }

    Person person;
    try {
      person = googleProvider.getMe(externalAccessToken);
    } catch (Exception e) {
      LOG.error("Authentication failed: {}", e.getMessage());
      throw new HubAccessException("Authentication failed", e);
    }

    return authenticate(
      UserAuthType.Google,
      person.getId(),
      externalAccessToken,
      externalRefreshToken,
      person.getDisplayName(),
      person.getImage().getUrl(),
      person.getEmails().get(0).getValue()
    );
  }

  @Override
  public String computeKey(String token) {
    return String.format("%s:%s", sessionNamespace, token);
  }
}
