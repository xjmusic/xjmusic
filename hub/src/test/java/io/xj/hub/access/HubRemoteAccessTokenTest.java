// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.manager.UserManager;
import io.xj.hub.manager.UserManagerImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubKvStoreProvider;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import javax.servlet.http.Cookie;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class HubRemoteAccessTokenTest {
  @Mock
  GoogleProvider googleProvider;

  @Mock
  HubSqlStoreProvider sqlStoreProvider;

  @Mock
  HubAccessTokenGenerator hubAccessTokenGenerator;

  @Mock
  HubKvStoreProvider hubKVStoreProvider;

  @Mock
  private Jedis redisClient;

  Account account1;
  Account account2;
  private UserManager userManager;
  private UserAuth userAuth;
  private Collection<AccountUser> accountUsers;
  private User user;
  private JsonapiPayloadFactory payloadFactory;

  private final AppEnvironment env = AppEnvironment.from(ImmutableMap.of(
    "ACCESS_TOKEN_DOMAIN", "com.coconuts",
    "ACCESS_TOKEN_MAX_AGE_SECONDS", "60",
    "ACCESS_TOKEN_NAME", "access_token_jammy",
    "ACCESS_TOKEN_PATH", "/dough",
    "REDIS_SESSION_NAMESPACE", "xj_session_test"
  ));

  @BeforeEach
  public void setUp() throws Exception {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    userManager = new UserManagerImpl(env, entityFactory, googleProvider, hubAccessTokenGenerator, sqlStoreProvider, hubKVStoreProvider);

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

  @AfterEach
  public void tearDown() {
    System.clearProperty("access.token.domain");
    System.clearProperty("access.token.path");
    System.clearProperty("access.token.max.age");
    System.clearProperty("access.token.name");
  }

  @Test
  public void create() throws Exception {
    when(hubKVStoreProvider.getClient())
      .thenReturn(redisClient);
    when(hubAccessTokenGenerator.generate())
      .thenReturn("token123");

    userManager.create(user, userAuth, accountUsers);

    HubAccess expectUserAccess = new HubAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setRoleTypes(ImmutableList.of(UserRoleType.User, UserRoleType.Artist))
      .setAccountIds(ImmutableSet.of(account1.getId(), account2.getId()));
    verify(redisClient).set("xj_session_test:token123", payloadFactory.serialize(expectUserAccess));
  }

  @Test
  public void newCookie() {
    Cookie cookie = userManager.newCookie("12345");

    assertEquals("access_token_jammy", cookie.getName());
    assertEquals("com.coconuts", cookie.getDomain());
    assertEquals("/dough", cookie.getPath());
    assertEquals("12345", cookie.getValue());
  }

}
