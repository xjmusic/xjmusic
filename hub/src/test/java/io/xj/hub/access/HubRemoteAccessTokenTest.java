// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.manager.UserManager;
import io.xj.hub.manager.UserManagerImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.kv.HubKvStoreProvider;
import io.xj.hub.persistence.kv.HubKvStoreProviderImpl;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProviderImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HubRemoteAccessTokenTest {
  @Mock
  GoogleProvider googleProvider;

  @Mock
  HubSqlStoreProvider sqlStoreProvider;

  @Mock
  HubAccessTokenGenerator hubAccessTokenGenerator;

  HubKvStoreProvider hubKVStoreProvider;

  Account account1;
  Account account2;
  private UserManager userManager;
  private UserAuth userAuth;
  private Collection<AccountUser> accountUsers;
  private User user;

  @BeforeEach
  public void setUp() throws Exception {
    hubKVStoreProvider = new HubKvStoreProviderImpl(new EntityFactoryImpl(new JsonProviderImpl()), "", 1);

    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    userManager = new UserManagerImpl(entityFactory, googleProvider, hubAccessTokenGenerator, sqlStoreProvider, hubKVStoreProvider, "xj_session_test", "access_token_jammy", "com.coconuts", "/dough", 1, "token123");

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
  }

  @Test
  public void create() throws Exception {
    when(hubAccessTokenGenerator.generate())
      .thenReturn("token123");

    userManager.create(user, userAuth, accountUsers);

    var result = hubKVStoreProvider.get(HubAccess.class, "xj_session_test:token123");
    Assertions.assertNotNull(result);
    assertEquals(user.getId(), result.getUserId());
    assertEquals(userAuth.getId(), result.getUserAuthId());
    assertArrayEquals(ImmutableList.of(UserRoleType.User, UserRoleType.Artist).toArray(), result.getRoleTypes().toArray());
    assertArrayEquals(ImmutableSet.of(account1.getId(), account2.getId()).toArray(), result.getAccountIds().toArray());
  }

  @Test
  public void get() throws Exception {
    when(hubAccessTokenGenerator.generate())
      .thenReturn("token123");
    userManager.create(user, userAuth, accountUsers);

    var result = hubKVStoreProvider.get(HubAccess.class, "xj_session_test:token123");

    Assertions.assertNotNull(result);
    assertEquals(user.getId(), result.getUserId());
  }

  @Test
  public void get_nullIfNotFound() throws Exception {
    when(hubAccessTokenGenerator.generate())
      .thenReturn("token123");
    userManager.create(user, userAuth, accountUsers);

    var actual = hubKVStoreProvider.get(HubAccess.class, "xj_session_test:token456");

    Assertions.assertNull(actual);
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
