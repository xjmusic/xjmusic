// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.entity.AccountUser;
import io.xj.service.hub.entity.User;
import io.xj.service.hub.entity.UserAuth;
import io.xj.service.hub.entity.UserAuthType;
import io.xj.service.hub.entity.UserRole;
import io.xj.service.hub.entity.UserRoleType;
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubAccessTest {
  @Mock
  private ContainerRequestContext crc;

  @Test
  public void matchRoles() {
    HubAccess hubAccess = HubAccess.create("User,Artist");

    assertTrue(hubAccess.isAllowed(UserRoleType.User));
    assertTrue(hubAccess.isAllowed(UserRoleType.Artist));
    assertTrue(hubAccess.isAllowed(UserRoleType.User, UserRoleType.Artist));
    assertFalse(hubAccess.isAllowed(UserRoleType.Admin));
  }

  @Test
  public void createFromUserAuth_setsUserId() {
    User user = User.create();
    UserAuth userAuth = UserAuth.create(user, UserAuthType.Google);
    Collection<AccountUser> accountUsers = ImmutableSet.of();
    Collection<UserRole> userRoles = ImmutableSet.of();

    HubAccess result = HubAccess.create(userAuth, accountUsers, userRoles);

    assertEquals(user.getId(), result.getUserId());
  }

  @Test
  public void getUserId() {
    UUID id = UUID.randomUUID();
    HubAccess hubAccess = new HubAccess().setUserId(id);

    assertEquals(id, hubAccess.getUserId());
  }

  @Test
  public void fromContext() {
    HubAccess expectHubAccess = new HubAccess().setUserId(UUID.randomUUID());
    when(crc.getProperty(HubAccess.CONTEXT_KEY))
      .thenReturn(expectHubAccess);

    HubAccess actualHubAccess = HubAccess.fromContext(crc);

    assertEquals(expectHubAccess, actualHubAccess);
  }

  @Test
  public void serialize() throws AppException, EntityException {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(
      new HubAccessControlModule(),
      new DAOModule(),
      new HubIngestModule(),
      new HubPersistenceModule(),
      new MixerModule(),
      new JsonApiModule(),
      new FileStoreModule()));
    EntityFactory entityFactory = injector.getInstance(EntityFactory.class);

    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    HubAccess hubAccess = HubAccess.create("User,Artist")
      .setUserId(userId)
      .setAccountIds(ImmutableList.of(accountId));

    assertEquals(String.format("{\"roleTypes\":[\"User\",\"Artist\"],\"accountIds\":[\"%s\"],\"userId\":\"%s\",\"userAuthId\":null}", accountId, userId), entityFactory.serialize(hubAccess));
  }

  @Test
  public void getAccounts() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    HubAccess hubAccess = new HubAccess()
      .setAccountIds(ImmutableSet.of(id1, id2, id3));

    assertArrayEquals(new UUID[]{id1, id2, id3}, hubAccess.getAccountIds().toArray());
  }

  @Test
  public void isAdmin() {
    HubAccess hubAccess = HubAccess.create("User,Admin");

    assertTrue(hubAccess.isTopLevel());
  }

  @Test
  public void isAdmin_Not() {
    HubAccess hubAccess = HubAccess.create("User,Artist");

    assertFalse(hubAccess.isTopLevel());
  }

  @Test
  public void valid() {
    HubAccess hubAccess = HubAccess.create("User,Artist")
      .setUserId(UUID.randomUUID())
      .setUserAuthId(UUID.randomUUID())
      .setAccountIds(ImmutableList.of(UUID.randomUUID()));

    assertTrue(hubAccess.isValid());
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void valid_evenWithNoAccounts() {
    HubAccess hubAccess = HubAccess.create("User,Artist")
      .setUserId(UUID.randomUUID())
      .setUserAuthId(UUID.randomUUID());

    assertTrue(hubAccess.isValid());
  }

  @Test
  public void valid_not() {
    HubAccess hubAccess = new HubAccess()
      .setUserId(UUID.randomUUID())
      .setUserAuthId(UUID.randomUUID())
      .setAccountIds(ImmutableList.of(UUID.randomUUID()));

    assertFalse(hubAccess.isValid());
  }

}
