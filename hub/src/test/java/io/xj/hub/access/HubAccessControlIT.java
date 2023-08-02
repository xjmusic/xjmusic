// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.manager.UserManager;
import io.xj.hub.manager.UserManagerImpl;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
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

import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class HubAccessControlIT {

  static final int STRESS_TEST_ITERATIONS = 100;
  UserManager userManager;
  HubIntegrationTest test;

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
    test.reset();

    userManager = new UserManagerImpl(
      test.getEntityFactory(),
      test.getGoogleProvider(),
      test.getAccessTokenGenerator(),
      test.getSqlStoreProvider(),
      test.getKvStoreProvider(),
      "xj_session_test",
      "access_token",
      "",
      "/",
      1,
      "abc123"
    );
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void get() throws Exception {
    // user
    User user = buildUser("Test", "test@test.com", "test.jpg", "User,Artist");
    // user auth
    UserAuth userAuth = new UserAuth();
    UUID userAuthId = UUID.randomUUID();
    userAuth.setId(userAuthId);
    userAuth.setUserId(user.getId());
    userAuth.setType(UserAuthType.Google);
    userAuth.setExternalAccount("google");
    userAuth.setExternalAccessToken("google-token");
    // account user
    AccountUser accountUser = new AccountUser();
    accountUser.setUserId(user.getId());
    UUID accountId = UUID.randomUUID();
    accountUser.setAccountId(accountId);
    // access control provider
    Collection<AccountUser> accounts = new ArrayList<>(List.of(accountUser));
    String TEST_TOKEN = userManager.create(user, userAuth, accounts);

    // now stress test
    for (int i = 0; STRESS_TEST_ITERATIONS > i; i++) {
      HubAccess result = userManager.get(TEST_TOKEN);
      assertTrue(result.isValid(), "Result is valid");
    }
  }

  @Test
  public void computeKey() {
    assertEquals("xj_session_test:*", userManager.computeKey("*"));
  }

}
