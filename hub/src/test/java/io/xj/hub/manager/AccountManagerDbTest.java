// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.Account;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class AccountManagerDbTest {
  AccountManager subject;
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

    // Instantiate the test entity
    subject = new AccountManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() {
    // future test: AccountManagerImpl of()
  }

  @Test
  public void readOne_asSetToModel() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");

    var result = subject.readOne(access, fake.account1.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getId());
    assertEquals("bananas", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");

    Collection<Account> results = subject.readMany(access, new ArrayList<>());

    assertNotNull(results);
    assertEquals(1L, results.size());

    var result0 = results.iterator().next();
    assertEquals("bananas", result0.getName());
  }

  @Test
  public void update() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin");
    var entity = new Account();
    entity.setId(UUID.randomUUID());
    entity.setName("jammers");

    subject.update(access, fake.account1.getId(), entity);

    var result = subject.readOne(HubAccess.internal(), fake.account1.getId());
    assertNotNull(result);
    assertEquals("jammers", result.getName());
  }

  @Test
  public void update_failsIfNotAdmin() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");
    var entity = new Account();
    entity.setId(UUID.randomUUID());
    entity.setName("jammers");

    var e = assertThrows(ManagerException.class,
      () -> subject.update(access, fake.account1.getId(), entity));
    assertEquals("top-level access is required", e.getMessage());
  }

  @Test
  public void delete() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin");

    subject.destroy(access, fake.account1.getId());

    var e = assertThrows(ManagerException.class,
      () -> subject.readOne(HubAccess.internal(), fake.account1.getId()));
    assertTrue(e.getMessage().contains("does not exist"), "Record should not exist");
  }

  @Test
  public void delete_failsIfNotAdmin() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User");

    var e = assertThrows(ManagerException.class,
      () -> subject.destroy(access, fake.account1.getId()));
    assertEquals("top-level access is required", e.getMessage());
  }

  @Test
  public void delete_failsIfHasLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin");
    test.insert(buildLibrary(fake.account1, "Testing"));

    var e = assertThrows(ManagerException.class, () ->
      subject.destroy(access, fake.account1.getId()));
    assertEquals("Account still has libraries!", e.getMessage());
  }

  @Test
  public void delete_failsIfHasAccountUser() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin");
    fake.user1 = test.insert(buildUser("jim",
      "jim@jim.com",
      "https://www.jim.com/jim.png",
      "User"));
    test.insert(buildAccountUser(fake.account1, fake.user1));

    var e = assertThrows(ManagerException.class,
      () -> subject.destroy(access, fake.account1.getId()));
    assertEquals("Account still has user access!", e.getMessage());
  }

}
