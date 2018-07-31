// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.AccessControlProviderImpl;
import io.xj.core.access.token.TokenGenerator;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_role.UserRole;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.persistence.redis.RedisDatabaseProvider;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import javax.ws.rs.core.Cookie;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JSONOutputProviderImplTest {
  @Mock TokenGenerator tokenGenerator;
  @Mock RedisDatabaseProvider redisDatabaseProvider;
  @Mock private Jedis redisClient;
  private Injector injector;
  private AccessControlProvider accessControlProvider;
  private UserAuth userAuth;
  private Collection<AccountUser> accounts;
  private Collection<UserRole> roles;

  @Before
  public void setUp() throws Exception {
    System.setProperty("access.token.domain", "com.manuts");
    System.setProperty("access.token.path", "/deez");
    System.setProperty("access.token.max.age", "60");
    System.setProperty("access.token.name", "access_token_jammy");
    createInjector();
    accessControlProvider = injector.getInstance(AccessControlProvider.class);

    userAuth = new UserAuth();
    userAuth.setUserId(BigInteger.valueOf(5609877L));
    userAuth.setId(BigInteger.valueOf(12363L));

    accounts = new LinkedList<>();
    AccountUser accountRole1 = new AccountUser();
    accountRole1.setAccountId(BigInteger.valueOf(790809874L));
    AccountUser accountRole2 = new AccountUser();
    accountRole2.setAccountId(BigInteger.valueOf(90888932L));
    accounts.add(accountRole1);
    accounts.add(accountRole2);

    roles = new LinkedList<>();
    UserRole role1 = new UserRole();
    role1.setTypeEnum(UserRoleType.User);
    UserRole role2 = new UserRole();
    role2.setTypeEnum(UserRoleType.Artist);
    roles.add(role1);
    roles.add(role2);
  }

  @After
  public void tearDown() throws Exception {
    userAuth = null;
    accounts = null;
    roles = null;
    System.clearProperty("access.token.domain");
    System.clearProperty("access.token.path");
    System.clearProperty("access.token.max.age");
    System.clearProperty("access.token.name");
  }

  @Test
  public void create() throws Exception {
    when(redisDatabaseProvider.getClient())
      .thenReturn(redisClient);
    when(tokenGenerator.generate())
      .thenReturn("token123");

    accessControlProvider.create(userAuth, accounts, roles);

    Map<String, String> expectUserAccess = Maps.newHashMap();
    expectUserAccess.put("userId", "5609877");
    expectUserAccess.put("userAuthId", "12363");
    expectUserAccess.put("roles", "User,Artist");
    expectUserAccess.put("accounts", "790809874,90888932");
    verify(redisClient).hmset("token123", expectUserAccess);
  }

  @Test
  public void newCookie() throws Exception {
    Cookie cookie = accessControlProvider.newCookie("12345");

    assertEquals("access_token_jammy", cookie.getName());
    assertEquals("com.manuts", cookie.getDomain());
    assertEquals("/deez", cookie.getPath());
    assertEquals("12345", cookie.getValue());
  }

  @Test
  public void expire() throws Exception {

  }

  @Test
  public void get() throws Exception {

  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(TokenGenerator.class).toInstance(tokenGenerator);
          bind(RedisDatabaseProvider.class).toInstance(redisDatabaseProvider);
          bind(AccessControlProvider.class).to(AccessControlProviderImpl.class);
          bind(JsonFactory.class).to(JacksonFactory.class);
        }
      }));
  }

}
