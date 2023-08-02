// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUserAuth;
import static io.xj.hub.IntegrationTestingFixtures.buildUserAuthToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class UserManagerDbTest {
  UserManager subjectManager;

  HubIntegrationTest test;
  IntegrationTestingFixtures fake;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @Autowired
  HubIntegrationTestFactory integrationTestFactory;

  @BeforeEach
  public void setUp() throws Exception {
    test = integrationTestFactory.build();
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "User, Admin"));
    test.insert(buildAccountUser(fake.account1, fake.user2));
    UserAuth userAuth = test.insert(buildUserAuth(fake.user2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222"));
    test.insert(buildUserAuthToken(userAuth, "this-is-my-actual-access-token"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Bill has a "user" role but no account membership
    fake.user4 = test.insert(buildUser("bill", "bill@email.com", "https://pictures.com/bill.gif", "User"));

    // Instantiate the test subject
    subjectManager = new UserManagerImpl(test.getEntityFactory(), test.getGoogleProvider(), test.getAccessTokenGenerator(), test.getSqlStoreProvider(), test.getKvStoreProvider(), "", "", "", "", 1, "");
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void authenticate_NewUser() throws Exception {
    String accessToken = subjectManager.authenticate(
      UserAuthType.Google,
      "88888",
      "accessToken123",
      "refreshToken456",
      "wayne",
      "https://pictures.com/wayne.gif",
      "shamu@email.com"
    );

    // Created User HubAccess Token
    assertNotNull(accessToken);

    // access token was persisted
    UserAuthToken userAccessToken = subjectManager.readOneAuthToken(HubAccess.internal(), accessToken);
    assertNotNull(userAccessToken);

    // Created User Auth
    UserAuth userAuth = subjectManager.readOneAuth(HubAccess.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("88888", userAuth.getExternalAccount());
    assertEquals("accessToken123", userAuth.getExternalAccessToken());
    assertEquals("refreshToken456", userAuth.getExternalRefreshToken());

    // Created User
    User user = subjectManager.readOne(HubAccess.internal(), userAccessToken.getUserId());
    assertNotNull(user);
    assertEquals("wayne", user.getName());
    assertEquals("https://pictures.com/wayne.gif", user.getAvatarUrl());
    assertEquals("shamu@email.com", user.getEmail());
  }

  @Test
  public void authenticate_ExistingUser() throws Exception {
    String accessToken = subjectManager.authenticate(
      UserAuthType.Google,
      "22222",
      "accessToken123",
      "refreshToken456",
      "john wayne",
      "https://pictures.com/john.gif",
      "john@email.com"
    );

    // Created User HubAccess Token
    assertNotNull(accessToken);
    UserAuthToken userAccessToken = subjectManager.readOneAuthToken(HubAccess.internal(), accessToken);
    assertNotNull(userAccessToken);
    // Created User Auth
    UserAuth userAuth = subjectManager.readOneAuth(HubAccess.internal(), userAccessToken.getUserAuthId());
    assertNotNull(userAuth);
    assertEquals(UserAuthType.Google, userAuth.getType());
    assertEquals("22222", userAuth.getExternalAccount());
    assertEquals("external_access_token_123", userAuth.getExternalAccessToken());
    assertEquals("external_refresh_token_123", userAuth.getExternalRefreshToken());
    // Created User
    User user = subjectManager.readOne(HubAccess.internal(), userAccessToken.getUserId());
    assertNotNull(user);
    assertEquals(fake.user2.getId(), user.getId());
    assertEquals("john", user.getName());
    assertEquals("https://pictures.com/john.gif", user.getAvatarUrl());
    assertEquals("john@email.com", user.getEmail());
  }

  @Test
  public void update() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin,User");
    User update = buildUser("Timmy", "timmy@email.com", "https://pictures.com/timmy.jpg", "User,Artist,Engineer,Admin");

    User result = subjectManager.update(access, fake.user2.getId(), update);

    assertNotNull(result);
    assertEquals(fake.user2.getId(), result.getId());
    assertEquals("timmy@email.com", result.getEmail());
    assertEquals("https://pictures.com/timmy.jpg", result.getAvatarUrl());
    assertEquals("Timmy", result.getName());
    assertEquals("Admin, Engineer, Artist, User", result.getRoles());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin,User");

    User result = subjectManager.readOne(access, fake.user2.getId());

    assertNotNull(result);
    assertEquals(fake.user2.getId(), result.getId());
    assertEquals("john@email.com", result.getEmail());
    assertEquals("https://pictures.com/john.gif", result.getAvatarUrl());
    assertEquals("john", result.getName());
    assertEquals("User, Admin", result.getRoles());
  }

  @Test
  public void readOne_inMultipleAccounts() throws Exception {
    fake.account2 = test.insert(buildAccount("too bananas"));
    test.insert(buildAccountUser(fake.account2, fake.user3));
    HubAccess access = HubAccess.create(fake.user3, UUID.randomUUID(), List.of(fake.account1, fake.account2));

    User result = subjectManager.readOne(access, fake.user3.getId());

    assertNotNull(result);
    assertEquals(fake.user3.getId(), result.getId());
  }

  @Test
  public void readOne_UserCannotSeeUserWithoutCommonAccountMembership() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");

    var e = assertThrows(ManagerException.class, () ->
      subjectManager.readOne(access, fake.user4.getId()));

    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readOne_UserSeesAnotherUserWithCommonAccountMembership() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");

    User result = subjectManager.readOne(access, fake.user3.getId());

    assertNotNull(result);
    assertEquals(fake.user3.getId(), result.getId());
    assertEquals("jenny@email.com", result.getEmail());
    assertEquals("https://pictures.com/jenny.gif", result.getAvatarUrl());
    assertEquals("jenny", result.getName());
    assertEquals("User", result.getRoles());
  }

  @Test
  public void readOne_UserWithNoAccountMembershipCanStillSeeSelf() throws Exception {
    HubAccess access = HubAccess.create(fake.user4, "User");

    User result = subjectManager.readOne(access, fake.user4.getId());

    assertNotNull(result);
    assertEquals("bill", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User,Admin");

    Collection<User> result = subjectManager.readMany(access, new ArrayList<>());

    assertEquals(3L, result.size());
  }

  @Test
  public void readMany_UserSeesSelfAndOtherUsersInSameAccount() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");

    Collection<User> result = subjectManager.readMany(access, new ArrayList<>());

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_UserWithoutAccountMembershipSeesOnlySelf() throws Exception {
    // Bill is in no accounts
    HubAccess access = HubAccess.create(fake.user4, "User");

    Collection<User> result = subjectManager.readMany(access, new ArrayList<>());

    assertNotNull(result);
    assertEquals(1L, result.size());
    assertEquals("bill", result.iterator().next().getName());
  }

  @Test
  public void destroyAllTokens() throws Exception {
    subjectManager.destroyAllTokens(fake.user2.getId());

    try {
      subjectManager.readOneAuthToken(HubAccess.internal(), "this-is-my-actual-access-token");
      fail();
    } catch (ManagerException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }
}
