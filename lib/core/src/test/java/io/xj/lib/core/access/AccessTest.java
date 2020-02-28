// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.access;

import com.google.api.client.json.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Account;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.model.User;
import io.xj.lib.core.model.UserAuth;
import io.xj.lib.core.model.UserRole;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.testing.AppTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessTest {
  @Mock
  private ContainerRequestContext crc;

  @Test
  public void matchRoles() {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist"
    ));

    assertTrue(access.isAllowed(UserRoleType.User));
    assertTrue(access.isAllowed(UserRoleType.Artist));
    assertTrue(access.isAllowed(UserRoleType.User, UserRoleType.Artist));
    assertFalse(access.isAllowed(UserRoleType.Admin));
  }

  @Test
  public void getUserId() throws CoreException {
    UUID id = UUID.randomUUID();
    Access access = new Access(ImmutableMap.of(
      "userId", id.toString()
    ));

    assertEquals(id, access.getUserId());
  }

  @Test
  public void fromContext() {
    Access expectAccess = new Access(ImmutableMap.of(
      "userId", UUID.randomUUID().toString()
    ));
    when(crc.getProperty(Access.CONTEXT_KEY))
      .thenReturn(expectAccess);

    Access actualAccess = Access.fromContext(crc);

    assertEquals(expectAccess, actualAccess);
  }

  @Test
  public void toJSON() {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);

    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Access access = new Access(ImmutableMap.of(
      "userId", userId.toString(),
      "roles", "User,Artist",
      "accounts", accountId.toString()
    ));

    assertEquals(String.format("{\"roles\":\"User,Artist\",\"accounts\":\"%s\",\"userId\":\"%s\"}", accountId, userId), access.toJSON(jsonFactory));
  }

  @Test
  public void getAccounts() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    Access access = new Access(ImmutableMap.of(
      "accounts", String.format("%s,%s,%s", id1, id2, id3)
    ));

    assertArrayEquals(new UUID[]{id1, id2, id3}, access.getAccountIds().toArray());
  }

  @Test
  public void isAdmin() {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Admin"
    ));

    assertTrue(access.isTopLevel());
  }

  @Test
  public void isAdmin_Not() {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist"
    ));

    assertFalse(access.isTopLevel());
  }

  @Test
  public void intoMap() {
    User user = User.create();
    Account account = Account.create();
    UserAuth userAuth = UserAuth.create();
    userAuth.setUserId(user.getId());
    AccountUser accountUser = AccountUser.create(account, user);
    Collection<AccountUser> userAccountRoles = ImmutableList.of(accountUser);
    UserRole userRole1 = UserRole.create(user, UserRoleType.User);
    UserRole userRole2 = UserRole.create(user, UserRoleType.Artist);
    Collection<UserRole> userRoles = ImmutableList.of(userRole1, userRole2);
    Access access = new Access(userAuth, userAccountRoles, userRoles);

    assertEquals(ImmutableMap.of(
      "userAuthId", userAuth.getId().toString(),
      "userId", user.getId().toString(),
      "roles", "User,Artist",
      "accounts", accountUser.getAccountId().toString()
    ), access.toMap());
  }

  @Test
  public void valid() {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", UUID.randomUUID().toString(),
      "userId", UUID.randomUUID().toString(),
      "roles", "User,Artist",
      "accounts", UUID.randomUUID().toString()
    ));

    assertTrue(access.isValid());
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void valid_evenWithNoAccounts() {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", UUID.randomUUID().toString(),
      "userId", UUID.randomUUID().toString(),
      "roles", "User,Artist"
    ));

    assertTrue(access.isValid());
  }

  @Test
  public void valid_not() {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", UUID.randomUUID().toString(),
      "userId", UUID.randomUUID().toString(),
      "accounts", UUID.randomUUID().toString()
    ));

    assertFalse(access.isValid());
  }

}
