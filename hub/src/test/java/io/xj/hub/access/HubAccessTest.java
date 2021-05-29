// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import io.xj.UserRole;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.testing.HubTestConfiguration;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.mixer.MixerModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
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

    assertTrue(hubAccess.isAllowed(UserRole.Type.User));
    assertTrue(hubAccess.isAllowed(UserRole.Type.Artist));
    assertTrue(hubAccess.isAllowed(UserRole.Type.User, UserRole.Type.Artist));
    assertFalse(hubAccess.isAllowed(UserRole.Type.Admin));
  }

  @Test
  public void getUserId() {
    String id = UUID.randomUUID().toString();
    HubAccess hubAccess = new HubAccess().setUserId(id);

    assertEquals(id, hubAccess.getUserId());
  }

  @Test
  public void fromContext() {
    HubAccess expectHubAccess = new HubAccess().setUserId(UUID.randomUUID().toString());
    when(crc.getProperty(HubAccess.CONTEXT_KEY))
      .thenReturn(expectHubAccess);

    HubAccess actualHubAccess = HubAccess.fromContext(crc);

    assertEquals(expectHubAccess, actualHubAccess);
  }

  @Test
  public void serialize() throws AppException, JsonApiException {
    Config config = HubTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of(
      new HubAccessControlModule(),
      new DAOModule(),
      new HubIngestModule(),
      new HubPersistenceModule(),
      new MixerModule(),
      new JsonApiModule(),
      new FileStoreModule()));
    PayloadFactory payloadFactory = injector.getInstance(PayloadFactory.class);

    String userId = UUID.randomUUID().toString();
    String accountId = UUID.randomUUID().toString();
    HubAccess hubAccess = HubAccess.create("User,Artist")
      .setUserId(userId)
      .setAccountIds(ImmutableList.of(accountId));

    assertEquals(String.format("{\"roleTypes\":[\"User\",\"Artist\"],\"accountIds\":[\"%s\"],\"userId\":\"%s\",\"userAuthId\":null}", accountId, userId), payloadFactory.serialize(hubAccess));
  }

  @Test
  public void getAccounts() {
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();
    String id3 = UUID.randomUUID().toString();
    HubAccess hubAccess = new HubAccess()
      .setAccountIds(ImmutableSet.of(id1, id2, id3));

    assertArrayEquals(new String[]{id1, id2, id3}, hubAccess.getAccountIds().toArray());
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
      .setUserId(UUID.randomUUID().toString())
      .setUserAuthId(UUID.randomUUID().toString())
      .setAccountIds(ImmutableList.of(UUID.randomUUID().toString()));

    assertTrue(hubAccess.isValid());
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void valid_evenWithNoAccounts() {
    HubAccess hubAccess = HubAccess.create("User,Artist")
      .setUserId(UUID.randomUUID().toString())
      .setUserAuthId(UUID.randomUUID().toString());

    assertTrue(hubAccess.isValid());
  }

  @Test
  public void valid_not() {
    HubAccess hubAccess = new HubAccess()
      .setUserId(UUID.randomUUID().toString())
      .setUserAuthId(UUID.randomUUID().toString())
      .setAccountIds(ImmutableList.of(UUID.randomUUID().toString()));

    assertFalse(hubAccess.isValid());
  }

}
