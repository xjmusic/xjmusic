package io.outright.xj.core.app.access;

import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.jooq.types.ULong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;

import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlTest extends Mockito {
  @Mock private ContainerRequestContext crc;

  @Test
  public void matchRoles() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user,artist"
    ));

    assertTrue(access.matchRoles(Role.USER));
    assertTrue(access.matchRoles(Role.ARTIST));
    assertTrue(access.matchRoles(Role.USER, Role.ARTIST));
    assertFalse(access.matchRoles(Role.ADMIN));
  }

  @Test
  public void getUserId() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2"
    ));

    assertEquals(ULong.valueOf(2), access.getUserId());
  }

  @Test
  public void fromContext() throws Exception {
    AccessControl expectAccess = new AccessControl(ImmutableMap.of(
      "userId", "2"
    ));
    when(crc.getProperty(AccessControl.CONTEXT_KEY))
      .thenReturn(expectAccess);

    AccessControl actualAccess = AccessControl.fromContext(crc);

    assertEquals(actualAccess, expectAccess);
  }

  @Test
  public void toJSON() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2",
      "roles", "user,artist",
      "accounts", "1"
    ));

    assertEquals("{\"userId\":\"2\",\"roles\":\"user,artist\",\"accounts\":\"1\"}", access.toJSON());
  }

  @Test
  public void getAccounts() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "accounts", "1,3,7"
    ));

    assertArrayEquals(new ULong[]{ULong.valueOf(1), ULong.valueOf(3), ULong.valueOf(7)}, access.getAccounts());
  }

  @Test
  public void isAdmin() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user,admin"
    ));

    assertTrue(access.isAdmin());
  }

  @Test
  public void isAdmin_Not() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user,artist"
    ));

    assertFalse(access.isAdmin());
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
    AccessControl access = new AccessControl(userAuthRecord, userAccountRoleRecords, userRoles);

    assertEquals(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "roles", "user,artist",
      "accounts", "101"
    ), access.intoMap());
  }

  @Test
  public void valid() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "roles", "user,artist",
      "accounts", "101"
    ));

    assertTrue(access.valid());
  }

  @Test
  public void valid_not() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userAuthId", "1",
      "userId", "1",
      "accounts", "101"
    ));

    assertFalse(access.valid());
  }

}
