// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.user.User;
import io.xj.core.model.user_access_token.UserAccessToken;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.assertj.core.util.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UserIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private UserDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(2, 2, UserRoleType.Admin);
    IntegrationTestEntity.insertAccountUser(3, 1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(5, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(6, 1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(7, 4, UserRoleType.User);

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
      UserAuthType.Google,
      "88888",
      "accesstoken123",
      "refreshtoken456",
      "wayne",
      "http://pictures.com/wayne.gif",
      "shamu@email.com"
    );

    // Created User Access Token
    assertNotNull(accessToken);

    // future test: token stored in Redis with correct auth

    // access token was persisted
    UserAccessToken userAccessToken = testDAO.readOneAccessToken(Access.internal(), accessToken);
    assertNotNull(userAccessToken);

    // Created User Auth
    UserAuth userAuth = testDAO.readOneAuth(Access.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("88888", userAuth.getExternalAccount());
    assertEquals("accesstoken123", userAuth.getExternalAccessToken());
    assertEquals("refreshtoken456", userAuth.getExternalRefreshToken());

    // Created User
    User user = testDAO.readOne(Access.internal(), userAccessToken.getUserId());
    assertNotNull(user);
    assertEquals("wayne", user.getName());
    assertEquals("http://pictures.com/wayne.gif", user.getAvatarUrl());
    assertEquals("shamu@email.com", user.getEmail());
  }

  @Test
  public void authenticate_ExistingUser() throws Exception {
    String accessToken = testDAO.authenticate(
      UserAuthType.Google,
      "22222",
      "accesstoken123",
      "refreshtoken456",
      "john wayne",
      "http://pictures.com/john.gif",
      "john@email.com"
    );

    // Created User Access Token
    assertNotNull(accessToken);
    // future test: token stored in Redis with correct auth
    UserAccessToken userAccessToken = testDAO.readOneAccessToken(Access.internal(), accessToken);
    assertNotNull(userAccessToken);
    // Created User Auth
    UserAuth userAuth = testDAO.readOneAuth(Access.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("22222", userAuth.getExternalAccount());
    assertEquals("external_access_token_123", userAuth.getExternalAccessToken());
    assertEquals("external_refresh_token_123", userAuth.getExternalRefreshToken());
    // Created User
    User user = testDAO.readOne(Access.internal(), userAccessToken.getUserId());
    assertNotNull(user);
    assertEquals(BigInteger.valueOf(2), user.getId());
    assertEquals("john", user.getName());
    assertEquals("http://pictures.com/john.gif", user.getAvatarUrl());
    assertEquals("john@email.com", user.getEmail());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    User result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("User,Admin", result.getRoles());
  }

  @Test
  public void readOne_toJSONObject() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    User result = testDAO.readOne(access, BigInteger.valueOf(2));
    assertNotNull(result);
    JSONObject resultJSON = JSON.objectFrom(result);

    assertNotNull(resultJSON);
    assertEquals(2, resultJSON.get("id"));
    assertEquals("john@email.com", resultJSON.get("email"));
    assertEquals("http://pictures.com/john.gif", resultJSON.get("avatarUrl"));
    assertEquals("john", resultJSON.get("name"));
    assertEquals("User,Admin", resultJSON.get("roles"));
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    User result = testDAO.readOne(access, BigInteger.valueOf(3));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(3), result.getId());
    assertEquals("jenny@email.com", result.getEmail());
    assertEquals("http://pictures.com/jenny.gif", result.getAvatarUrl());
    assertEquals("jenny", result.getName());
    assertEquals("User", result.getRoles());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    User result = testDAO.readOne(access, BigInteger.valueOf(4));

    assertNull(result);
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill has no account membership
      "roles", "User",
      "accounts", ""
    ));

    User result = testDAO.readOne(access, BigInteger.valueOf(4));

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Admin",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, Lists.newArrayList()));

    assertNotNull(result);
    assertEquals(3, result.length());
  }

  @Test
  public void readAll_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, Lists.newArrayList()));

    assertNotNull(result);
    assertEquals(2, result.length());
  }

  @Test
  public void readAll_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill is in no accounts
      "roles", "User",
      "accounts", ""
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, Lists.newArrayList()));

    assertNotNull(result);
    assertEquals(1, result.length());
    JSONObject resultSub = (JSONObject) result.get(0);
    assertEquals("bill", resultSub.get("name"));
  }

  @Test
  public void destroyAllTokens() throws Exception {
    testDAO.destroyAllTokens(BigInteger.valueOf(2));

    UserAccessToken result = testDAO.readOneAccessToken(Access.internal(), "this-is-my-actual-access-token");
    assertNull(result);
    // future test: token destroyed in Redis
  }

  @Test
  public void updateUserRolesAndDestroyTokens() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("User,Artist");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(2), inputData);

    // Access Token deleted
    UserAccessToken result = testDAO.readOneAccessToken(Access.internal(), "this-is-my-actual-access-token");
    assertNull(result);
    // future test: token destroyed in Redis
    // Added artist role
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(2), UserRoleType.Artist));
    // Removed admin role
    assertNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(2), UserRoleType.Admin));
  }

  /**
   [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
   */
  @Test
  public void updateUserRoles_fromLegacyFormat() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(152, 53, "user");
    IntegrationTestEntity.insertUserRole(153, 53, "artist");
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("User,Artist,Engineer,Admin");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53), inputData);

    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.User));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Artist));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Engineer));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Admin));
  }

  /**
   [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
   */
  @Test
  public void updateUserRoles_withLegacyFormat() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(152, 53, "user");
    IntegrationTestEntity.insertUserRole(153, 53, "artist");
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("user,artist,engineer,admin");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53), inputData);

    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.User));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Artist));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Engineer));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Admin));
  }

  /**
   [#154118206]should be impossible to remove all roles.
   */
  @Test
  public void updateUserRoles_cannotRemoveAllRoles() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(152, 53, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(153, 53, UserRoleType.Artist);
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles(",");

    failure.expect(BusinessException.class);
    failure.expectMessage("Valid Role is required");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53), inputData);
  }

  /**
   [#154118206]should be impossible to remove all roles.
   */
  @Test
  public void updateUserRoles_cannotRemoveAllRoles_invalidRole() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(152, 53, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(153, 53, UserRoleType.Artist);
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("shmengineer");

    failure.expect(BusinessException.class);
    failure.expectMessage("'Shmengineer' is not a valid role");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53), inputData);

    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Admin));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53), UserRoleType.Artist));
  }

}
