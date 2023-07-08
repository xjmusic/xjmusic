// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// future test: permissions of different users to readMany vs. of vs. update or delete account users
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class AccountUserManagerDbTest {
  AccountUserManager testManager;
  AccountUser accountUser_1_2;
  HubIntegrationTest test;
  IntegrationTestingFixtures fake;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @Autowired
  HubIntegrationTestFactory integrationTestFactory;

  @BeforeEach
  public void setUp() throws Exception {
    test = integrationTestFactory.build();
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
    testManager = new AccountUserManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @AfterAll
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

    var result = testManager.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user5.getId(), result.getUserId());
  }

  @Test
  public void create_FailIfAlreadyExists() {
    HubAccess access = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user2.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.create(access, inputData));

    assertEquals("Account User already exists!", e.getMessage());
  }

  @Test
  public void create_FailIfNotAdmin() {
    HubAccess access = HubAccess.create("User");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());
    inputData.setUserId(fake.user2.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.create(access, inputData));

    assertEquals("top-level access is required", e.getMessage());
  }

  @Test
  public void create_FailsWithoutAccountID() {
    HubAccess access = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setUserId(fake.user2.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.create(access, inputData));

    assertEquals("Account ID is required.", e.getMessage());
  }

  @Test
  public void create_FailsWithoutUserId() {
    HubAccess access = HubAccess.create("Admin");
    var inputData = new AccountUser();
    inputData.setAccountId(fake.account1.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.create(access, inputData));

    assertEquals("User ID is required.", e.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Artist");

    var result = testManager.readOne(access, accountUser_1_2.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "Artist");
    var e = assertThrows(ManagerException.class, () -> testManager.readOne(access, accountUser_1_2.getId()));

    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readMany_Admin() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    Collection<AccountUser> result = testManager.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_UserCanSeeInsideOwnAccount() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    Collection<AccountUser> result = testManager.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "Artist");

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
      assertTrue(e.getMessage().contains("does not exist"), "Record should not exist");
    }
  }

  @Test
  public void delete_FailIfNotAdmin() {
    HubAccess access = HubAccess.create("User");

    var e = assertThrows(ManagerException.class, () -> testManager.destroy(access, accountUser_1_2.getId()));

    assertEquals("top-level access is required", e.getMessage());
  }
}
