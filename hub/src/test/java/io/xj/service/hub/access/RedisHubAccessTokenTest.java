// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserRole;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.dao.DAOModule;
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
  private Collection<UserRole> roles;
  private User user;
  private PayloadFactory payloadFactory;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault()
      .withValue("access.tokenDomain", ConfigValueFactory.fromAnyRef("com.coconuts"))
      .withValue("access.tokenMaxAgeSeconds", ConfigValueFactory.fromAnyRef(60))
      .withValue("access.tokenName", ConfigValueFactory.fromAnyRef("access_token_jammy"))
      .withValue("access.tokenPath", ConfigValueFactory.fromAnyRef("/deez"));
    var injector = Guice.createInjector(Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule()).with(
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
    payloadFactory = injector.getInstance(PayloadFactory.class);

    user = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    userAuth = UserAuth.newBuilder()
      .setUserId(user.getId())
      .setId(UUID.randomUUID().toString())
      .build();
    account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    account2 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();

    var accountUser1 = AccountUser.newBuilder()
      .setAccountId(account1.getId())
      .build();
    var accountUser2 = AccountUser.newBuilder()
      .setAccountId(account2.getId())
      .build();
    accountUsers = List.of(accountUser1, accountUser2);

    UserRole role1 = UserRole.newBuilder()
      .setType(UserRole.Type.User)
      .build();
    UserRole role2 = UserRole.newBuilder()
      .setType(UserRole.Type.Artist)
      .build();
    roles = List.of(role1, role2);
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

    HubAccess expectUserAccess = new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setRoleTypes(ImmutableList.of(UserRole.Type.User, UserRole.Type.Artist))
      .setAccountIds(ImmutableSet.of(account1.getId(), account2.getId()));
    verify(redisClient).set("xj_session_test:token123", payloadFactory.serialize(expectUserAccess));
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
