// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.User;
import io.xj.UserRole;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.testing.HubIntegrationTestModule;
import io.xj.hub.testing.HubIntegrationTestProvider;
import io.xj.hub.testing.HubTestConfiguration;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bananas")
      .build());

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("john")
      .setEmail("john@email.com")
      .setAvatarUrl("http://pictures.com/john.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.Admin)
      .build());
    accountUser_1_2 = test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId())
      .build());

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("jenny")
      .setEmail("jenny@email.com")
      .setAvatarUrl("http://pictures.com/jenny.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user3.getId())
      .build());

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
    fake.user5 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Jim")
      .setEmail("jim@email.com")
      .setAvatarUrl("http://pictures.com/jim.gif")
      .build());
    var inputData = AccountUser.newBuilder()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user5.getId())
      .build();

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user5.getId(), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var inputData = AccountUser.newBuilder()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId())
      .build();

    failure.expect(DAOException.class);
    failure.expectMessage("Account User already exists!");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void create_FailIfNotAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create("User");
    var inputData = AccountUser.newBuilder()
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user2.getId())
      .build();

    failure.expect(DAOException.class);
    failure.expectMessage("top-level hubAccess is required");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var inputData = AccountUser.newBuilder()
      .setUserId(fake.user2.getId())
      .build();

    failure.expect(DAOException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutUserId() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var inputData = AccountUser.newBuilder()
      .setAccountId(fake.account1.getId())
      .build();

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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "Artist");
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "Artist");

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
