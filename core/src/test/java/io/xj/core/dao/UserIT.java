// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.user.User;
import io.xj.core.model.user_access_token.UserAccessToken;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRoleType;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private UserDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);
    IntegrationTestEntity.insertAccountUser(1, 2);
    IntegrationTestEntity.insertUserAuth(2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, UserAuthType.Google, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(4, UserRoleType.User);

    // Instantiate the test subject
    testDAO = injector.getInstance(UserDAO.class);
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
    assertEquals(BigInteger.valueOf(2L), user.getId());
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

    User result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("User,Admin", result.getRoles());
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    User result = testDAO.readOne(access, BigInteger.valueOf(3L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(3L), result.getId());
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
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(4L));
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill has no account membership
      "roles", "User",
      "accounts", ""
    ));

    User result = testDAO.readOne(access, BigInteger.valueOf(4L));

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Admin",
      "accounts", "1"
    ));

    Collection<User> result = testDAO.readAll(access, Lists.newArrayList());

    assertEquals(3L, result.size());
  }

  @Test
  public void readAll_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<User> result = testDAO.readAll(access, Lists.newArrayList());

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill is in no accounts
      "roles", "User",
      "accounts", ""
    ));

    Collection<User> result = testDAO.readAll(access, Lists.newArrayList());

    assertNotNull(result);
    assertEquals(1L, result.size());
    assertEquals("bill", result.iterator().next().getName());
  }

  @Test
  public void destroyAllTokens() throws Exception {
    testDAO.destroyAllTokens(BigInteger.valueOf(2L));

    try {
      testDAO.readOneAccessToken(Access.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void updateUserRolesAndDestroyTokens() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("User,Artist");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(2L), inputData);

    // Access Token deleted
    try {
      testDAO.readOneAccessToken(Access.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
    // future test: token destroyed in Redis
    // Added artist role
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(2L), UserRoleType.Artist));
    // Removed admin role
    try {
      testDAO.readOneRole(Access.internal(), BigInteger.valueOf(2L), UserRoleType.Admin);
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  /**
   [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
   */
  @Test
  public void updateUserRoles_fromLegacyFormat() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(53, "user");
    IntegrationTestEntity.insertUserRole(53, "artist");
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("User,Artist,Engineer,Admin");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);

    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.User));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Artist));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Engineer));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Admin));
  }

  /**
   [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
   */
  @Test
  public void updateUserRoles_withLegacyFormat() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(53, "user");
    IntegrationTestEntity.insertUserRole(53, "artist");
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("user,artist,engineer,admin");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);

    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.User));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Artist));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Engineer));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Admin));
  }

  /**
   [#154118206]should be impossible to remove all roles.
   */
  @Test
  public void updateUserRoles_cannotRemoveAllRoles() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(53, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(53, UserRoleType.Artist);
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles(",");

    failure.expect(CoreException.class);
    failure.expectMessage("Valid Role is required");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);
  }

  /**
   [#154118206]should be impossible to remove all roles.
   */
  @Test
  public void updateUserRoles_cannotRemoveAllRoles_invalidRole() throws Exception {
    IntegrationTestEntity.insertUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif");
    IntegrationTestEntity.insertUserRole(53, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(53, UserRoleType.Artist);
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("shmengineer");

    failure.expect(CoreException.class);
    failure.expectMessage("'Shmengineer' is not a valid role");

    testDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);

    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Admin));
    assertNotNull(testDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Artist));
  }

}
