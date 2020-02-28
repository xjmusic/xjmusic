// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Account;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.model.User;
import io.xj.lib.core.model.UserAuth;
import io.xj.lib.core.model.UserAuthToken;
import io.xj.lib.core.model.UserAuthType;
import io.xj.lib.core.model.UserRole;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.IntegrationTestProvider;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private UserDAO subjectDAO;

  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(fake.user2, UserRoleType.User));
    test.insert(UserRole.create(fake.user2, UserRoleType.Admin));
    test.insert(AccountUser.create(fake.account1, fake.user2));
    UserAuth userAuth = test.insert(UserAuth.create(fake.user2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222"));
    test.insert(UserAuthToken.create(userAuth, "this-is-my-actual-access-token"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(fake.user3, UserRoleType.User));
    test.insert(AccountUser.create(fake.account1, fake.user3));

    // Bill has a "user" role but no account membership
    fake.user4 = test.insert(User.create("bill", "bill@email.com", "http://pictures.com/bill.gif"));
    test.insert(UserRole.create(fake.user4, UserRoleType.User));

    // Instantiate the test subject
    subjectDAO = injector.getInstance(UserDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
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
    assertEquals(fake.user2.getId(), user.getId());
    assertEquals("john", user.getName());
    assertEquals("http://pictures.com/john.gif", user.getAvatarUrl());
    assertEquals("john@email.com", user.getEmail());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    User result = subjectDAO.readOne(access, fake.user2.getId());

    assertNotNull(result);
    assertEquals(fake.user2.getId(), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("Admin,User", result.getRoles());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    subjectDAO.readOne(access, fake.user4.getId());
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    User result = subjectDAO.readOne(access, fake.user3.getId());

    assertNotNull(result);
    assertEquals(fake.user3.getId(), result.getId());
    assertEquals("jenny@email.com", result.getEmail());
    assertEquals("http://pictures.com/jenny.gif", result.getAvatarUrl());
    assertEquals("jenny", result.getName());
    assertEquals("User", result.getRoles());
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    Access access = Access.create(fake.user4, "User");

    User result = subjectDAO.readOne(access, fake.user4.getId());

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Admin");

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertEquals(3L, result.size());
  }

  @Test
  public void readAll_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    // Bill is in no accounts
    Access access = Access.create(fake.user4, "User");

    Collection<User> result = subjectDAO.readMany(access, Lists.newArrayList());

    assertNotNull(result);
    assertEquals(1L, result.size());
    assertEquals("bill", result.iterator().next().getName());
  }

  @Test
  public void destroyAllTokens() throws Exception {
    subjectDAO.destroyAllTokens(fake.user2.getId());

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

    subjectDAO.updateUserRolesAndDestroyTokens(access, fake.user2.getId(), inputData);

    // Access Token deleted
    try {
      subjectDAO.readOneAuthToken(Access.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
    // future test: token destroyed in Redis
    // Added artist role
    assertNotNull(subjectDAO.readOneRole(Access.internal(), fake.user2.getId(), UserRoleType.Artist));
    // Removed admin role
    try {
      subjectDAO.readOneRole(Access.internal(), fake.user2.getId(), UserRoleType.Admin);
      fail();
    } catch (CoreException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  // [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
  @Test
  public void updateUserRoles_fromLegacyFormat() throws Exception {
    fake.user53 = test.insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    test.insert(UserRole.create(fake.user53, UserRoleType.User));
    test.insert(UserRole.create(fake.user53, UserRoleType.Artist));
    Access access = Access.create("Admin");
    User inputData = new User()
      .setRoles("User,Artist,Engineer,Admin");

    subjectDAO.updateUserRolesAndDestroyTokens(access, fake.user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(Access.internal(), fake.user53.getId(), UserRoleType.User));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), fake.user53.getId(), UserRoleType.Artist));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), fake.user53.getId(), UserRoleType.Engineer));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), fake.user53.getId(), UserRoleType.Admin));
  }


  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles() throws Exception {
    fake.user53 = test.insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    test.insert(UserRole.create(fake.user53, UserRoleType.User));
    test.insert(UserRole.create(fake.user53, UserRoleType.Artist));
    Access access = Access.create("Admin");
    User inputData = new User()
      .setRoles(",");

    failure.expect(CoreException.class);
    failure.expectMessage("Valid Role is required");

    subjectDAO.updateUserRolesAndDestroyTokens(access, fake.user53.getId(), inputData);
  }

  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles_invalidRole() throws Exception {
    fake.user53 = test.insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    test.insert(UserRole.create(fake.user53, UserRoleType.User));
    test.insert(UserRole.create(fake.user53, UserRoleType.Artist));
    Access access = Access.create("Admin");
    User inputData = new User()
      .setRoles("duke");

    failure.expect(CoreException.class);
    failure.expectMessage("'Duke' is not a valid role");

    subjectDAO.updateUserRolesAndDestroyTokens(access, fake.user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(Access.internal(), fake.user53.getId(), UserRoleType.Admin));
    assertNotNull(subjectDAO.readOneRole(Access.internal(), fake.user53.getId(), UserRoleType.Artist));
  }

}
