// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.persistence.HubRedisProvider;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import javax.ws.rs.core.Cookie;
import java.util.Collection;
import java.util.List;
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
  private User user;
  private JsonapiPayloadFactory payloadFactory;

  @Before
  public void setUp() throws Exception {
    var env = Environment.from(ImmutableMap.of(
      "ACCESS_TOKEN_DOMAIN", "com.coconuts",
      "ACCESS_TOKEN_MAX_AGE_SECONDS", "60",
      "ACCESS_TOKEN_NAME", "access_token_jammy",
      "ACCESS_TOKEN_PATH", "/dough",
      "REDIS_SESSION_NAMESPACE", "xj_session_test"
    ));
    var injector = Guice.createInjector(Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
          bind(HubAccessTokenGenerator.class).toInstance(hubAccessTokenGenerator);
          bind(HubRedisProvider.class).toInstance(hubRedisProvider);
          bind(HubAccessControlProvider.class).to(HubAccessControlProviderImpl.class);
        }
      }));
    hubAccessControlProvider = injector.getInstance(HubAccessControlProvider.class);
    payloadFactory = injector.getInstance(JsonapiPayloadFactory.class);

    user = new User();
    user.setId(UUID.randomUUID());
    user.setRoles("User, Artist");

    userAuth = new UserAuth();
    userAuth.setId(UUID.randomUUID());
    userAuth.setUserId(user.getId());

    account1 = new Account();
    account1.setId(UUID.randomUUID());

    account2 = new Account();
    account2.setId(UUID.randomUUID());

    var accountUser1 = new AccountUser();
    accountUser1.setAccountId(account1.getId());

    var accountUser2 = new AccountUser();
    accountUser2.setAccountId(account2.getId());

    accountUsers = List.of(accountUser1, accountUser2);
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

    hubAccessControlProvider.create(user, userAuth, accountUsers);

    HubAccess expectUserAccess = new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setRoleTypes(ImmutableList.of(UserRoleType.User, UserRoleType.Artist))
      .setAccountIds(ImmutableSet.of(account1.getId(), account2.getId()));
    verify(redisClient).set("xj_session_test:token123", payloadFactory.serialize(expectUserAccess));
  }

  @Test
  public void newCookie() {
    Cookie cookie = hubAccessControlProvider.newCookie("12345");

    assertEquals("access_token_jammy", cookie.getName());
    assertEquals("com.coconuts", cookie.getDomain());
    assertEquals("/dough", cookie.getPath());
    assertEquals("12345", cookie.getValue());
  }

}
