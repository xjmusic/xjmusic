// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.account_user;

import com.google.common.collect.ImmutableList;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or delete account users
public class AccountUserIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUserDAO testDAO;
  private AccountUser accountUser_1_2;

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    account1 = insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    accountUser_1_2 = insert(AccountUser.create(account1, user2));

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(AccountUser.create(account1, user3));

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountUserDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create("Admin");
    User user5 = insert(User.create("Jim", "jim@email.com", "http://pictures.com/jim.gif"));
    AccountUser inputData = new AccountUser()
      .setAccountId(account1.getId())
      .setUserId(user5.getId());

    AccountUser result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(user5.getId(), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = Access.create("Admin");
    AccountUser inputData = new AccountUser()
      .setAccountId(account1.getId())
      .setUserId(user2.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("Account User already exists!");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailIfNotAdmin() throws Exception {
    Access access = Access.create("User");
    AccountUser inputData = new AccountUser()
      .setAccountId(account1.getId())
      .setUserId(user2.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = Access.create("Admin");
    AccountUser inputData = new AccountUser()
      .setUserId(user2.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutUserId() throws Exception {
    Access access = Access.create("Admin");
    AccountUser inputData = new AccountUser()
      .setAccountId(account1.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");

    AccountUser result = testDAO.readOne(access, accountUser_1_2.getId());

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(user2.getId(), result.getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, accountUser_1_2.getId());
  }

  @Test
  public void readAll_Admin() throws Exception {
    Access access = Access.create("Admin");

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserCanSeeInsideOwnAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "Artist");

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(account1.getId()));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, accountUser_1_2.getId());

    assertNotExist(testDAO, accountUser_1_2.getId());
  }

  @Test
  public void delete_FailIfNotAdmin() throws Exception {
    Access access = Access.create("User");

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, accountUser_1_2.getId());
  }
}
