// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import io.xj.core.access.impl.Access;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_role.UserRole;
import io.xj.core.model.user_role.UserRoleType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AccessTest extends Mockito {
  @Mock private ContainerRequestContext crc;

  @Test
  public void matchRoles() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User,Artist"
    ));

    assertTrue(access.isAllowed(UserRoleType.User));
    assertTrue(access.isAllowed(UserRoleType.Artist));
    assertTrue(access.isAllowed(UserRoleType.User, UserRoleType.Artist));
    assertFalse(access.isAllowed(UserRoleType.Admin));
  }

  @Test
  public void getUserId() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2"
    ));

    assertEquals(BigInteger.valueOf(2), access.getUserId());
  }

  @Test
  public void fromContext() throws Exception {
    Access expectAccess = Access.from(ImmutableMap.of(
      "userId", "2"
    ));
    when(crc.getProperty(Access.CONTEXT_KEY))
      .thenReturn(expectAccess);

    Access actualAccess = Access.fromContext(crc);

    assertEquals(actualAccess, expectAccess);
  }

  @Test
  public void toJSON() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2",
      "roles", "User,Artist",
      "accounts", "1"
    ));

    assertEquals("{\"roles\":\"User,Artist\",\"accounts\":\"1\",\"userId\":\"2\"}", access.toJSON());
  }

  @Test
  public void getAccounts() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "accounts", "1,3,7"
    ));

    assertArrayEquals(new BigInteger[]{BigInteger.valueOf(1), BigInteger.valueOf(3), BigInteger.valueOf(7)}, access.getAccountIds().toArray());
  }

  @Test
  public void isAdmin() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User,Admin"
    ));

    assertTrue(access.isTopLevel());
  }

  @Test
  public void isAdmin_Not() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User,Artist"
    ));

    assertFalse(access.isTopLevel());
  }

  @Test
  public void intoMap() throws Exception {
    UserAuth userAuth = new UserAuth();
    userAuth.setId(BigInteger.valueOf(1));
    userAuth.setUserId(BigInteger.valueOf(1));
    AccountUser accountUser = new AccountUser();
    accountUser.setUserId(BigInteger.valueOf(1));
    accountUser.setAccountId(BigInteger.valueOf(101));
    Collection<AccountUser> userAccountRoles = ImmutableList.of(accountUser);
    UserRole userRole1 = new UserRole();
    userRole1.setUserId(BigInteger.valueOf(1));
    userRole1.setTypeEnum(UserRoleType.User);
    UserRole userRole2 = new UserRole();
    userRole2.setUserId(BigInteger.valueOf(1));
    userRole2.setTypeEnum(UserRoleType.Artist);
    Collection<UserRole> userRoles = ImmutableList.of(userRole1, userRole2);
    Access access = new Access(userAuth, userAccountRoles, userRoles);

    assertEquals(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "roles", "User,Artist",
      "accounts", "101"
    ), access.toMap());
  }

  @Test
  public void valid() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "roles", "User,Artist",
      "accounts", "101"
    ));

    assertTrue(access.isValid());
  }

  @Test
  public void valid_not() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101"
    ));

    assertFalse(access.isValid());
  }

}
