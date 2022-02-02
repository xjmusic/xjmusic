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
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

public class AccountManagerImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountManager testManager;
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

    // Instantiate the test subject
    testManager = injector.getInstance(AccountManager.class);
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    var result = testManager.readOne(hubAccess, fake.account1.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getId());
    assertEquals("bananas", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<Account> results = testManager.readMany(hubAccess, Lists.newArrayList());

    assertNotNull(results);
    assertEquals(1L, results.size());

    var result0 = results.iterator().next();
    assertEquals("bananas", result0.getName());
  }

  @Test
  public void update() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    var entity = new Account();
    entity.setId(UUID.randomUUID());
    entity.setName("jammers");

    testManager.update(hubAccess, fake.account1.getId(), entity);

    var result = testManager.readOne(HubAccess.internal(), fake.account1.getId());
    assertNotNull(result);
    assertEquals("jammers", result.getName());
  }

  @Test
  public void update_failsIfNotAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    var entity = new Account();
    entity.setId(UUID.randomUUID());
    entity.setName("jammers");

    failure.expect(ManagerException.class);
    failure.expectMessage("top-level hubAccess is required");

    testManager.update(hubAccess, fake.account1.getId(), entity);
  }

  @Test
  public void delete() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    testManager.destroy(hubAccess, fake.account1.getId());

    try {
      testManager.readOne(HubAccess.internal(), fake.account1.getId());
      fail();
    } catch (ManagerException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void delete_failsIfNotAdmin() throws Exception {
    failure.expect(ManagerException.class);
    failure.expectMessage("top-level hubAccess is required");
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    testManager.destroy(hubAccess, fake.account1.getId());
  }

  @Test
  public void delete_failsIfHasLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    test.insert(buildLibrary(fake.account1, "Testing"));

    var e = assertThrows(ManagerException.class, () ->
      testManager.destroy(hubAccess, fake.account1.getId()));
    assertEquals("Found Library in Account", e.getMessage());
  }

  @Test
  public void delete_failsIfHasAccountUser() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    fake.user1 = test.insert(buildUser("jim",
      "jim@jim.com",
      "https://www.jim.com/jim.png",
      "User"));
    test.insert(buildAccountUser(fake.account1, fake.user1));

    failure.expect(ManagerException.class);
    failure.expectMessage("Found User in Account");

    testManager.destroy(hubAccess, fake.account1.getId());
  }

}
