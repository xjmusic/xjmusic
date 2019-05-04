// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.account_user.AccountUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or delete account users
public class AccountUserIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUserDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertAccountUser(1, 2);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountUserDAO.class);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertUser(5, "Jim", "jim@email.com", "http://pictures.com/jim.gif");
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L))
      .setUserId(BigInteger.valueOf(5L));

    AccountUser result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(BigInteger.valueOf(5L), result.getUserId());
  }

  @Test(expected = CoreException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L))
      .setUserId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L))
      .setUserId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setUserId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutUserId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AccountUser result = testDAO.readOne(access, BigInteger.valueOf(1002000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1002000L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1002000L));
  }

  @Test
  public void readAll_Admin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    Collection<AccountUser> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserCanSeeInsideOwnAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<AccountUser> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<AccountUser> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1002000L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1002000L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1002000L));
  }
}
