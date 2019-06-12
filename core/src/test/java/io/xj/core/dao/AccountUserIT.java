// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.AccountUser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or delete account users
public class AccountUserIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUserDAO testDAO;
  private AccountUser accountUser_1_2;
    private AccountUser accountUser_1_3;

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(newAccount(1, "bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif"));
    accountUser_1_2 = insert(newAccountUser(1, 2));

    // Jenny has a "user" role and belongs to account "bananas"
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    accountUser_1_3 = insert(newAccountUser(1, 3));

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountUserDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    insert(newUser(5, "Jim", "jim@email.com", "http://pictures.com/jim.gif"));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L))
      .setUserId(BigInteger.valueOf(5L));

    AccountUser result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(BigInteger.valueOf(5L), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L))
      .setUserId(BigInteger.valueOf(2L));

    failure.expect(CoreException.class);
    failure.expectMessage("Account User already exists!");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L))
      .setUserId(BigInteger.valueOf(2L));

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setUserId(BigInteger.valueOf(2L));

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutUserId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1L));

    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AccountUser result = testDAO.readOne(access, accountUser_1_2.getId());

    assertNotNull(result);
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

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserCanSeeInsideOwnAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1002000L));

    assertNotExist(testDAO, BigInteger.valueOf(1002000L));
  }

  @Test
  public void delete_FailIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, BigInteger.valueOf(1002000L));
  }
}
