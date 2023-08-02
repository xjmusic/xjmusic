// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import java.util.Set;
import io.xj.hub.enums.UserRoleType;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
public class HubAccessTest {
  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  HttpServletRequest req;

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
    when(req.getAttribute(HubAccess.CONTEXT_KEY)).thenReturn(expectHubAccess);

    HubAccess actualHubAccess = HubAccess.fromRequest(req);

    assertEquals(expectHubAccess, actualHubAccess);
  }

  @Test
  public void serialize() throws JsonapiException {
    JsonProviderImpl jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);

    var userId = UUID.randomUUID();
    var accountId = UUID.randomUUID();
    HubAccess access = HubAccess.create("User,Artist")
      .setUserId(userId)
      .setAccountIds(List.of(accountId));

    assertEquals(String.format("{\"roleTypes\":[\"User\",\"Artist\"],\"accountIds\":[\"%s\"],\"userId\":\"%s\"}", accountId, userId), payloadFactory.serialize(access));
  }

  @Test
  public void getAccounts() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    HubAccess access = new HubAccess()
      .setAccountIds(Set.of(id1, id2, id3));

    var result = access.getAccountIds();

    assertEquals(3, result.size());
    assertTrue(result.contains(id1));
    assertTrue(result.contains(id2));
    assertTrue(result.contains(id3));
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
      .setAccountIds(List.of(UUID.randomUUID()));

    assertTrue(access.isValid());
  }

  /**
   * User expects to log in without having access to any accounts. https://www.pivotaltracker.com/story/show/154580129
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
      .setAccountIds(List.of(UUID.randomUUID()));

    assertFalse(access.isValid());
  }

}
