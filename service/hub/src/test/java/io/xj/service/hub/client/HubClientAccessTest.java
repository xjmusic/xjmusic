// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.xj.service.hub.entity.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubClientAccessTest {
  @Mock
  private ContainerRequestContext crc;

  @Test
  public void matchRoles() {
    HubClientAccess access = HubClientAccess.create("User,Artist");

    assertTrue(access.isAllowed(UserRoleType.User));
    assertTrue(access.isAllowed(UserRoleType.Artist));
    assertTrue(access.isAllowed(UserRoleType.User, UserRoleType.Artist));
    assertFalse(access.isAllowed(UserRoleType.Admin));
  }

  @Test
  public void createFromUserAuth_setsUserId() throws HubClientException {
    User user = User.create();
    UserAuth userAuth = UserAuth.create(user, UserAuthType.Google);

    HubClientAccess result = HubClientAccess.create(user, userAuth, ImmutableList.of(), "User,Admin");

    assertEquals(user.getId(), result.getUserId());
  }

  @Test
  public void getUserId() throws HubClientException {
    User user = User.create();
    HubClientAccess access = HubClientAccess.create(user, "User");

    assertEquals(user.getId(), access.getUserId());
  }

  @Test
  public void fromContext() {
    HubClientAccess expectAccess = HubClientAccess.create(User.create(), "User");
    when(crc.getProperty(HubClientAccess.CONTEXT_KEY))
      .thenReturn(expectAccess);

    HubClientAccess actualAccess = HubClientAccess.fromContext(crc);

    assertEquals(expectAccess, actualAccess);
  }

  @Test
  public void toJSON() throws JsonProcessingException {
    User user = User.create();
    Account account = Account.create();
    HubClientAccess access = HubClientAccess.create(user, ImmutableList.of(account), "User,Artist");

    String result = new ObjectMapper().writeValueAsString(access);
    assertEquals(String.format("{\"roleTypes\":[\"User\",\"Artist\"],\"accountIds\":[\"%s\"],\"token\":null,\"userId\":\"%s\",\"userAuthId\":null,\"roles\":\"User,Artist\"}", account.getId(), user.getId()), result);
  }

  @Test
  public void getAccountIds() {
    Account account1 = Account.create();
    Account account2 = Account.create();
    Account account3 = Account.create();
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1, account2, account3), "User");

    assertTrue(access.getAccountIds().contains(account1.getId()));
    assertTrue(access.getAccountIds().contains(account2.getId()));
    assertTrue(access.getAccountIds().contains(account3.getId()));
  }

  @Test
  public void isAdmin() {
    HubClientAccess access = HubClientAccess.create("User,Admin");

    assertTrue(access.isTopLevel());
  }

  @Test
  public void isAdmin_Not() {
    HubClientAccess access = HubClientAccess.create("User,Artist");

    assertFalse(access.isTopLevel());
  }

  @Test
  public void valid() {
    HubClientAccess access = HubClientAccess.create(
      User.create(),
      UserAuth.create(User.create(), UserAuthType.Google),
      ImmutableList.of(Account.create()),
      "User,Artist");

    assertTrue(access.isValid());
  }

  @Test
  public void valid_not() {
    HubClientAccess access = HubClientAccess.create(
      User.create(),
      UserAuth.create(User.create(), UserAuthType.Google),
      ImmutableList.of(Account.create()));

    assertFalse(access.isValid());
  }

}
