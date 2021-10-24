// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static io.xj.hub.IntegrationTestingFixtures.*;
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
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));
    accountUser_1_2 = test.insert(buildAccountUser(fake.account1, fake.user2));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

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
    fake.user5 = test.insert(buildUser("Jim", "jim@email.com", "https://pictures.com/jim.gif", "Admin"));
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user5.getId());

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user5.getId(), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user2.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Account User already exists!");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void create_FailIfNotAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create("User");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user2.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("top-level hubAccess is required");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setUserId(fake.user2.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutUserId() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("User ID is required");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");

    var result = testDAO.readOne(hubAccess, accountUser_1_2.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, accountUser_1_2.getId());
  }

  @Test
  public void readMany_Admin() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    Collection<AccountUser> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_UserCanSeeInsideOwnAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<AccountUser> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")
    ), "Artist");

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
