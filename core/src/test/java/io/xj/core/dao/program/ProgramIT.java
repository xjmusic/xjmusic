// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.program;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentState;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.User;
import io.xj.core.model.UserRole;
import io.xj.core.model.UserRoleType;
import io.xj.core.work.WorkManager;
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
public class ProgramIT extends FixtureIT {
  @Spy
  private final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramDAO testDAO;
  private ProgramSequenceBinding sequenceBinding1a_0;

  @Before
  public void setUp() throws Exception {
    reset();

    // inject mocks
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      }));

    // Account "bananas"
    account1 = insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    insert(UserRole.create(user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(UserRole.create(user3, UserRoleType.User));
    insert(AccountUser.create(account1, user3));

    // Library "palm tree" has program "fonds" and program "nuts"
    library1 = insert(Library.create(account1, "palm tree", now()));
    program1 = insert(Program.create(user3, library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    ProgramSequence sequence1a = insert(ProgramSequence.create(program1, 4, "Ants", 0.583, "D minor", 120.0));
    sequenceBinding1a_0 = insert(ProgramSequenceBinding.create(sequence1a, 0));
    insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "leafy"));
    insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    program2 = insert(Program.create(user3, library1, ProgramType.Rhythm, ProgramState.Published, "nuts", "C#", 120.0, 0.6));

    // Library "boat" has program "helm" and program "sail"
    library2 = insert(Library.create(account1, "boat", now()));
    program3 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    ProgramSequence sequence3a = insert(ProgramSequence.create(program3, 16, "Ants", 0.583, "D minor", 120.0));
    insert(ProgramSequenceBinding.create(sequence3a, 0));
    program4 = insert(Program.create(user3, library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Program subject = Program.create()
      .setKey("G minor 7")
      .setUserId(user3.getId())
      .setLibraryId(library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(library2.getId(), result.getLibraryId());
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
    Access access = Access.create(user2, ImmutableList.of(account1), "User,Artist");
    Program inputData = Program.create()
      .setKey("G minor 7")
      .setUserId(user3.getId())
      .setLibraryId(library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Program inputData = Program.create()
      .setUserId(user3.getId())
      .setLibraryId(library2.getId())
      .setDensity(0.583)
      .setName("cannons fifty nine");

    Program result = testDAO.clone(access, program1.getId(), inputData);
    // TODO test clones sub-entities of program

    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(library2.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");

    Program result = testDAO.readOne(access, program2.getId());

    assertNotNull(result);
    assertEquals(ProgramType.Rhythm, result.getType());
    assertEquals(ProgramState.Published, result.getState());
    assertEquals(program2.getId(), result.getId());
    assertEquals(library1.getId(), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, program1.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Admin");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_excludesProgramsInEraseState() throws Exception {
    insert(Program.create(user3, library2, ProgramType.Main, ProgramState.Erase, "fonds", "C#", 120.0, 0.6));
    Access access = Access.create(ImmutableList.of(account1), "User");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");
    Program subject = Program.create()
      .setName("cannons")
      .setLibraryId(UUID.randomUUID());

    try {
      testDAO.update(access, program1.getId(), subject);

    } catch (Exception e) {
      Program result = testDAO.readOne(Access.internal(), program1.getId());
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(library1.getId(), result.getLibraryId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    Program subject = new Program()
      .setId(program1.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setUserId(user3.getId())
      .setLibraryId(library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    testDAO.update(access, program1.getId(), subject);

    Program result = testDAO.readOne(Access.internal(), program1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(library2.getId(), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Program or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a program originally belonging to Jenny
    Access access = Access.create(user2, ImmutableList.of(account1), "Admin");
    Program subject = new Program()
      .setId(program1.getId())
      .setKey("G minor 7")
      .setUserId(user3.getId())
      .setDensity(1.0)
      .setLibraryId(library2.getId())
      .setName("cannons")
      .setState("Published")
      .setTempo(129.4)
      .setType("Main");

    testDAO.update(access, program1.getId(), subject);

    Program result = testDAO.readOne(Access.internal(), program1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, program2.getId());

    assertNotExist(testDAO, program2.getId());
  }

  @Test
  public void destroy_succeedsAfterChosenForProduction() throws Exception {
    Access access = Access.create("Admin");
    Chain chain = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    Segment segment0 = insert(Segment.create(chain, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create()
      .setSegmentId(segment0.getId())
      .setProgramId(program1.getId())
      .setProgramSequenceBindingId(sequenceBinding1a_0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(-5));

    testDAO.destroy(access, program2.getId());

    assertNotExist(testDAO, program2.getId());
  }


  @Test
  public void erase() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    program35 = insert(Program.create(user3, library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));

    testDAO.erase(access, program35.getId());

    Program result = testDAO.readOne(Access.internal(), program35.getId());
    assertNotNull(result);
    assertEquals(ProgramState.Erase, result.getState());
  }

  @Test
  public void erase_failsIfNotInAccount() throws Exception {
    Account account2 = Account.create();
    Access access = Access.create(ImmutableList.of(account2), "Artist");

    failure.expect(CoreException.class);
    failure.expectMessage("Program does not exist");

    testDAO.erase(access, program1.getId());
  }

  @Test
  public void erase_SucceedsEvenWithChildren() throws Exception {
    Access access = Access.create(user2, ImmutableList.of(account1), "Artist");
    program35 = insert(Program.create(user3, library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    ProgramSequence sequence1001a = insert(ProgramSequence.create(program35, 16, "Intro", 0.6, "C", 120.0));
    insert(ProgramSequenceBinding.create(sequence1001a, 0));

    testDAO.erase(access, program35.getId());

    Program result = testDAO.readOne(Access.internal(), program35.getId());
    assertNotNull(result);
    assertEquals(ProgramState.Erase, result.getState());
  }
}

