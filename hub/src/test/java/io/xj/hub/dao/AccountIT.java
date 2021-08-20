// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.Library;
import io.xj.api.User;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AccountIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(new Account()
      .id(UUID.randomUUID())
      .name("bananas")
      );

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() {
    // future test: AccountDAOImpl of()
  }

  @Test
  public void readOne_asSetToModel() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    var result = testDAO.readOne(hubAccess, fake.account1.getId());

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getId());
    assertEquals("bananas", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<Account> results = testDAO.readMany(hubAccess, Lists.newArrayList());

    assertNotNull(results);
    assertEquals(1L, results.size());

    var result0 = results.iterator().next();
    assertEquals("bananas", result0.getName());
  }

  @Test
  public void update() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    var entity = new Account()
      .id(UUID.randomUUID())
      .name("jammers")
      ;

    testDAO.update(hubAccess, fake.account1.getId(), entity);

    var result = testDAO.readOne(HubAccess.internal(), fake.account1.getId());
    assertNotNull(result);
    assertEquals("jammers", result.getName());
  }

  @Test
  public void update_failsIfNotAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    var entity = new Account()
      .id(UUID.randomUUID())
      .name("jammers")
      ;

    failure.expect(DAOException.class);
    failure.expectMessage("top-level hubAccess is required");

    testDAO.update(hubAccess, fake.account1.getId(), entity);
  }

  @Test
  public void delete() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    testDAO.destroy(hubAccess, fake.account1.getId());

    try {
      testDAO.readOne(HubAccess.internal(), fake.account1.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void delete_failsIfNotAdmin() throws Exception {
    failure.expect(DAOException.class);
    failure.expectMessage("top-level hubAccess is required");
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    testDAO.destroy(hubAccess, fake.account1.getId());
  }

  @Test
  public void delete_failsIfHasLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("Testing")
      );

    failure.expect(DAOException.class);
    failure.expectMessage("Found Library in Account");

    testDAO.destroy(hubAccess, fake.account1.getId());
  }

  @Test
  public void delete_failsIfHasAccountUser() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    fake.user1 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("jim")
      .email("jim@jim.com")
      .avatarUrl("http://www.jim.com/jim.png")
      );
    test.insert(new AccountUser()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .userId(fake.user1.getId())
      );

    failure.expect(DAOException.class);
    failure.expectMessage("Found User in Account");

    testDAO.destroy(hubAccess, fake.account1.getId());
  }

}
