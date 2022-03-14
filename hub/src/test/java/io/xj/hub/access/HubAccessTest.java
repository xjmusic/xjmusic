// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.manager.ManagerModule;
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
    HubAccess access = HubAccess.create("User,Artist");

    assertTrue(access.isAnyAllowed(UserRoleType.User));
    assertTrue(access.isAnyAllowed(UserRoleType.Artist));
    assertTrue(access.isAnyAllowed(UserRoleType.User, UserRoleType.Artist));
    assertFalse(access.isAnyAllowed(UserRoleType.Admin));
  }

  @Test
  public void getUserId() {
    UUID id = UUID.randomUUID();
    HubAccess access = new HubAccess().setUserId(id);

    assertEquals(id, access.getUserId());
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
      new ManagerModule(),
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
    HubAccess access = HubAccess.create("User,Artist")
      .setUserId(userId)
      .setAccountIds(ImmutableList.of(accountId));

    assertEquals(String.format("{\"roleTypes\":[\"User\",\"Artist\"],\"accountIds\":[\"%s\"],\"userId\":\"%s\"}", accountId, userId), payloadFactory.serialize(access));
  }

  @Test
  public void getAccounts() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    HubAccess access = new HubAccess()
      .setAccountIds(ImmutableSet.of(id1, id2, id3));

    assertArrayEquals(new UUID[]{id1, id2, id3}, access.getAccountIds().toArray());
  }

  @Test
  public void isAdmin() {
    HubAccess access = HubAccess.create("User,Admin");

    assertTrue(access.isTopLevel());
  }

  @Test
  public void isAdmin_Not() {
    HubAccess access = HubAccess.create("User,Artist");

    assertFalse(access.isTopLevel());
  }

  @Test
  public void valid() {
    HubAccess access = HubAccess.create("User,Artist")
      .setUserId(UUID.randomUUID())
      .setUserAuthId(UUID.randomUUID())
      .setAccountIds(ImmutableList.of(UUID.randomUUID()));

    assertTrue(access.isValid());
  }

  /**
   [#154580129] User expects to log in without having access to any accounts.
   */
  @Test
  public void valid_evenWithNoAccounts() {
    HubAccess access = HubAccess.create("User,Artist")
      .setUserId(UUID.randomUUID())
      .setUserAuthId(UUID.randomUUID());

    assertTrue(access.isValid());
  }

  @Test
  public void valid_not() {
    HubAccess access = new HubAccess()
      .setUserId(UUID.randomUUID())
      .setUserAuthId(UUID.randomUUID())
      .setAccountIds(ImmutableList.of(UUID.randomUUID()));

    assertFalse(access.isValid());
  }

}
