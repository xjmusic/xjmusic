// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import com.google.common.collect.Lists;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.access.token.TokenGenerator;
import io.xj.core.dao.UserDAO;
import io.xj.core.external.google.GoogleProvider;
import io.xj.core.integration.IntegrationTestEntity;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRole;
import io.xj.core.model.user_role.UserRoleType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlIT {
  private static final int STRESS_TEST_ITERATIONS = 100;
  private AccessControlProvider accessControlProvider;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  public TokenGenerator tokenGenerator;
  @Mock
  public GoogleProvider googleProvider;
  @Mock
  public UserDAO userDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    Injector injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(TokenGenerator.class).toInstance(tokenGenerator);
          bind(GoogleProvider.class).toInstance(googleProvider);
          bind(UserDAO.class).toInstance(userDAO);
        }
      }));

    accessControlProvider = injector.getInstance(AccessControlProvider.class);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void get() throws Exception {
    when(tokenGenerator.generate())
      .thenReturn("token123");
    // user auth
    UserAuth userAuth = new UserAuth();
    BigInteger TEST_USER_AUTH_ID = BigInteger.valueOf(64);
    userAuth.setId(TEST_USER_AUTH_ID);
    BigInteger TEST_USER_ID = BigInteger.valueOf(1337);
    userAuth.setUserId(TEST_USER_ID);
    userAuth.setTypeEnum(UserAuthType.Google);
    userAuth.setExternalAccount("google");
    userAuth.setExternalAccessToken("google-token");
    // user role
    UserRole userRole = new UserRole();
    userRole.setUserId(TEST_USER_ID);
    userRole.setTypeEnum(UserRoleType.User);
    // account user
    AccountUser accountUser = new AccountUser();
    accountUser.setUserId(TEST_USER_ID);
    BigInteger TEST_ACCOUNT_ID = BigInteger.valueOf(76);
    accountUser.setAccountId(TEST_ACCOUNT_ID);
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
