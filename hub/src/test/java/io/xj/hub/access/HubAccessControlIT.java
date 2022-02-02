// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class HubAccessControlIT {

  private static final int STRESS_TEST_ITERATIONS = 100;
  HubAccessControlProvider hubAccessControlProvider;
  private HubIntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    var env = Environment.from(ImmutableMap.of(
      "REDIS_SESSION_NAMESPACE", "xj_session_test"
    ));

    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new FileStoreModule(), new JsonapiModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));

    test = injector.getInstance(HubIntegrationTestProvider.class);

    test.reset();

    hubAccessControlProvider = injector.getInstance(HubAccessControlProvider.class);
  }

  @After
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
    String TEST_TOKEN = hubAccessControlProvider.create(user, userAuth, accounts);

    // now stress test
    for (int i = 0; STRESS_TEST_ITERATIONS > i; i++) {
      HubAccess result = hubAccessControlProvider.get(TEST_TOKEN);
      assertTrue("Result is valid", result.isValid());
    }
  }

  @Test
  public void computeKey() {
    assertEquals("xj_session_test:*", hubAccessControlProvider.computeKey("*"));
  }

}
