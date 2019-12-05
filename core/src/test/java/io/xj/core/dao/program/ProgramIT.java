// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.program;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentState;
import io.xj.core.model.User;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.Assert;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.core.testing.InternalResources;
import io.xj.core.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private WorkManager workManager;
  private ProgramDAO testDAO;
  private ProgramSequenceBinding sequenceBinding1a_0;

  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    workManager = injector.getInstance(WorkManager.class);
    injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(fake.user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(fake.user3, UserRoleType.User));
    test.insert(AccountUser.create(fake.account1, fake.user3));

    // Library "palm tree" has program "fonds" and program "nuts"
    fake.library1 = test.insert(Library.create(fake.account1, "palm tree", InternalResources.now()));
    fake.program1 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    ProgramSequence sequence1a = test.insert(ProgramSequence.create(fake.program1, 4, "Ants", 0.583, "D minor", 120.0));
    sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.create(sequence1a, 0));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "leafy"));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Rhythm, ProgramState.Published, "nuts", "C#", 120.0, 0.6));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.create(fake.account1, "boat", InternalResources.now()));
    fake.program3 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    ProgramSequence sequence3a = test.insert(ProgramSequence.create(fake.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(sequence3a, 0));
    fake.program4 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program subject = Program.create()
      .setKey("G minor 7")
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  /**
   [#156144567] Artist expects to of a Main-type program without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    Program inputData = Program.create()
      .setKey("G minor 7")
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program inputData = Program.create()
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library2.getId())
      .setDensity(0.583)
      .setName("cannons fifty nine");

    Program result = testDAO.clone(access, fake.program1.getId(), inputData);
    // TODO test clones sub-entities of program

    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Program result = testDAO.readOne(access, fake.program2.getId());

    assertNotNull(result);
    assertEquals(ProgramType.Rhythm, result.getType());
    assertEquals(ProgramState.Published, result.getState());
    assertEquals(fake.program2.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fake.program1.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Admin");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_excludesProgramsInEraseState() throws Exception {
    test.insert(Program.create(fake.user3, fake.library2, ProgramType.Main, ProgramState.Erase, "fonds", "C#", 120.0, 0.6));
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");
    Program subject = Program.create()
      .setName("cannons")
      .setLibraryId(UUID.randomUUID());

    try {
      testDAO.update(access, fake.program1.getId(), subject);

    } catch (Exception e) {
      Program result = testDAO.readOne(Access.internal(), fake.program1.getId());
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program subject = new Program()
      .setId(fake.program1.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    testDAO.update(access, fake.program1.getId(), subject);

    Program result = testDAO.readOne(Access.internal(), fake.program1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.library2.getId(), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Program or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a program originally belonging to Jenny
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Admin");
    Program subject = new Program()
      .setId(fake.program1.getId())
      .setKey("G minor 7")
      .setUserId(fake.user3.getId())
      .setDensity(1.0)
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setState("Published")
      .setTempo(129.4)
      .setType("Main");

    testDAO.update(access, fake.program1.getId(), subject);

    Program result = testDAO.readOne(Access.internal(), fake.program1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, fake.program2.getId());

    Assert.assertNotExist(testDAO, fake.program2.getId());
  }

  @Test
  public void destroy_succeedsAfterChosenForProduction() throws Exception {
    Access access = Access.create("Admin");
    Chain chain = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    fake.segment0 = test.insert(Segment.create(chain, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create()
      .setSegmentId(fake.segment0.getId())
      .setProgramId(fake.program1.getId())
      .setProgramSequenceBindingId(sequenceBinding1a_0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(-5));

    testDAO.destroy(access, fake.program2.getId());

    Assert.assertNotExist(testDAO, fake.program2.getId());
  }


  @Test
  public void erase() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Artist");
    fake.program35 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));

    testDAO.erase(access, fake.program35.getId());

    Program result = testDAO.readOne(Access.internal(), fake.program35.getId());
    assertNotNull(result);
    assertEquals(ProgramState.Erase, result.getState());
  }

  @Test
  public void erase_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.create();
    Access access = Access.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(CoreException.class);
    failure.expectMessage("Program does not exist");

    testDAO.erase(access, fake.program1.getId());
  }

  @Test
  public void erase_SucceedsEvenWithChildren() throws Exception {
    Access access = Access.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    fake.program35 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    ProgramSequence sequence1001a = test.insert(ProgramSequence.create(fake.program35, 16, "Intro", 0.6, "C", 120.0));
    test.insert(ProgramSequenceBinding.create(sequence1001a, 0));

    testDAO.erase(access, fake.program35.getId());

    Program result = testDAO.readOne(Access.internal(), fake.program35.getId());
    assertNotNull(result);
    assertEquals(ProgramState.Erase, result.getState());
  }
}

