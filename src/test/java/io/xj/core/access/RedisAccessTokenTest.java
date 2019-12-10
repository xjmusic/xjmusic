// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.core.CoreModule;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.User;
import io.xj.core.model.UserAuth;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.persistence.RedisDatabaseProvider;
import io.xj.core.testing.AppTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import javax.ws.rs.core.Cookie;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedisAccessTokenTest {
  @Mock
  TokenGenerator tokenGenerator;
  @Mock
  RedisDatabaseProvider redisDatabaseProvider;
  Account account1;
  Account account2;
  @Mock
  private Jedis redisClient;
  private AccessControlProvider accessControlProvider;
  private UserAuth userAuth;
  private Collection<AccountUser> accountUsers;
  private Collection<UserRole> roles;
  private User user;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault()
      .withValue("access.tokenDomain", ConfigValueFactory.fromAnyRef("com.coconuts"))
      .withValue("access.tokenMaxAgeSeconds", ConfigValueFactory.fromAnyRef(60))
      .withValue("access.tokenName", ConfigValueFactory.fromAnyRef("access_token_jammy"))
      .withValue("access.tokenPath", ConfigValueFactory.fromAnyRef("/deez"));
    Injector injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Config.class).toInstance(config);
          bind(TokenGenerator.class).toInstance(tokenGenerator);
          bind(RedisDatabaseProvider.class).toInstance(redisDatabaseProvider);
          bind(AccessControlProvider.class).to(AccessControlProviderImpl.class);
          bind(JsonFactory.class).to(JacksonFactory.class);
        }
      }));
    accessControlProvider = injector.getInstance(AccessControlProvider.class);

    user = User.create();

    userAuth = new UserAuth();
    userAuth.setUserId(user.getId());
    userAuth.setId(UUID.randomUUID());

    account1 = Account.create();
    account2 = Account.create();

    accountUsers = new LinkedList<>();
    AccountUser accountUser1 = new AccountUser();
    accountUser1.setAccountId(account1.getId());
    AccountUser accountUser2 = new AccountUser();
    accountUser2.setAccountId(account2.getId());
    accountUsers.add(accountUser1);
    accountUsers.add(accountUser2);

    roles = new LinkedList<>();
    UserRole role1 = new UserRole();
    role1.setTypeEnum(UserRoleType.User);
    UserRole role2 = new UserRole();
    role2.setTypeEnum(UserRoleType.Artist);
    roles.add(role1);
    roles.add(role2);
  }

  @After
  public void tearDown() {
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

    accessControlProvider.create(userAuth, accountUsers, roles);

    Map<String, String> expectUserAccess = Maps.newHashMap();
    expectUserAccess.put("userId", user.getId().toString());
    expectUserAccess.put("userAuthId", userAuth.getId().toString());
    expectUserAccess.put("roles", "User,Artist");
    expectUserAccess.put("accounts", account1.getId().toString() + "," + account2.getId().toString());
    verify(redisClient).hmset("token123", expectUserAccess);
    /*
        userAuth.setUserId(BigInteger.valueOf(5609877L));
    userAuth.setId(BigInteger.valueOf(12363L));

    accounts = new LinkedList<>();
    AccountUser accountRole1 = new AccountUser();
    accountRole1.setAccountId(BigInteger.valueOf(790809874L));
    AccountUser accountRole2 = new AccountUser();
    accountRole2.setAccountId(BigInteger.valueOf(90888932L));

     */
  }

  @Test
  public void newCookie() {
    Cookie cookie = accessControlProvider.newCookie("12345");

    assertEquals("access_token_jammy", cookie.getName());
    assertEquals("com.coconuts", cookie.getDomain());
    assertEquals("/deez", cookie.getPath());
    assertEquals("12345", cookie.getValue());
  }

}
