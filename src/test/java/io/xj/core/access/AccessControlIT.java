// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.access.token.TokenGenerator;
import io.xj.core.app.AppConfiguration;
import io.xj.core.model.AccountUser;
import io.xj.core.model.UserAuth;
import io.xj.core.model.UserAuthType;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlIT {

  private static final int STRESS_TEST_ITERATIONS = 100;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  public TokenGenerator tokenGenerator;
  AccessControlProvider accessControlProvider;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));

    test = injector.getInstance(IntegrationTestProvider.class);

    test.reset();

    accessControlProvider = injector.getInstance(AccessControlProvider.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void get() throws Exception {
    when(tokenGenerator.generate())
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
    String TEST_TOKEN = accessControlProvider.create(userAuth, accounts, roles);

    // now stress test
    for (int i = 0; STRESS_TEST_ITERATIONS > i; i++) {
      Access result = accessControlProvider.get(TEST_TOKEN);
      assertTrue("Result is valid", result.isValid());
    }
  }

}
