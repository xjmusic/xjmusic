// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.api.client.json.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubAccessTest {
  @Mock
  private ContainerRequestContext crc;

  @Test
  public void matchRoles() {
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "roles", "User,Artist"
    ));

    assertTrue(hubAccess.isAllowed(UserRoleType.User));
    assertTrue(hubAccess.isAllowed(UserRoleType.Artist));
    assertTrue(hubAccess.isAllowed(UserRoleType.User, UserRoleType.Artist));
    assertFalse(hubAccess.isAllowed(UserRoleType.Admin));
  }

  @Test
  public void createFromUserAuth_setsUserId() throws HubAccessException {
    User user = User.create();
    UserAuth userAuth = UserAuth.create(user, UserAuthType.Google);
    Collection<AccountUser> accountUsers = ImmutableSet.of();
    Collection<UserRole> userRoles = ImmutableSet.of();

    HubAccess result = HubAccess.create(userAuth, accountUsers, userRoles);

    assertEquals(user.getId(), result.getUserId());
  }

  @Test
  public void getUserId() throws HubAccessException {
    UUID id = UUID.randomUUID();
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "userId", id.toString()
    ));

    assertEquals(id, hubAccess.getUserId());
  }

  @Test
  public void fromContext() {
    HubAccess expectHubAccess = new HubAccess(ImmutableMap.of(
      "userId", UUID.randomUUID().toString()
    ));
    when(crc.getProperty(HubAccess.CONTEXT_KEY))
      .thenReturn(expectHubAccess);

    HubAccess actualHubAccess = HubAccess.fromContext(crc);

    assertEquals(expectHubAccess, actualHubAccess);
  }

  @Test
  public void toJSON() throws AppException {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(
      new HubAccessControlModule(),
      new DAOModule(),
      new HubIngestModule(),
      new HubPersistenceModule(),
      new MixerModule(),
      new JsonApiModule(),
      new FileStoreModule()));
    JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);

    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "userId", userId.toString(),
      "roles", "User,Artist",
      "accounts", accountId.toString()
    ));

    assertEquals(String.format("{\"roles\":\"User,Artist\",\"accounts\":\"%s\",\"userId\":\"%s\"}", accountId, userId), hubAccess.toJSON(jsonFactory));
  }

  @Test
  public void getAccounts() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "accounts", String.format("%s,%s,%s", id1, id2, id3)
    ));

    assertArrayEquals(new UUID[]{id1, id2, id3}, hubAccess.getAccountIds().toArray());
  }

  @Test
  public void isAdmin() {
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "roles", "User,Admin"
    ));

    assertTrue(hubAccess.isTopLevel());
  }

  @Test
  public void isAdmin_Not() {
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "roles", "User,Artist"
    ));

    assertFalse(hubAccess.isTopLevel());
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
    HubAccess hubAccess = HubAccess.create(userAuth, userAccountRoles, userRoles);

    assertEquals(ImmutableMap.of(
      "userAuthId", userAuth.getId().toString(),
      "userId", user.getId().toString(),
      "roles", "User,Artist",
      "accounts", accountUser.getAccountId().toString()
    ), hubAccess.toMap());
  }

  @Test
  public void valid() {
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "userAuthId", UUID.randomUUID().toString(),
      "userId", UUID.randomUUID().toString(),
      "roles", "User,Artist",
      "accounts", UUID.randomUUID().toString()
    ));

    assertTrue(hubAccess.isValid());
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void valid_evenWithNoAccounts() {
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "userAuthId", UUID.randomUUID().toString(),
      "userId", UUID.randomUUID().toString(),
      "roles", "User,Artist"
    ));

    assertTrue(hubAccess.isValid());
  }

  @Test
  public void valid_not() {
    HubAccess hubAccess = new HubAccess(ImmutableMap.of(
      "userAuthId", UUID.randomUUID().toString(),
      "userId", UUID.randomUUID().toString(),
      "accounts", UUID.randomUUID().toString()
    ));

    assertFalse(hubAccess.isValid());
  }

}
