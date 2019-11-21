// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.user;

import com.google.common.collect.ImmutableList;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.UserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.User;
import io.xj.core.model.UserAuth;
import io.xj.core.model.UserAuthType;
import io.xj.core.model.UserAuthToken;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    account1 = insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    insert(UserRole.create(user2, UserRoleType.User));
    insert(UserRole.create(user2, UserRoleType.Admin));
    insert(AccountUser.create(account1, user2));
    UserAuth userAuth = insert(UserAuth.create(user2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222"));
    insert(UserAuthToken.create(userAuth, "this-is-my-actual-access-token"));

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(UserRole.create(user3, UserRoleType.User));
    insert(AccountUser.create(account1, user3));

    // Bill has a "user" role but no account membership
    user4 = insert(User.create("bill", "bill@email.com", "http://pictures.com/bill.gif"));
    insert(UserRole.create(user4, UserRoleType.User));

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
    UserAuthToken userAccessToken = subjectDAO.readOneAuthToken(Access.internal(), accessToken);
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
    UserAuthToken userAccessToken = subjectDAO.readOneAuthToken(Access.internal(), accessToken);
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
    assertEquals(user2.getId(), user.getId());
    assertEquals("john", user.getName());
    assertEquals("http://pictures.com/john.gif", user.getAvatarUrl());
    assertEquals("john@email.com", user.getEmail());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");

    User result = subjectDAO.readOne(access, user2.getId());

    assertNotNull(result);
    assertEquals(user2.getId(), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("User,Admin", result.getRoles());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    subjectDAO.readOne(access, user4.getId());
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");

    User result = subjectDAO.readOne(access, user3.getId());

    assertNotNull(result);
    assertEquals(user3.getId(), result.getId());
    assertEquals("jenny@email.com", result.getEmail());
    assertEquals("http://pictures.com/jenny.gif", result.getAvatarUrl());
    assertEquals("jenny", result.getName());
    assertEquals("User", result.getRoles());
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    Access access = Access.create(user4, "User");

    User result = subjectDAO.readOne(access, user4.getId());

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User,Admin");

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertEquals(3L, result.size());
  }

  @Test
  public void readAll_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    // Bill is in no accounts
    Access access = Access.create(user4, "User");

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertNotNull(result);
    assertEquals(1L, result.size());
    assertEquals("bill", result.iterator().next().getName());
  }

  @Test
  public void destroyAllTokens() throws Exception {
    subjectDAO.destroyAllTokens(user2.getId());

    try {
      subjectDAO.readOneAuthToken(Access.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void updateUserRolesAndDestroyTokens() throws Exception {
    Access access = Access.create("Admin");
    User inputData = new User()
      .setRoles("User,Artist");

    subjectDAO.updateUserRolesAndDestroyTokens(access, user2.getId(), inputData);

    // Access Token deleted
    try {
      subjectDAO.readOneAuthToken(Access.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
    // future test: token destroyed in Redis
    // Added artist role
    assertNotNull(subjectDAO.readOneRole(Access.internal(), user2.getId(), UserRoleType.Artist));
    // Removed admin role
    try {
      subjectDAO.readOneRole(Access.internal(), user2.getId(), UserRoleType.Admin);
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  // [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
  @Test
  public void updateUserRoles_fromLegacyFormat() throws Exception {
    User user53 = insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    insert(UserRole.create(user53, UserRoleType.User));
    insert(UserRole.create(user53, UserRoleType.Artist));
    Access access = Access.create("Admin");
    User inputData = new User()
      .setRoles("User,Artist,Engineer,Admin");

    subjectDAO.updateUserRolesAndDestroyTokens(access, user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(Access.internal(), user53.getId(), UserRoleType.User));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), user53.getId(), UserRoleType.Artist));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), user53.getId(), UserRoleType.Engineer));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), user53.getId(), UserRoleType.Admin));
  }


  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles() throws Exception {
    User user53 = insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    insert(UserRole.create(user53, UserRoleType.User));
    insert(UserRole.create(user53, UserRoleType.Artist));
    Access access = Access.create("Admin");
    User inputData = new User()
      .setRoles(",");

    failure.expect(CoreException.class);
    failure.expectMessage("Valid Role is required");

    subjectDAO.updateUserRolesAndDestroyTokens(access, user53.getId(), inputData);
  }

  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles_invalidRole() throws Exception {
    User user53 = insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    insert(UserRole.create(user53, UserRoleType.User));
    insert(UserRole.create(user53, UserRoleType.Artist));
    Access access = Access.create("Admin");
    User inputData = new User()
      .setRoles("duke");

    failure.expect(CoreException.class);
    failure.expectMessage("'Duke' is not a valid role");

    subjectDAO.updateUserRolesAndDestroyTokens(access, user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(Access.internal(), user53.getId(), UserRoleType.Admin));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), user53.getId(), UserRoleType.Artist));
  }

}
