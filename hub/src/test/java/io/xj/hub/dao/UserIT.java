// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUserAuth;
import static io.xj.hub.IntegrationTestingFixtures.buildUserAuthToken;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserIT {
  private UserDAO subjectDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif", "User, Admin"));
    test.insert(buildAccountUser(fake.account1, fake.user2));
    UserAuth userAuth = test.insert(buildUserAuth(fake.user2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222"));
    test.insert(buildUserAuthToken(userAuth, "this-is-my-actual-access-token"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Bill has a "user" role but no account membership
    fake.user4 = test.insert(buildUser("bill", "bill@email.com", "http://pictures.com/bill.gif", "User"));

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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin,User");

    User result = subjectDAO.readOne(hubAccess, fake.user2.getId());

    assertNotNull(result);
    assertEquals(fake.user2.getId(), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("http://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("User, Admin", result.getRoles());
  }

  @Test
  public void readOne_inMultipleAccounts() throws Exception {
    fake.account2 = test.insert(buildAccount("too bananas"));
    test.insert(buildAccountUser(fake.account2, fake.user3));
    HubAccess hubAccess = HubAccess.create(fake.user3, ImmutableList.of(fake.account1, fake.account2));

    User result = subjectDAO.readOne(hubAccess, fake.user3.getId());

    assertNotNull(result);
    assertEquals(fake.user3.getId(), result.getId());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    var e = assertThrows(DAOException.class, () ->
      subjectDAO.readOne(hubAccess, fake.user4.getId()));

    assertEquals("Record does not exist", e.getMessage());
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
}
