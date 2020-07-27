// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubAccessControlIT {

  private static final int STRESS_TEST_ITERATIONS = 100;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  public HubAccessTokenGenerator hubAccessTokenGenerator;
  HubAccessControlProvider hubAccessControlProvider;
  private HubIntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new FileStoreModule(), new JsonApiModule(), new HubIntegrationTestModule()));

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
    when(hubAccessTokenGenerator.generate())
      .thenReturn("token123");
    // user auth
    UserAuth userAuth = new UserAuth();
    UUID userAuthId = UUID.randomUUID();
    userAuth.setId(userAuthId);
    UUID userId = UUID.randomUUID();
    userAuth.setUserId(userId);
    userAuth.setTypeEnum(UserAuthType.Google);
    userAuth.setExternalAccount("google");
    userAuth.setExternalAccessToken("google-token");
    // user role
    UserRole userRole = new UserRole();
    userRole.setUserId(userId);
    userRole.setTypeEnum(UserRoleType.User);
    // account user
    AccountUser accountUser = new AccountUser();
    accountUser.setUserId(userId);
    UUID accountId = UUID.randomUUID();
    accountUser.setAccountId(accountId);
    // access control provider
    Collection<AccountUser> accounts = Lists.newArrayList(accountUser);
    Collection<UserRole> roles = Lists.newArrayList(userRole);
    String TEST_TOKEN = hubAccessControlProvider.create(userAuth, accounts, roles);

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
