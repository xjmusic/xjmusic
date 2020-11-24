// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.xj.Account;
import io.xj.User;
import io.xj.UserAuth;
import io.xj.UserRole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.UUID;

import static io.xj.service.hub.HubContentFixtures.buildHubClientAccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubClientAccessTest {
  @Mock
  private ContainerRequestContext crc;

  @Test
  public void matchRoles() {
    HubClientAccess access = buildHubClientAccess("User,Artist");

    assertTrue(access.isAllowed(UserRole.Type.User));
    assertTrue(access.isAllowed(UserRole.Type.Artist));
    assertTrue(access.isAllowed(UserRole.Type.User, UserRole.Type.Artist));
    assertFalse(access.isAllowed(UserRole.Type.Admin));
  }

  @Test
  public void createFromUserAuth_setsUserId() throws HubClientException {
    User user = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    UserAuth userAuth = UserAuth.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(user.getId())
      .setType(UserAuth.Type.Google)
      .build();

    HubClientAccess result = buildHubClientAccess(user, userAuth, ImmutableList.of(), "User,Admin");

    assertEquals(user.getId(), result.getUserId());
  }

  @Test
  public void getUserId() throws HubClientException {
    User user = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubClientAccess access = buildHubClientAccess(user, "User");

    assertEquals(user.getId(), access.getUserId());
  }

  @Test
  public void fromContext() {
    User user = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubClientAccess expectAccess = buildHubClientAccess(user, "User");
    when(crc.getProperty(HubClientAccess.CONTEXT_KEY))
      .thenReturn(expectAccess);

    HubClientAccess actualAccess = HubClientAccess.fromContext(crc);

    assertEquals(expectAccess, actualAccess);
  }

  @Test
  public void toJSON() throws JsonProcessingException {
    User user = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var account = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubClientAccess access = buildHubClientAccess(user, ImmutableList.of(account), "User,Artist");

    String result = new ObjectMapper().writeValueAsString(access);
    assertEquals(String.format("{\"roleTypes\":[\"User\",\"Artist\"],\"accountIds\":[\"%s\"],\"token\":null,\"userId\":\"%s\",\"userAuthId\":null,\"roles\":\"User,Artist\"}", account.getId(), user.getId()), result);
  }

  @Test
  public void getAccountIds() {
    var account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var account2 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var account3 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1, account2, account3), "User");

    assertTrue(access.getAccountIds().contains(account1.getId()));
    assertTrue(access.getAccountIds().contains(account2.getId()));
    assertTrue(access.getAccountIds().contains(account3.getId()));
  }

  @Test
  public void isAdmin() {
    HubClientAccess access = buildHubClientAccess("User,Admin");

    assertTrue(access.isTopLevel());
  }

  @Test
  public void isAdmin_Not() {
    HubClientAccess access = buildHubClientAccess("User,Artist");

    assertFalse(access.isTopLevel());
  }

  @Test
  public void valid() {
    User user = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var account = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubClientAccess access = buildHubClientAccess(
      user,
      UserAuth.newBuilder()
        .setUserId(user.getId())
        .setType(UserAuth.Type.Google)
        .build(),
      ImmutableList.of(account),
      "User,Artist");

    assertTrue(access.isValid());
  }

  @Test
  public void valid_not() {
    User user = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var account = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubClientAccess access = buildHubClientAccess(
      user,
      UserAuth.newBuilder()
        .setUserId(user.getId())
        .setType(UserAuth.Type.Google)
        .build(),
      ImmutableList.of(account));

    assertFalse(access.isValid());
  }

}
