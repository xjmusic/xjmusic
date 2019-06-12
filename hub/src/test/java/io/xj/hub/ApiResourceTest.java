//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.CoreTest;
import io.xj.core.access.impl.Access;
import io.xj.core.app.ApiResource;
import io.xj.core.dao.AccountDAO;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.account.AccountUser;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

import static io.xj.core.access.impl.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApiResourceTest extends CoreTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  AccountDAO accountDAO;
  @Mock
  AccountUserDAO accountUserDAO;
  private Access access;
  private Instant at;
  private ApiResource subject;
  private Payload payload;
  private AccountUser accountUser;

  @Before
  public void setUp() {
    access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    subject = new ApiResource();
    payload = new Payload();
    at = Instant.parse("2019-07-18T21:28:07Z");
    accountUser = newAccountUser(12, 17);
    payload.setDataEntity(accountUser);
  }

  @Test
  public void extractPrimaryObject() {
    // FUTURE
  }

  @Test
  public void setInjector() {
    // FUTURE
  }

  @Test
  public void create() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(accountUserDAO.newInstance()).thenReturn(newAccountUser(12, 17));
    accountUser.setId(BigInteger.valueOf(17));
    when(accountUserDAO.create(same(access), any()))
      .thenReturn(accountUser);

    Response result = subject.create(crc, accountUserDAO, payload);

    assertEquals(201, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("account-users", "17")
      .belongsTo(Account.class, "12")
      .belongsTo(User.class, "17");
  }

  @Test
  public void readOne() {
    // FUTURE
  }

  @Test
  public void readMany_empty() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Collection<Account> noAccounts = ImmutableList.of();
    when(accountDAO.readMany(same(access), eq(ImmutableList.of(BigInteger.valueOf(17)))))
      .thenReturn(noAccounts);

    Response result = subject.readMany(crc, accountDAO, "17");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("accounts", ImmutableList.of());
  }

  @Test
  public void readMany() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Collection<Account> accounts = ImmutableList.of(newAccount(27, "Test Account", at));
    when(accountDAO.readMany(same(access), eq(ImmutableList.of(BigInteger.valueOf(17)))))
      .thenReturn(accounts);

    Response result = subject.readMany(crc, accountDAO, "17");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("accounts", ImmutableList.of("27"));
  }

  @Test
  public void readMany_listOfParentIds() {
    // FUTURE
  }

  @Test
  public void update() {
    // FUTURE
  }

  @Test
  public void delete() {
    // FUTURE
  }
}
