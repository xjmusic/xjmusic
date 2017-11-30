// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.access;

import io.xj.core.access.impl.Access;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.AccountUserRecord;
import io.xj.core.tables.records.UserAuthRecord;
import io.xj.core.tables.records.UserRoleRecord;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
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
    Access access = new Access(ImmutableMap.of(
      "roles", "user,artist"
    ));

    assertTrue(access.matchAnyOf(Role.USER));
    assertTrue(access.matchAnyOf(Role.ARTIST));
    assertTrue(access.matchAnyOf(Role.USER, Role.ARTIST));
    assertFalse(access.matchAnyOf(Role.ADMIN));
  }

  @Test
  public void getUserId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2"
    ));

    assertEquals(ULong.valueOf(2), access.getUserId());
  }

  @Test
  public void fromContext() throws Exception {
    Access expectAccess = new Access(ImmutableMap.of(
      "userId", "2"
    ));
    when(crc.getProperty(Access.CONTEXT_KEY))
      .thenReturn(expectAccess);

    Access actualAccess = Access.fromContext(crc);

    assertEquals(actualAccess, expectAccess);
  }

  @Test
  public void toJSON() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "user,artist",
      "accounts", "1"
    ));

    assertEquals("{\"userId\":\"2\",\"roles\":\"user,artist\",\"accounts\":\"1\"}", access.toJSON());
  }

  @Test
  public void getAccounts() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "accounts", "1,3,7"
    ));

    assertArrayEquals(new ULong[]{ULong.valueOf(1), ULong.valueOf(3), ULong.valueOf(7)}, access.getAccounts());
  }

  @Test
  public void isAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user,admin"
    ));

    assertTrue(access.isTopLevel());
  }

  @Test
  public void isAdmin_Not() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user,artist"
    ));

    assertFalse(access.isTopLevel());
  }

  @Test
  public void intoMap() throws Exception {
    UserAuthRecord userAuthRecord = new UserAuthRecord();
    userAuthRecord.setId(ULong.valueOf(1));
    userAuthRecord.setUserId(ULong.valueOf(1));
    AccountUserRecord accountUser = new AccountUserRecord();
    accountUser.setUserId(ULong.valueOf(1));
    accountUser.setAccountId(ULong.valueOf(101));
    Collection<AccountUserRecord> userAccountRoleRecords = ImmutableList.of(accountUser);
    UserRoleRecord userRole1 = new UserRoleRecord();
    userRole1.setUserId(ULong.valueOf(1));
    userRole1.setType(Role.USER);
    UserRoleRecord userRole2 = new UserRoleRecord();
    userRole2.setUserId(ULong.valueOf(1));
    userRole2.setType(Role.ARTIST);
    Collection<UserRoleRecord> userRoles = ImmutableList.of(userRole1, userRole2);
    Access access = new Access(userAuthRecord, userAccountRoleRecords, userRoles);

    assertEquals(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "roles", "user,artist",
      "accounts", "101"
    ), access.intoMap());
  }

  @Test
  public void valid() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "roles", "user,artist",
      "accounts", "101"
    ));

    assertTrue(access.valid());
  }

  @Test
  public void valid_not() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101"
    ));

    assertFalse(access.valid());
  }

}
