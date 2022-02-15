// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

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
public class AccountUserManagerImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUserManager testManager;
  private AccountUser accountUser_1_2;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
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
    testManager = injector.getInstance(AccountUserManager.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    fake.user5 = test.insert(buildUser("Jim", "jim@email.com", "https://pictures.com/jim.gif", "Admin"));
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user5.getId());

    var result = testManager.create(
      access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user5.getId(), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user2.getId());

    failure.expect(ManagerException.class);
    failure.expectMessage("Account User already exists!");

    testManager.create(
      access, inputData);
  }

  @Test
  public void create_FailIfNotAdmin() throws Exception {
    HubAccess access = HubAccess.create("User");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user2.getId());

    failure.expect(ManagerException.class);
    failure.expectMessage("top-level access is required");

    testManager.create(
      access, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setUserId(fake.user2.getId());

    failure.expect(ManagerException.class);
    failure.expectMessage("Account ID is required");

    testManager.create(
      access, inputData);
  }

  @Test
  public void create_FailsWithoutUserId() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());

    failure.expect(ManagerException.class);
    failure.expectMessage("User ID is required");

    testManager.create(
      access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Artist");

    var result = testManager.readOne(access, accountUser_1_2.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "Artist");
    failure.expect(ManagerException.class);
    failure.expectMessage("does not exist");

    testManager.readOne(access, accountUser_1_2.getId());
  }

  @Test
  public void readMany_Admin() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    Collection<AccountUser> result = testManager.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_UserCanSeeInsideOwnAccount() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<AccountUser> result = testManager.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")
    ), "Artist");

    Collection<AccountUser> result = testManager.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    testManager.destroy(access, accountUser_1_2.getId());

    try {
      testManager.readOne(HubAccess.internal(), accountUser_1_2.getId());
      fail();
    } catch (ManagerException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void delete_FailIfNotAdmin() throws Exception {
    HubAccess access = HubAccess.create("User");

    failure.expect(ManagerException.class);
    failure.expectMessage("top-level access is required");

    testManager.destroy(access, accountUser_1_2.getId());
  }
}
