// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Account;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.model.User;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.Assert;
import io.xj.lib.core.testing.IntegrationTestProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or delete account users
public class AccountUserIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUserDAO testDAO;
  private AccountUser accountUser_1_2;
  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    accountUser_1_2 = test.insert(AccountUser.create(fake.account1, fake.user2));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(AccountUser.create(fake.account1, fake.user3));

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountUserDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create("Admin");
    fake.user5 = test.insert(User.create("Jim", "jim@email.com", "http://pictures.com/jim.gif"));
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user5.getId());

    AccountUser result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user5.getId(), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = Access.create("Admin");
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("Account User already exists!");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailIfNotAdmin() throws Exception {
    Access access = Access.create("User");
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = Access.create("Admin");
    AccountUser inputData = new AccountUser()
      .setUserId(fake.user2.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutUserId() throws Exception {
    Access access = Access.create("Admin");
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");

    AccountUser result = testDAO.readOne(access, accountUser_1_2.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user2.getId(), result.getUserId());
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

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserCanSeeInsideOwnAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "Artist");

    Collection<AccountUser> result = testDAO.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, accountUser_1_2.getId());

    Assert.assertNotExist(testDAO, accountUser_1_2.getId());
  }

  @Test
  public void delete_FailIfNotAdmin() throws Exception {
    Access access = Access.create("User");

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, accountUser_1_2.getId());
  }
}
