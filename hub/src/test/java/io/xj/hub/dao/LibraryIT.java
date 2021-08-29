// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.Instrument;
import io.xj.api.InstrumentState;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.User;
import io.xj.hub.HubException;
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
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LibraryIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private LibraryDAO testDAO;
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

    // Account "palm tree" has library "leaves" and library "coconuts"
    fake.account1 = test.insert(new Account()
      .id(UUID.randomUUID())
      .name("palm tree"));
    fake.library1a = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("leaves"));
    fake.library1b = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("coconuts"));

    // Account "boat" has library "helm" and library "sail"
    fake.account2 = test.insert(new Account()
      .id(UUID.randomUUID())
      .name("boat"));
    fake.library2a = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account2.getId())
      .name("helm"));
    fake.library2b = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account2.getId())
      .name("sail"));

    // Instantiate the test subject
    testDAO = injector.getInstance(LibraryDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .name("coconuts")
      .accountId(fake.account1.getId())
      ;

    Library result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void create_asEngineer() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Library inputData = new Library()
      .name("coconuts")
      .accountId(fake.account1.getId())
      ;

    Library result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void create_asEngineer_failsWithoutAccountAccess() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "Engineer");
    Library inputData = new Library()
      .name("coconuts")
      .accountId(fake.account1.getId())
      ;

    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .name("coconuts")
      ;

    failure.expect(DAOException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(
      hubAccess, inputData);
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Library result = testDAO.readOne(hubAccess, fake.library1b.getId());

    assertNotNull(result);
    assertEquals(fake.library1b.getId(), result.getId());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.account1.getId());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<Library> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
    Iterator<Library> resultIt = result.iterator();
    assertEquals("leaves", resultIt.next().getName());
    assertEquals("coconuts", resultIt.next().getName());
  }

  @Test
  public void readMany_fromAllAccounts() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1, fake.account2), "User");

    Collection<Library> result = testDAO.readMany(hubAccess, Lists.newArrayList());

    assertEquals(4L, result.size());
    Iterator<Library> it = result.iterator();
    assertEquals("leaves", it.next().getName());
    assertEquals("coconuts", it.next().getName());
    assertEquals("helm", it.next().getName());
    assertEquals("sail", it.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");

    Collection<Library> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .name("cannons")
      ;

    failure.expect(DAOException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .accountId(fake.account1.getId())
      ;

    failure.expect(DAOException.class);
    failure.expectMessage("Name is required");

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);
  }

  @Test
  public void update() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .name("cannons")
      .accountId(fake.account1.getId())
      ;

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);

    Library result = testDAO.readOne(HubAccess.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void update_asEngineer() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Library inputData = new Library()
      .name("cannons")
      .accountId(fake.account1.getId())
      ;

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);

    Library result = testDAO.readOne(HubAccess.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void update_asEngineer_failsWithoutAccountAccess() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "Engineer");
    Library inputData = new Library()
      .name("cannons")
      .accountId(fake.account1.getId())
      ;

    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .name("cannons")
      .accountId(fake.account1.getId())
      ;

    try {
      testDAO.update(hubAccess, fake.library1a.getId(), inputData);

    } catch (Exception e) {
      Library result = testDAO.readOne(HubAccess.internal(), fake.library1a.getId());
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(fake.library1a.getId(), result.getAccountId());
      assertSame(HubException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .name("cannons")
      .accountId(fake.account2.getId())
      ;

    testDAO.update(hubAccess, fake.library2a.getId(), inputData);

    Library result = testDAO.readOne(HubAccess.internal(), fake.library2a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account2.getId(), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .name("trunk")
      .accountId(fake.account1.getId())
      ;

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);

    Library result = testDAO.readOne(HubAccess.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    testDAO.destroy(hubAccess, fake.library1a.getId());

    try {
      testDAO.readOne(HubAccess.internal(), fake.library1a.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void delete_FailsIfLibraryHasProgram() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    fake.user101 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("bill")
      .email("bill@email.com")
      .avatarUrl("http://pictures.com/bill.gif"));
    test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2b.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("brilliant")
      .key("C#")
      .tempo(120.0)
      .density(0.6));

    try {
      testDAO.destroy(hubAccess, fake.library2b.getId());
    } catch (Exception e) {
      Library result = testDAO.readOne(HubAccess.internal(), fake.library2b.getId());
      assertNotNull(result);
      assertSame(DAOException.class, e.getClass());
      assertEquals("Found Program in Library", e.getMessage());
    }
  }

  @Test
  public void delete_FailsIfLibraryHasInstrument() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    fake.user101 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("bill")
      .email("bill@email.com")
      .avatarUrl("http://pictures.com/bill.gif"));
    test.insert(new Instrument()
      .id(UUID.randomUUID())
      .libraryId(fake.library2b.getId())
      .type(InstrumentType.PERCUSSIVE)
      .state(InstrumentState.PUBLISHED)
      .name("brilliant")
      .density(0.0));

    try {
      testDAO.destroy(hubAccess, fake.library2b.getId());
    } catch (Exception e) {
      Library result = testDAO.readOne(HubAccess.internal(), fake.library2b.getId());
      assertNotNull(result);
      assertSame(DAOException.class, e.getClass());
      assertEquals("Found Instrument in Library", e.getMessage());
    }
  }
}
