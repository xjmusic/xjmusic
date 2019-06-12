// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableMap;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.user.User;
import io.xj.core.model.user.access_token.UserAccessToken;
import io.xj.core.model.user.auth.UserAuth;
import io.xj.core.model.user.auth.UserAuthType;
import io.xj.core.model.user.role.UserRoleType;
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

public class UserIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private UserDAO subjectDAO;

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(newAccount(1, "bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif"));
    insert(newUserRole(2, UserRoleType.User));
    insert(newUserRole(2, UserRoleType.Admin));
    insert(newAccountUser(1, 2));
    UserAuth userAuth = insert(newUserAuth(1, 2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222"));
    insert(newUserAccessToken(userAuth, "this-is-my-actual-access-token"));

    // Jenny has a "user" role and belongs to account "bananas"
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(newUserRole(3, UserRoleType.User));
    insert(newAccountUser(1, 3));

    // Bill has a "user" role but no account membership
    insert(newUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif"));
    insert(newUserRole(4, UserRoleType.User));

    // Instantiate the test subject
    subjectDAO = injector.getInstance(UserDAO.class);
  }

  @Test
  public void authenticate_NewUser() throws Exception {
    String accessToken = subjectDAO.authenticate(
      UserAuthType.Google,
      "88888",
      "accessToken123",
      "refreshToken456",
      "wayne",
      "http://pictures.com/wayne.gif",
      "shamu@email.com"
    );

    // Created User Access Token
    assertNotNull(accessToken);

    // future test: token stored in Redis with correct auth

    // access token was persisted
    UserAccessToken userAccessToken = subjectDAO.readOneAccessToken(Access.internal(), accessToken);
    assertNotNull(userAccessToken);

    // Created User Auth
    UserAuth userAuth = subjectDAO.readOneAuth(Access.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("88888", userAuth.getExternalAccount());
    assertEquals("accessToken123", userAuth.getExternalAccessToken());
    assertEquals("refreshToken456", userAuth.getExternalRefreshToken());

    // Created User
    User user = subjectDAO.readOne(Access.internal(), userAccessToken.getUserId());
    assertNotNull(user);
    assertEquals("wayne", user.getName());
    assertEquals("http://pictures.com/wayne.gif", user.getAvatarUrl());
    assertEquals("shamu@email.com", user.getEmail());
  }

  @Test
  public void authenticate_ExistingUser() throws Exception {
    String accessToken = subjectDAO.authenticate(
      UserAuthType.Google,
      "22222",
      "accessToken123",
      "refreshToken456",
      "john wayne",
      "http://pictures.com/john.gif",
      "john@email.com"
    );

    // Created User Access Token
    assertNotNull(accessToken);
    // future test: token stored in Redis with correct auth
    UserAccessToken userAccessToken = subjectDAO.readOneAccessToken(Access.internal(), accessToken);
    assertNotNull(userAccessToken);
    // Created User Auth
    UserAuth userAuth = subjectDAO.readOneAuth(Access.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("22222", userAuth.getExternalAccount());
    assertEquals("external_access_token_123", userAuth.getExternalAccessToken());
    assertEquals("external_refresh_token_123", userAuth.getExternalRefreshToken());
    // Created User
    User user = subjectDAO.readOne(Access.internal(), userAccessToken.getUserId());
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

    User result = subjectDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("User,Admin", result.getRoles());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    subjectDAO.readOne(access, BigInteger.valueOf(4L));
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    User result = subjectDAO.readOne(access, BigInteger.valueOf(3L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(3L), result.getId());
    assertEquals("jenny@email.com", result.getEmail());
    assertEquals("http://pictures.com/jenny.gif", result.getAvatarUrl());
    assertEquals("jenny", result.getName());
    assertEquals("User", result.getRoles());
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill has no account membership
      "roles", "User",
      "accounts", ""
    ));

    User result = subjectDAO.readOne(access, BigInteger.valueOf(4L));

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Admin",
      "accounts", "1"
    ));

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertEquals(3L, result.size());
  }

  @Test
  public void readAll_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "4", // Bill is in no accounts
      "roles", "User",
      "accounts", ""
    ));

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertNotNull(result);
    assertEquals(1L, result.size());
    assertEquals("bill", result.iterator().next().getName());
  }

  @Test
  public void destroyAllTokens() throws Exception {
    subjectDAO.destroyAllTokens(BigInteger.valueOf(2L));

    try {
      subjectDAO.readOneAccessToken(Access.internal(), "this-is-my-actual-access-token");
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

    subjectDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(2L), inputData);

    // Access Token deleted
    try {
      subjectDAO.readOneAccessToken(Access.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
    // future test: token destroyed in Redis
    // Added artist role
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(2L), UserRoleType.Artist));
    // Removed admin role
    try {
      subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(2L), UserRoleType.Admin);
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  // [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
  @Test
  public void updateUserRoles_fromLegacyFormat() throws Exception {
    insert(newUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    insert(newUserRole(53, "user"));
    insert(newUserRole(53, "artist"));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("User,Artist,Engineer,Admin");

    subjectDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);

    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.User));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Artist));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Engineer));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Admin));
  }


  // [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
  @Test
  public void updateUserRoles_withLegacyFormat() throws Exception {
    insert(newUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    insert(newUserRole(53, "user"));
    insert(newUserRole(53, "artist"));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("user,artist,engineer,admin");

    subjectDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);

    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.User));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Artist));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Engineer));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Admin));
  }

  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles() throws Exception {
    insert(newUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    insert(newUserRole(53, UserRoleType.User));
    insert(newUserRole(53, UserRoleType.Artist));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles(",");

    failure.expect(CoreException.class);
    failure.expectMessage("Valid Role is required");

    subjectDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);
  }

  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles_invalidRole() throws Exception {
    insert(newUser(53, "julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    insert(newUserRole(53, UserRoleType.User));
    insert(newUserRole(53, UserRoleType.Artist));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    User inputData = new User()
      .setRoles("duke");

    failure.expect(CoreException.class);
    failure.expectMessage("'Duke' is not a valid role");

    subjectDAO.updateUserRolesAndDestroyTokens(access, BigInteger.valueOf(53L), inputData);

    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Admin));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), BigInteger.valueOf(53L), UserRoleType.Artist));
  }

}
