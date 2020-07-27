// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.persistence.HubRedisProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
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
public class RedisHubAccessTokenTest {
  @Mock
  HubAccessTokenGenerator hubAccessTokenGenerator;
  @Mock
  HubRedisProvider hubRedisProvider;
  Account account1;
  Account account2;
  @Mock
  private Jedis redisClient;
  private HubAccessControlProvider hubAccessControlProvider;
  private UserAuth userAuth;
  private Collection<AccountUser> accountUsers;
  private Collection<UserRole> roles;
  private User user;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault()
      .withValue("access.tokenDomain", ConfigValueFactory.fromAnyRef("com.coconuts"))
      .withValue("access.tokenMaxAgeSeconds", ConfigValueFactory.fromAnyRef(60))
      .withValue("access.tokenName", ConfigValueFactory.fromAnyRef("access_token_jammy"))
      .withValue("access.tokenPath", ConfigValueFactory.fromAnyRef("/deez"));
    Injector injector = Guice.createInjector(Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Config.class).toInstance(config);
          bind(HubAccessTokenGenerator.class).toInstance(hubAccessTokenGenerator);
          bind(HubRedisProvider.class).toInstance(hubRedisProvider);
          bind(HubAccessControlProvider.class).to(HubAccessControlProviderImpl.class);
          bind(JsonFactory.class).to(JacksonFactory.class);
        }
      }));
    hubAccessControlProvider = injector.getInstance(HubAccessControlProvider.class);

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
    when(hubRedisProvider.getClient())
      .thenReturn(redisClient);
    when(hubAccessTokenGenerator.generate())
      .thenReturn("token123");

    hubAccessControlProvider.create(userAuth, accountUsers, roles);

    Map<String, String> expectUserAccess = Maps.newHashMap();
    expectUserAccess.put("userId", user.getId().toString());
    expectUserAccess.put("userAuthId", userAuth.getId().toString());
    expectUserAccess.put("roles", "User,Artist");
    expectUserAccess.put("accounts", account1.getId().toString() + "," + account2.getId().toString());
    verify(redisClient).hmset("xj_session_test:token123", expectUserAccess);
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
    Cookie cookie = hubAccessControlProvider.newCookie("12345");

    assertEquals("access_token_jammy", cookie.getName());
    assertEquals("com.coconuts", cookie.getDomain());
    assertEquals("/deez", cookie.getPath());
    assertEquals("12345", cookie.getValue());
  }

}
