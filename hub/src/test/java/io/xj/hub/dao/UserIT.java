// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserAuthToken;
import io.xj.UserRole;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.testing.HubIntegrationTestModule;
import io.xj.hub.testing.HubIntegrationTestProvider;
import io.xj.hub.testing.HubTestConfiguration;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.util.ValueException;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private UserDAO subjectDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bananas")
      .build());

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("john")
      .setEmail("john@email.com")
      .setAvatarUrl("http://pictures.com/john.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.Admin)
      .build());
    test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId())
      .build());
    UserAuth userAuth = test.insert(UserAuth.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserAuth.Type.Google)
      .setExternalAccessToken("external_access_token_123")
      .setExternalRefreshToken("external_refresh_token_123")
      .setExternalAccount("22222")
      .build());
    test.insert(UserAuthToken.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setUserAuthId(userAuth.getId())
      .setAccessToken("this-is-my-actual-access-token")
      .build());

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("jenny")
      .setEmail("jenny@email.com")
      .setAvatarUrl("http://pictures.com/jenny.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user3.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user3.getId())
      .build());

    // Bill has a "user" role but no account membership
    fake.user4 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bill")
      .setEmail("bill@email.com")
      .setAvatarUrl("http://pictures.com/bill.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user4.getId())
      .setType(UserRole.Type.User)
      .build());

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
      UserAuth.Type.Google,
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
    assertEquals(UserAuth.Type.Google, userAuth.getType());
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
      UserAuth.Type.Google,
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
    assertEquals(UserAuth.Type.Google, userAuth.getType());
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
    User inputData = User.newBuilder()
      .setRoles("User,Artist")
      .build();

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
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user2.getId(), UserRole.Type.Artist));
    // Removed admin role
    try {
      subjectDAO.readOneRole(HubAccess.internal(), fake.user2.getId(), UserRole.Type.Admin);
      fail();
    } catch (DAOException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  // [#154118206] Admin wants to change user roles, even if they are in lowercase legacy format
  @Test
  public void updateUserRoles_fromLegacyFormat() throws Exception {
    fake.user53 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("julio")
      .setEmail("julio.rodriguez@xj.io")
      .setAvatarUrl("http://pictures.com/julio.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user53.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user53.getId())
      .setType(UserRole.Type.Artist)
      .build());
    HubAccess hubAccess = HubAccess.create("Admin");
    User inputData = User.newBuilder()
      .setRoles("User,Artist,Engineer,Admin")
      .build();

    subjectDAO.updateUserRolesAndDestroyTokens(hubAccess, fake.user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRole.Type.User));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRole.Type.Artist));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRole.Type.Engineer));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRole.Type.Admin));
  }


  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles() throws Exception {
    fake.user53 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("julio")
      .setEmail("julio.rodriguez@xj.io")
      .setAvatarUrl("http://pictures.com/julio.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user53.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user53.getId())
      .setType(UserRole.Type.Artist)
      .build());
    HubAccess hubAccess = HubAccess.create("Admin");
    User inputData = User.newBuilder()
      .setRoles(",")
      .build();

    failure.expect(DAOException.class);
    failure.expectMessage("Valid Role is required");

    subjectDAO.updateUserRolesAndDestroyTokens(hubAccess, fake.user53.getId(), inputData);
  }

  // [#154118206]should be impossible to remove all roles.
  @Test
  public void updateUserRoles_cannotRemoveAllRoles_invalidRole() throws Exception {
    fake.user53 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("julio")
      .setEmail("julio.rodriguez@xj.io")
      .setAvatarUrl("http://pictures.com/julio.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user53.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user53.getId())
      .setType(UserRole.Type.Artist)
      .build());
    HubAccess hubAccess = HubAccess.create("Admin");
    User inputData = User.newBuilder()
      .setRoles("duke")
      .build();

    failure.expect(ValueException.class);
    failure.expectMessage("No enum constant io.xj.UserRole.Type.Duke");

    subjectDAO.updateUserRolesAndDestroyTokens(hubAccess, fake.user53.getId(), inputData);

    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRole.Type.Admin));
    assertNotNull(subjectDAO.readOneRole(HubAccess.internal(), fake.user53.getId(), UserRole.Type.Artist));
  }

}
