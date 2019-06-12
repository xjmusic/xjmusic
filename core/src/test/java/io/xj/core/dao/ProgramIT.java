// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.work.WorkManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

// future test: permissions of different users to readMany vs. create vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramIT extends FixtureIT {
  @Spy
  private final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramDAO testDAO;

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
    insert(newAccount(1, "bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif"));
    insert(newUserRole(2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(newUserRole(3, UserRoleType.User));
    insert(newAccountUser(1, 3));

    // Library "palm tree" has program "fonds" and program "nuts"
    insert(newLibrary(1, 1, "palm tree", now()));
    program1 = newProgram(1, 3, 1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now());
    Sequence sequence1a = program1.add(newSequence(1, "Ants", 0.583, "D minor", 120.0));
    SequenceBinding sequenceBinding1a_0 = program1.add(newSequenceBinding(sequence1a, 0));
    program1.add(newSequenceBindingMeme(sequenceBinding1a_0, "leafy"));
    program1.add(newSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    insert(program1);
    insert(newProgram(2, 3, 1, ProgramType.Rhythm, ProgramState.Published, "nuts", "C#", 120.0, now()));

    // Library "boat" has program "helm" and program "sail"
    insert(newLibrary(2, 1, "boat", now()));
    program3 = newProgram(3, 3, 2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, now());
    Sequence sequence3a = program3.add(newSequence(16, "Ants", 0.583, "D minor", 120.0));
    program3.add(newSequenceBinding(sequence3a, 0));
    insert(newProgram(4, 3, 2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, now()));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Program subject = programFactory.newProgram()
      .setKey("G minor 7")
      .setUserId(BigInteger.valueOf(3))
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  /**
   [#156144567] Artist expects to create a Main-type program without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Program subject = programFactory.newProgram()
      .setKey("G minor 7")
      .setUserId(BigInteger.valueOf(3))
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Program subject = programFactory.newProgram()
      .setUserId(BigInteger.valueOf(3))
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons fifty nine");

    Program result = testDAO.clone(access, BigInteger.valueOf(1L), subject);

    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Program result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(ProgramType.Rhythm, result.getType());
    assertEquals(ProgramState.Published, result.getState());
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_excludesProgramsInEraseState() throws Exception {
    insert(newProgram(27, 3, 2, ProgramType.Main, ProgramState.Erase, "fonds", "C#", 120.0, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Program subject = programFactory.newProgram()
      .setName("cannons")
      .setLibraryId(BigInteger.valueOf(3L));

    try {
      testDAO.update(access, BigInteger.valueOf(1L), subject);

    } catch (Exception e) {
      Program result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
      assertSame(CoreException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Program subject = programFactory.newProgram()
      .setKey("G minor 7")
      .setUserId(BigInteger.valueOf(3))
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    testDAO.update(access, BigInteger.valueOf(1L), subject);

    Program result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Program or Instrument to always remain the same as when it was created, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2", // John will edit a program originally belonging to Jenny
      "roles", "Admin",
      "accounts", "1"
    ));
    Program subject = programFactory.newProgram()
      .setKey("G minor 7")
      .setUserId(BigInteger.valueOf(3))
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setState("Published")
      .setTempo(129.4)
      .setType("Main");

    testDAO.update(access, BigInteger.valueOf(1L), subject);

    Program result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
  }

  @Test
  public void destroy() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(2L));

    assertNotExist(testDAO, BigInteger.valueOf(2L));
  }

  @Test
  public void destroy_succeedsAfterChosenForProduction() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null, now());
    Segment segment0 = newSegment(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120.0, "chains-1-segments-9f7s89d8a7892.wav");
    segment0.add(new Choice()
      .setProgramId(BigInteger.valueOf(1))
      .setSequenceBindingId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(-5));

    testDAO.destroy(access, BigInteger.valueOf(2L));

    assertNotExist(testDAO, BigInteger.valueOf(2L));
  }


  @Test
  public void erase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    insert(newProgram(1001, 3, 2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()));

    testDAO.erase(access, BigInteger.valueOf(1001L));

    Program result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001L));
    assertNotNull(result);
    assertEquals(ProgramState.Erase, result.getState());
  }

  @Test
  public void erase_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("Program does not exist");

    testDAO.erase(access, BigInteger.valueOf(1L));
  }

  @Test
  public void erase_SucceedsEvenWithChildren() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program1001 = insert(newProgram(1001, 3, 2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()));
    Sequence sequence1001a = program1001.add(newSequence(16, "Intro", 0.6, "C", 120.0));
    program1001.add(newSequenceBinding(sequence1001a, 0));

    testDAO.erase(access, BigInteger.valueOf(1001L));

    Program result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001L));
    assertNotNull(result);
    assertEquals(ProgramState.Erase, result.getState());
  }
}

