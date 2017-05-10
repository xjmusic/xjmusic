// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.external.AuthType;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.User;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.outright.xj.core.tables.User.USER;
import static io.outright.xj.core.tables.UserAccessToken.USER_ACCESS_TOKEN;
import static io.outright.xj.core.tables.UserAuth.USER_AUTH;
import static io.outright.xj.core.tables.UserRole.USER_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UserIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private UserDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.USER);
    IntegrationTestEntity.insertUserRole(2, 2, Role.ADMIN);
    IntegrationTestEntity.insertAccountUser(3, 1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, AuthType.GOOGLE, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(5, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(6, 1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(7, 4, Role.USER);

    // Instantiate the test subject
    testDAO = injector.getInstance(UserDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void authenticate_NewUser() throws Exception {
    String accessToken = testDAO.authenticate(
      AuthType.GOOGLE,
      "88888",
      "accesstoken123",
      "refreshtoken456",
      "wayne",
      "http://pictures.com/wayne.gif",
      "wayne@email.com"
    );

    // Created User Access Token
    assertNotNull(accessToken);
    // TODO: assert token stored in Redis with correct auth
    UserAccessTokenRecord userAccessToken = IntegrationTestService.getDb()
      .selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.ACCESS_TOKEN.eq(accessToken))
      .fetchOne();
    assertNotNull(userAccessToken);
    // Created User Auth
    UserAuthRecord userAuth = IntegrationTestService.getDb()
      .selectFrom(USER_AUTH)
      .where(USER_AUTH.ID.eq(userAccessToken.getUserAuthId()))
      .fetchOne();
    assertNotNull(userAuth);
    assertEquals(AuthType.GOOGLE, userAuth.getType());
    assertEquals("88888", userAuth.getExternalAccount());
    assertEquals("accesstoken123", userAuth.getExternalAccessToken());
    assertEquals("refreshtoken456", userAuth.getExternalRefreshToken());
    // Created User
    UserRecord user = IntegrationTestService.getDb()
      .selectFrom(USER)
      .where(USER.ID.eq(userAccessToken.getUserId()))
      .fetchOne();
    assertNotNull(user);
    assertEquals("wayne", user.getName());
    assertEquals("http://pictures.com/wayne.gif", user.getAvatarUrl());
    assertEquals("wayne@email.com", user.getEmail());
  }

  @Test
  public void authenticate_ExistingUser() throws Exception {
    String accessToken = testDAO.authenticate(
      AuthType.GOOGLE,
      "22222",
      "accesstoken123",
      "refreshtoken456",
      "john wayne",
      "http://pictures.com/john.gif",
      "john@email.com"
    );

    // Created User Access Token
    assertNotNull(accessToken);
    // TODO: assert token stored in Redis with correct auth
    UserAccessTokenRecord userAccessToken = IntegrationTestService.getDb()
      .selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.ACCESS_TOKEN.eq(accessToken))
      .fetchOne();
    assertNotNull(userAccessToken);
    // Created User Auth
    UserAuthRecord userAuth = IntegrationTestService.getDb()
      .selectFrom(USER_AUTH)
      .where(USER_AUTH.ID.eq(userAccessToken.getUserAuthId()))
      .fetchOne();
    assertNotNull(userAuth);
    assertEquals(AuthType.GOOGLE, userAuth.getType());
    assertEquals("22222", userAuth.getExternalAccount());
    assertEquals("external_access_token_123", userAuth.getExternalAccessToken());
    assertEquals("external_refresh_token_123", userAuth.getExternalRefreshToken());
    // Created User
    UserRecord user = IntegrationTestService.getDb()
      .selectFrom(USER)
      .where(USER.ID.eq(userAccessToken.getUserId()))
      .fetchOne();
    assertNotNull(user);
    assertEquals(ULong.valueOf(2), user.getId());
    assertEquals("john", user.getName());
    assertEquals("http://pictures.com/john.gif", user.getAvatarUrl());
    assertEquals("john@email.com", user.getEmail());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    User result = new User().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("user,admin", result.getRoles());
  }

  @Test
  public void readOne_toJSONObject() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    User result = new User().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));
    assertNotNull(result);
    JSONObject resultJSON = result.toJSONObject();

    assertNotNull(resultJSON);
    assertEquals(ULong.valueOf(2), resultJSON.get("id"));
    assertEquals("john@email.com", resultJSON.get("email"));
    assertEquals("http://pictures.com/john.gif", resultJSON.get("avatarUrl"));
    assertEquals("john", resultJSON.get("name"));
    assertEquals("user,admin", resultJSON.get("roles"));
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    User result = new User().setFromRecord(testDAO.readOne(access, ULong.valueOf(3)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(3), result.getId());
    assertEquals("jenny@email.com", result.getEmail());
    assertEquals("http://pictures.com/jenny.gif", result.getAvatarUrl());
    assertEquals("jenny", result.getName());
    assertEquals("user", result.getRoles());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    User result = new User().setFromRecord(testDAO.readOne(access, ULong.valueOf(4)));

    assertNull(result);
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill has no account membership
      "roles", "user",
      "accounts", ""
    ));

    User result = new User().setFromRecord(testDAO.readOne(access, ULong.valueOf(4)));

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user,admin",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access));

    assertNotNull(result);
    assertEquals(3, result.length());
  }

  @Test
  public void readAll_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access));

    assertNotNull(result);
    assertEquals(2, result.length());
  }

  @Test
  public void readAll_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill is in no accounts
      "roles", "user",
      "accounts", ""
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access));

    assertNotNull(result);
    assertEquals(1, result.length());
    JSONObject resultSub = (JSONObject) result.get(0);
    assertEquals("bill", resultSub.get("name"));
  }

  @Test
  public void destroyAllTokens() throws Exception {
    testDAO.destroyAllTokens(ULong.valueOf(2));

    UserAccessTokenRecord result = IntegrationTestService.getDb()
      .selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.ACCESS_TOKEN.eq("this-is-my-actual-access-token"))
      .fetchOne();
    assertNull(result);
    // TODO: assert token destroyed in Redis
  }

  @Test
  public void updateUserRolesAndDestroyTokens() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    User inputData = new User()
      .setRoles("user,artist");

    testDAO.updateUserRolesAndDestroyTokens(access, ULong.valueOf(2), inputData);

    // Access Token deleted
    UserAccessTokenRecord result = IntegrationTestService.getDb()
      .selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.ACCESS_TOKEN.eq("this-is-my-actual-access-token"))
      .fetchOne();
    assertNull(result);
    // TODO: assert token destroyed in Redis
    // Added artist role
    UserRoleRecord addedRole = IntegrationTestService.getDb()
      .selectFrom(USER_ROLE)
      .where(USER_ROLE.USER_ID.eq(ULong.valueOf(2)))
      .and(USER_ROLE.TYPE.eq(Role.ARTIST))
      .fetchOne();
    assertNotNull(addedRole);
    // Removed admin role
    UserRoleRecord removedRole = IntegrationTestService.getDb()
      .selectFrom(USER_ROLE)
      .where(USER_ROLE.USER_ID.eq(ULong.valueOf(2)))
      .and(USER_ROLE.TYPE.eq(Role.ADMIN))
      .fetchOne();
    assertNull(removedRole);
  }

}
