// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.manager.UserManager;
import io.xj.hub.manager.UserManagerImpl;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.app.AppEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class HubAccessControlIT {
  private static final AppEnvironment env = AppEnvironment.from(ImmutableMap.of(
    "SESSION_NAMESPACE", "xj_session_test"
  ));

  private static final int STRESS_TEST_ITERATIONS = 100;
  UserManager userManager;
  private HubIntegrationTest test;

  @BeforeEach
  public void setUp() throws Exception {
    test = HubIntegrationTestFactory.build(env);

    test.reset();

    userManager = new UserManagerImpl(
      env,
      test.getEntityFactory(),
      test.getGoogleProvider(),
      test.getAccessTokenGenerator(),
      test.getSqlStoreProvider(),
      test.getKvStoreProvider()
    );
  }

  @AfterEach
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
    Collection<AccountUser> accounts = Lists.newArrayList(accountUser);
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
