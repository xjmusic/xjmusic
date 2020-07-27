// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.*;

public class UserIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private UserDAO subjectDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
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

    // Created User HubAccess Token
    assertNotNull(accessToken);

    // future test: token stored in Redis with correct auth

    // access token was persisted
    UserAuthToken userAccessToken = subjectDAO.readOneAuthToken(HubAccess.internal(), accessToken);
    assertNotNull(userAccessToken);

    // Created User Auth
    UserAuth userAuth = subjectDAO.readOneAuth(HubAccess.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("88888", userAuth.getExternalAccount());
    assertEquals("accessToken123", userAuth.getExternalAccessToken());
    assertEquals("refreshToken456", userAuth.getExternalRefreshToken());

    // Created User
    User user = subjectDAO.readOne(HubAccess.internal(), userAccessToken.getUserId());
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

    // Created User HubAccess Token
    assertNotNull(accessToken);
    // future test: token stored in Redis with correct auth
    UserAuthToken userAccessToken = subjectDAO.readOneAuthToken(HubAccess.internal(), accessToken);
    assertNotNull(userAccessToken);
    // Created User Auth
    UserAuth userAuth = subjectDAO.readOneAuth(HubAccess.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("22222", userAuth.getExternalAccount());
    assertEquals("external_access_token_123", userAuth.getExternalAccessToken());
    assertEquals("external_refresh_token_123", userAuth.getExternalRefreshToken());
    // Created User
    User user = subjectDAO.readOne(HubAccess.internal(), userAccessToken.getUserId());
    assertNotNull(user);
    assertEquals(fake.user2.getId(), user.getId());
    assertEquals("john", user.getName());
    assertEquals("http://pictures.com/john.gif", user.getAvatarUrl());
    assertEquals("john@email.com", user.getEmail());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    User result = subjectDAO.readOne(hubAccess, fake.user2.getId());

    assertNotNull(result);
    assertEquals(fake.user2.getId(), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("Admin,User", result.getRoles());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    subjectDAO.readOne(hubAccess, fake.user4.getId());
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    User result = subjectDAO.readOne(hubAccess, fake.user3.getId());

    assertNotNull(result);
    assertEquals(fake.user3.getId(), result.getId());
    assertEquals("jenny@email.com", result.getEmail());
    assertEquals("http://pictures.com/jenny.gif", result.getAvatarUrl());
    assertEquals("jenny", result.getName());
    assertEquals("User", result.getRoles());
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user4, "User");

    User result = subjectDAO.readOne(hubAccess, fake.user4.getId());

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User,Admin");

    Collection<User> result = subjectDAO.readMany(hubAccess, Lists.newArrayList());

    assertEquals(3L, result.size());
  }

  @Test
  public void readMany_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<User> result = subjectDAO.readMany(hubAccess, Lists.newArrayList());

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    // Bill is in no accounts
    HubAccess hubAccess = HubAccess.create(fake.user4, "User");

    Collection<User> result = subjectDAO.readMany(hubAccess, Lists.newArrayList());

    assertNotNull(result);
    assertEquals(1L, result.size());
    assertEquals("bill", result.iterator().next().getName());
  }

  @Test
  public void destroyAllTokens() throws Exception {
    subjectDAO.destroyAllTokens(fake.user2.getId());

    try {
      subjectDAO.readOneAuthToken(HubAccess.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (DAOException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void updateUserRolesAndDestroyTokens() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    User inputData = new User()
      .setRoles("User,Artist");

    subjectDAO.updateUserRolesAndDestroyTokens(hubAccess, fake.user2.getId(), inputData);

    // HubAccess Token deleted
    try {
      subjectDAO.readOneAuthToken(HubAccess.internal(), "this-is-my-actual-hubAccess-token");
      fail();
    } catch (DAOException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
    // future test: token destroyed in Redis
    // Added artist role
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user2.getId(), UserRoleType.Artist));
    // Removed admin role
    try {
      subjectDAO.readOneRole(HubAccess.internal(), fake.user2.getId(), UserRoleType.Admin);
      fail();
    } catch (DAOException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  // [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
  @Test
  public void updateUserRoles_fromLegacyFormat() throws Exception {
    fake.user53 = test.insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    test.insert(UserRole.create(fake.user53, UserRoleType.User));
    test.insert(UserRole.create(fake.user53, UserRoleType.Artist));
    HubAccess hubAccess = HubAccess.create("Admin");
    User inputData = new User()
      .setRoles("User,Artist,Engineer,Admin");

    subjectDAO.updateUserRolesAndDestroyTokens(hubAccess, fake.user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRoleType.User));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRoleType.Artist));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRoleType.Engineer));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRoleType.Admin));
  }


  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles() throws Exception {
    fake.user53 = test.insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    test.insert(UserRole.create(fake.user53, UserRoleType.User));
    test.insert(UserRole.create(fake.user53, UserRoleType.Artist));
    HubAccess hubAccess = HubAccess.create("Admin");
    User inputData = new User()
      .setRoles(",");

    failure.expect(DAOException.class);
    failure.expectMessage("Valid Role is required");

    subjectDAO.updateUserRolesAndDestroyTokens(hubAccess, fake.user53.getId(), inputData);
  }

  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles_invalidRole() throws Exception {
    fake.user53 = test.insert(User.create("julio", "julio.rodriguez@xj.io", "http://pictures.com/julio.gif"));
    test.insert(UserRole.create(fake.user53, UserRoleType.User));
    test.insert(UserRole.create(fake.user53, UserRoleType.Artist));
    HubAccess hubAccess = HubAccess.create("Admin");
    User inputData = new User()
      .setRoles("duke");

    failure.expect(ValueException.class);
    failure.expectMessage("'Duke' is not a valid role");

    subjectDAO.updateUserRolesAndDestroyTokens(hubAccess, fake.user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRoleType.Admin));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRoleType.Artist));
  }

}
