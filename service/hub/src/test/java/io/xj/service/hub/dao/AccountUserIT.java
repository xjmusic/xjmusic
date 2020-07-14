// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.entity.AccountUser;
import io.xj.service.hub.entity.User;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or delete account users
public class AccountUserIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUserDAO testDAO;
  private AccountUser accountUser_1_2;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
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
    HubAccess hubAccess = HubAccess.create("Admin");
    fake.user5 = test.insert(User.create("Jim", "jim@email.com", "http://pictures.com/jim.gif"));
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user5.getId());

    AccountUser result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user5.getId(), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Account User already exists!");

    testDAO.create(hubAccess, inputData);
  }

  @Test
  public void create_FailIfNotAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create("User");
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("top-level hubAccess is required");

    testDAO.create(hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    AccountUser inputData = new AccountUser()
      .setUserId(fake.user2.getId());

    failure.expect(ValueException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutUserId() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    AccountUser inputData = new AccountUser()
      .setAccountId(fake.account1.getId());

    failure.expect(ValueException.class);
    failure.expectMessage("User ID is required");

    testDAO.create(hubAccess, inputData);
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");

    AccountUser result = testDAO.readOne(hubAccess, accountUser_1_2.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, accountUser_1_2.getId());
  }

  @Test
  public void readAll_Admin() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    Collection<AccountUser> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_UserCanSeeInsideOwnAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<AccountUser> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "Artist");

    Collection<AccountUser> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    testDAO.destroy(hubAccess, accountUser_1_2.getId());

    try {
      testDAO.readOne(HubAccess.internal(), accountUser_1_2.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void delete_FailIfNotAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create("User");

    failure.expect(DAOException.class);
    failure.expectMessage("top-level hubAccess is required");

    testDAO.destroy(hubAccess, accountUser_1_2.getId());
  }
}
