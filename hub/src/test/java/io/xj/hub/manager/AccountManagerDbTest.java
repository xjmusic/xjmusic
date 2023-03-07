// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.app.AppEnvironment;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

public class AccountManagerDbTest {
  private AccountManager subject;
  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = AppEnvironment.getDefault();
    test = HubIntegrationTestFactory.build(env);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));

    // Instantiate the test entity
    subject = new AccountManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() {
    // future test: AccountManagerImpl of()
  }

  @Test
  public void readOne_asSetToModel() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    var result = subject.readOne(access, fake.account1.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getId());
    assertEquals("bananas", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    Collection<Account> results = subject.readMany(access, Lists.newArrayList());

    assertNotNull(results);
    assertEquals(1L, results.size());

    var result0 = results.iterator().next();
    assertEquals("bananas", result0.getName());
  }

  @Test
  public void update() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
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
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");
    var entity = new Account();
    entity.setId(UUID.randomUUID());
    entity.setName("jammers");

    var e = assertThrows(ManagerException.class,
      () -> subject.update(access, fake.account1.getId(), entity));
    assertEquals("top-level access is required", e.getMessage());
  }

  @Test
  public void delete() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    subject.destroy(access, fake.account1.getId());

    var e = assertThrows(ManagerException.class,
      () -> subject.readOne(HubAccess.internal(), fake.account1.getId()));
    assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
  }

  @Test
  public void delete_failsIfNotAdmin() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    var e = assertThrows(ManagerException.class,
      () -> subject.destroy(access, fake.account1.getId()));
    assertEquals("top-level access is required", e.getMessage());
  }

  @Test
  public void delete_failsIfHasLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
    test.insert(buildLibrary(fake.account1, "Testing"));

    var e = assertThrows(ManagerException.class, () ->
      subject.destroy(access, fake.account1.getId()));
    assertEquals("Account still has libraries!", e.getMessage());
  }

  @Test
  public void delete_failsIfHasAccountUser() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
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
