// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.external.AuthType;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.User;
import io.outright.xj.core.model.user.UserWrapper;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.util.CSV.CSV;

import org.jooq.types.ULong;

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
    IntegrationTestService.setup();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, Role.USER);
    IntegrationTestEntity.insertUserRole(2, Role.ADMIN);
    IntegrationTestEntity.insertAccountUser(1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, AuthType.GOOGLE, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, Role.USER);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(4, Role.USER);

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
    JSONObject actualResult = testDAO.readOne(ULong.valueOf(2));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(2), actualResult.get("id"));
    assertEquals("john@email.com", actualResult.get("email"));
    assertEquals("http://pictures.com/john.gif", actualResult.get("avatarUrl"));
    assertEquals("john", actualResult.get("name"));
    assertEquals("user,admin", actualResult.get("roles"));
  }

  @Test
  public void readOneVisible_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    JSONObject actualResult = testDAO.readOneVisible(ULong.valueOf(2), ULong.valueOf(3));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(3), actualResult.get("id"));
    assertEquals("jenny@email.com", actualResult.get("email"));
    assertEquals("http://pictures.com/jenny.gif", actualResult.get("avatarUrl"));
    assertEquals("jenny", actualResult.get("name"));
    assertEquals("user", actualResult.get("roles"));
  }

  @Test
  public void readOneVisible_UserCannotSeeUserWithoutCommonAccountMembership() throws Exception {
    JSONObject actualResult = testDAO.readOneVisible(ULong.valueOf(2), ULong.valueOf(4));

    assertNull(actualResult);
  }

  @Test
  public void readAll() throws Exception {
    JSONArray actualResult = testDAO.readAll();

    assertNotNull(actualResult);
    assertEquals(3, actualResult.length());
  }

  @Test
  public void readAllVisible_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    JSONArray actualResult = testDAO.readAllVisible(ULong.valueOf(2));

    assertNotNull(actualResult);
    assertEquals(2, actualResult.length());
  }

  @Test
  public void readAllVisible_UserWithoutAccountMembershipSeesNothing() throws Exception {
    JSONArray actualResult = testDAO.readAllVisible(ULong.valueOf(4));

    assertNotNull(actualResult);
    assertEquals(0, actualResult.length());
  }

  @Test
  public void destroyAllTokens() throws Exception {
    testDAO.destroyAllTokens(ULong.valueOf(2));

    UserAccessTokenRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.ACCESS_TOKEN.eq("this-is-my-actual-access-token"))
      .fetchOne();
    assertNull(deletedRecord);
    // TODO: assert token destroyed in Redis
  }

  @Test
  public void updateUserRolesAndDestroyTokens() throws Exception {
    User inputData = new User();
    inputData.setRoles("user,artist");
    UserWrapper inputDataWrapper = new UserWrapper();
    inputDataWrapper.setUser(inputData);

    testDAO.updateUserRolesAndDestroyTokens(ULong.valueOf(2), inputDataWrapper);

    // Access Token deleted
    UserAccessTokenRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(USER_ACCESS_TOKEN)
      .where(USER_ACCESS_TOKEN.ACCESS_TOKEN.eq("this-is-my-actual-access-token"))
      .fetchOne();
    assertNull(deletedRecord);
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
