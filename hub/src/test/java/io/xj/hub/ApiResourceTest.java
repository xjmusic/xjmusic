//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.access.Access;
import io.xj.core.app.ApiResource;
import io.xj.core.dao.AccountDAO;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.User;
import io.xj.core.payload.Payload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;

import static io.xj.core.access.Access.CONTEXT_KEY;
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
  private Account account12;
  private User user17;

  @Before
  public void setUp() {
    account12 = Account.create();
    user17 = User.create();
    access = Access.create(user17, ImmutableList.of(account12), "Artist");
    subject = new ApiResource();
    payload = new Payload();
    at = Instant.parse("2019-07-18T21:28:07Z");
    accountUser = AccountUser.create(account12, user17);
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
    when(accountUserDAO.newInstance()).thenReturn(AccountUser.create(account12, user17));
    when(accountUserDAO.create(same(access), any()))
      .thenReturn(accountUser);

    Response result = subject.create(crc, accountUserDAO, payload);

    assertEquals(201, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("account-users", accountUser.getId().toString())
      .belongsTo(Account.class, account12.getId().toString())
      .belongsTo(User.class, user17.getId().toString());
  }

  @Test
  public void readOne() {
    // FUTURE
  }

  @Test
  public void readMany_empty() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Collection<Account> noAccounts = ImmutableList.of();
    when(accountDAO.readMany(same(access), eq(ImmutableList.of(account12.getId()))))
      .thenReturn(noAccounts);

    Response result = subject.readMany(crc, accountDAO, account12.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("accounts", ImmutableList.of());
  }

  @Test
  public void readMany() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Account account27 = Account.create("Test Account", at);
    Collection<Account> accounts = ImmutableList.of(account27);
    when(accountDAO.readMany(same(access), eq(ImmutableList.of(account12.getId()))))
      .thenReturn(accounts);

    Response result = subject.readMany(crc, accountDAO, account12.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("accounts", ImmutableList.of(account27.getId().toString()));
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
