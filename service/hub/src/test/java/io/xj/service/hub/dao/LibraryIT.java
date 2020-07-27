// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

public class LibraryIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private LibraryDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "palm tree" has library "leaves" and library "coconuts"
    fake.account1 = test.insert(Account.create("palm tree"));
    fake.library1a = test.insert(Library.create(fake.account1, "leaves", Instant.now()));
    fake.library1b = test.insert(Library.create(fake.account1, "coconuts", Instant.now()));

    // Account "boat" has library "helm" and library "sail"
    fake.account2 = test.insert(Account.create("boat"));
    fake.library2a = test.insert(Library.create(fake.account2, "helm", Instant.now()));
    fake.library2b = test.insert(Library.create(fake.account2, "sail", Instant.now()));

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
      .setName("coconuts")
      .setAccountId(fake.account1.getId());

    Library result = testDAO.create(hubAccess, inputData);

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
      .setName("coconuts")
      .setAccountId(fake.account1.getId());

    Library result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void create_asEngineer_failsWithoutAccountAccess() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "Engineer");
    Library inputData = new Library()
      .setName("coconuts")
      .setAccountId(fake.account1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.create(hubAccess, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .setName("coconuts");

    failure.expect(ValueException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(hubAccess, inputData);
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User");
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User");

    Collection<Library> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .setName("cannons");

    failure.expect(ValueException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .setAccountId(fake.account1.getId());

    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);
  }

  @Test
  public void update() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account1.getId());

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
      .setName("cannons")
      .setAccountId(fake.account1.getId());

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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "Engineer");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.update(hubAccess, fake.library1a.getId(), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account1.getId());

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
      .setName("cannons")
      .setAccountId(fake.account2.getId());

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
      .setName("trunk")
      .setAccountId(fake.account1.getId());

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
    fake.user101 = test.insert(User.create("bill", "bill@email.com", "http://pictures.com/bill.gif"));
    test.insert(Program.create(fake.user101, fake.library2b, ProgramType.Main, ProgramState.Published, "brilliant", "C#", 120.0, 0.6));

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
    fake.user101 = test.insert(User.create("bill", "bill@email.com", "http://pictures.com/bill.gif"));
    test.insert(Instrument.create(fake.user101, fake.library2b, InstrumentType.Percussive, InstrumentState.Published, "brilliant"));

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
