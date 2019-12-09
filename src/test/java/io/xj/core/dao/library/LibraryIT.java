// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.library;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.User;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.Assert;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.core.testing.InternalResources;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class LibraryIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private LibraryDAO testDAO;
  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "palm tree" has library "leaves" and library "coconuts"
    fake.account1 = test.insert(Account.create("palm tree"));
    fake.library1a = test.insert(Library.create(fake.account1, "leaves", InternalResources.now()));
    fake.library1b = test.insert(Library.create(fake.account1, "coconuts", InternalResources.now()));

    // Account "boat" has library "helm" and library "sail"
    fake.account2 = test.insert(Account.create("boat"));
    fake.library2a = test.insert(Library.create(fake.account2, "helm", InternalResources.now()));
    fake.library2b = test.insert(Library.create(fake.account2, "sail", InternalResources.now()));

    // Instantiate the test subject
    testDAO = injector.getInstance(LibraryDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setName("coconuts")
      .setAccountId(fake.account1.getId());

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void create_asEngineer() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Engineer");
    Library inputData = new Library()
      .setName("coconuts")
      .setAccountId(fake.account1.getId());

    Library result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void create_asEngineer_failsWithoutAccountAccess() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "Engineer");
    Library inputData = new Library()
      .setName("coconuts")
      .setAccountId(fake.account1.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setName("coconuts");

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Library result = testDAO.readOne(access, fake.library1b.getId());

    assertNotNull(result);
    assertEquals(fake.library1b.getId(), result.getId());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fake.account1.getId());
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Library> result = testDAO.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
    Iterator<Library> resultIt = result.iterator();
    assertEquals("leaves", resultIt.next().getName());
    assertEquals("coconuts", resultIt.next().getName());
  }

  @Test
  public void readAll_fromAllAccounts() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1, fake.account2), "User");

    Collection<Library> result = testDAO.readMany(access, Lists.newArrayList());

    assertEquals(4L, result.size());
    Iterator<Library> it = result.iterator();
    assertEquals("leaves", it.next().getName());
    assertEquals("coconuts", it.next().getName());
    assertEquals("helm", it.next().getName());
    assertEquals("sail", it.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Library> result = testDAO.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setName("cannons");

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, fake.library1a.getId(), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setAccountId(fake.account1.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, fake.library1a.getId(), inputData);
  }

  @Test
  public void update() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account1.getId());

    testDAO.update(access, fake.library1a.getId(), inputData);

    Library result = testDAO.readOne(Access.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void update_asEngineer() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Engineer");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account1.getId());

    testDAO.update(access, fake.library1a.getId(), inputData);

    Library result = testDAO.readOne(Access.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void update_asEngineer_failsWithoutAccountAccess() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "Engineer");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account1.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.update(access, fake.library1a.getId(), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account1.getId());

    try {
      testDAO.update(access, fake.library1a.getId(), inputData);

    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), fake.library1a.getId());
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(fake.library1a.getId(), result.getAccountId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(fake.account2.getId());

    testDAO.update(access, fake.library2a.getId(), inputData);

    Library result = testDAO.readOne(Access.internal(), fake.library2a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account2.getId(), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    Access access = Access.create("Admin");
    Library inputData = new Library()
      .setName("trunk")
      .setAccountId(fake.account1.getId());

    testDAO.update(access, fake.library1a.getId(), inputData);

    Library result = testDAO.readOne(Access.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, fake.library1a.getId());

    Assert.assertNotExist(testDAO, fake.library1a.getId());
  }

  @Test
  public void delete_FailsIfLibraryHasProgram() throws Exception {
    Access access = Access.create("Admin");
    fake.user101 = test.insert(User.create("bill", "bill@email.com", "http://pictures.com/bill.gif"));
    test.insert(Program.create(fake.user101, fake.library2b, ProgramType.Main, ProgramState.Published, "brilliant", "C#", 120.0, 0.6));

    try {
      testDAO.destroy(access, fake.library2b.getId());
    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), fake.library2b.getId());
      assertNotNull(result);
      assertSame(CoreException.class, e.getClass());
      assertEquals("Found Program in Library", e.getMessage());
    }
  }

  @Test
  public void delete_FailsIfLibraryHasInstrument() throws Exception {
    Access access = Access.create("Admin");
    fake.user101 = test.insert(User.create("bill", "bill@email.com", "http://pictures.com/bill.gif"));
    test.insert(Instrument.create(fake.user101, fake.library2b, InstrumentType.Percussive, InstrumentState.Published, "brilliant"));

    try {
      testDAO.destroy(access, fake.library2b.getId());
    } catch (Exception e) {
      Library result = testDAO.readOne(Access.internal(), fake.library2b.getId());
      assertNotNull(result);
      assertSame(CoreException.class, e.getClass());
      assertEquals("Found Instrument in Library", e.getMessage());
    }
  }
}
