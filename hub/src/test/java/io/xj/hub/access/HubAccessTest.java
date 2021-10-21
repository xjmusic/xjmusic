// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.UUID;

import static org.junit.Assert.*;
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
  public void serialize() throws JsonapiException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(),
      new DAOModule(),
      new HubIngestModule(),
      new HubPersistenceModule(),
      new JsonapiModule(),
      new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    JsonapiPayloadFactory payloadFactory = injector.getInstance(JsonapiPayloadFactory.class);

    var userId = UUID.randomUUID();
    var accountId = UUID.randomUUID();
    HubAccess hubAccess = HubAccess.create("User,Artist")
      .setUserId(userId)
      .setAccountIds(ImmutableList.of(accountId));

    assertEquals(String.format("{\"roleTypes\":[\"User\",\"Artist\"],\"accountIds\":[\"%s\"],\"userId\":\"%s\"}", accountId, userId), payloadFactory.serialize(hubAccess));
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
   [#154580129] User expects to log in without having access to any accounts.
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
