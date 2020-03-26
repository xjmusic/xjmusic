// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.AccountUser;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.model.InstrumentType;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.ProgramSequenceChord;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.ProgramSequencePatternEvent;
import io.xj.service.hub.model.ProgramSequencePatternType;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.ProgramVoiceTrack;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.model.User;
import io.xj.service.hub.model.UserRole;
import io.xj.service.hub.model.UserRoleType;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.Assert;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.hub.testing.InternalResources;
import io.xj.service.hub.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.service.hub.tables.Program.PROGRAM;
import static io.xj.service.hub.tables.ProgramMeme.PROGRAM_MEME;
import static io.xj.service.hub.tables.ProgramSequence.PROGRAM_SEQUENCE;
import static io.xj.service.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static io.xj.service.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.service.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD;
import static io.xj.service.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.service.hub.tables.ProgramVoice.PROGRAM_VOICE;
import static io.xj.service.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK;
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
  private IntegrationTestingFixtures fixture;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new IntegrationTestModule()));
    workManager = injector.getInstance(WorkManager.class);
    injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubModule(), new IntegrationTestModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fixture = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fixture.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fixture.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(fixture.user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    fixture.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(fixture.user3, UserRoleType.User));
    test.insert(AccountUser.create(fixture.account1, fixture.user3));

    // Library "palm tree" has program "fonds" and program "nuts"
    fixture.library1 = test.insert(Library.create(fixture.account1, "palm tree", InternalResources.now()));
    fixture.program1 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    fixture.programSequence1 = test.insert(ProgramSequence.create(fixture.program1, 4, "Ants", 0.583, "D minor", 120.0));
    sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.create(fixture.programSequence1, 0));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "chunk"));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    fixture.program2 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Rhythm, ProgramState.Published, "nuts", "C#", 120.0, 0.6));

    // Library "boat" has program "helm" and program "sail"
    fixture.library2 = test.insert(Library.create(fixture.account1, "boat", InternalResources.now()));
    fixture.program3 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fixture.programSequence3 = test.insert(ProgramSequence.create(fixture.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fixture.programSequence3, 0));
    fixture.program4 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    Program subject = Program.create()
      .setKey("G minor 7")
      .setUserId(fixture.user3.getId())
      .setLibraryId(fixture.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fixture.library2.getId(), result.getLibraryId());
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
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "User,Artist");
    Program inputData = Program.create()
      .setKey("G minor 7")
      .setUserId(fixture.user3.getId())
      .setLibraryId(fixture.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fixture.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  /**
   [#170290553] Clone sub-entities of program
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    Program inputData = Program.create()
      .setUserId(fixture.user3.getId())
      .setLibraryId(fixture.library2.getId())
      .setDensity(0.583)
      .setName("cannons fifty nine");
    test.insert(ProgramMeme.create(fixture.program1, "cinnamon"));
    ProgramVoice voice = test.insert(ProgramVoice.create(fixture.program1, InstrumentType.Percussive, "drums"));
    ProgramVoiceTrack track = test.insert(ProgramVoiceTrack.create(voice, "Kick"));
    test.insert(ProgramSequenceChord.create(fixture.programSequence1, 0, "D"));
    ProgramSequencePattern pattern = test.insert(ProgramSequencePattern.create(fixture.programSequence1, voice, ProgramSequencePatternType.Loop, 8, "jam"));
    test.insert(ProgramSequencePatternEvent.create(pattern, track, 0, 1, "C", 1));

    DAOCloner<Program> resultCloner = testDAO.clone(access, fixture.program1.getId(), inputData);

    Program result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fixture.library2.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
    // Cloned ProgramMeme
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_MEME)
      .where(PROGRAM_MEME.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoice.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramVoiceTrack belongs to ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoiceTrack.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequence.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChord belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChord.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBinding belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBinding.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
    assertEquals(2, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBindingMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(2), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePattern.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePatternEvent.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User");

    Program result = testDAO.readOne(access, fixture.program2.getId());

    assertNotNull(result);
    assertEquals(ProgramType.Rhythm, result.getType());
    assertEquals(ProgramState.Published, result.getState());
    assertEquals(fixture.program2.getId(), result.getId());
    assertEquals(fixture.library1.getId(), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(HubException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fixture.program1.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Admin");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(fixture.library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Program> result = testDAO.readMany(access, ImmutableList.of(fixture.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User");
    Program subject = Program.create()
      .setName("cannons")
      .setLibraryId(UUID.randomUUID());

    try {
      testDAO.update(access, fixture.program1.getId(), subject);

    } catch (Exception e) {
      Program result = testDAO.readOne(Access.internal(), fixture.program1.getId());
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(fixture.library1.getId(), result.getLibraryId());
      assertSame(ValueException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    Program subject = new Program()
      .setId(fixture.program1.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setUserId(fixture.user3.getId())
      .setLibraryId(fixture.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    testDAO.update(access, fixture.program1.getId(), subject);

    Program result = testDAO.readOne(Access.internal(), fixture.program1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fixture.library2.getId(), result.getLibraryId());
  }

  /**
   [#170390872] prevent user from changing program type of a Rhythm program, when it has any Tracks and/or Voices.
   */
  @Test
  public void update_failsToChangeTypeOfRhythmProgramWithVoice() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    test.insert(ProgramVoice.create(fixture.program2, InstrumentType.Percussive, "Drums"));
    Program subject = new Program()
      .setId(fixture.program2.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setUserId(fixture.user3.getId())
      .setLibraryId(fixture.library1.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    failure.expect(HubException.class);
    failure.expectMessage("Found Voice in Program; Can't change type away from Rhythm");

    testDAO.update(access, fixture.program2.getId(), subject);
  }

  /**
   [#156030760] Artist expects owner of Program or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a program originally belonging to Jenny
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Admin");
    Program subject = new Program()
      .setId(fixture.program1.getId())
      .setKey("G minor 7")
      .setUserId(fixture.user3.getId())
      .setDensity(1.0)
      .setLibraryId(fixture.library2.getId())
      .setName("cannons")
      .setState("Published")
      .setTempo(129.4)
      .setType("Main");

    testDAO.update(access, fixture.program1.getId(), subject);

    Program result = testDAO.readOne(Access.internal(), fixture.program1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, fixture.program2.getId());

    Assert.assertNotExist(testDAO, fixture.program2.getId());
  }

  @Test
  public void destroy_succeedsAfterChosenForProduction() throws Exception {
    Access access = Access.create("Admin");
    Chain chain = test.insert(Chain.create(fixture.account1, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    fixture.segment0 = test.insert(Segment.create(chain, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create()
      .setSegmentId(fixture.segment0.getId())
      .setProgramId(fixture.program1.getId())
      .setProgramSequenceBindingId(sequenceBinding1a_0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(-5));

    testDAO.destroy(access, fixture.program2.getId());

    Assert.assertNotExist(testDAO, fixture.program2.getId());
  }


  @Test
  public void destroy_asArtist() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Artist");
    fixture.program35 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));

    testDAO.destroy(access, fixture.program35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM)
      .where(PROGRAM.ID.eq(fixture.program35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fixture.account2 = Account.create();
    Access access = Access.create(ImmutableList.of(fixture.account2), "Artist");

    failure.expect(HubException.class);
    failure.expectMessage("Program belonging to you does not exist");

    testDAO.destroy(access, fixture.program1.getId());
  }

  /**
   [#170299297] Cannot delete Programs that have a Meme
   */
  @Test
  public void destroy_failsIfHasMemes() throws Exception {
    Access access = Access.create("Admin");
    Program program = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    test.insert(ProgramMeme.create(program, "frozen"));
    test.insert(ProgramMeme.create(program, "ham"));

    failure.expect(HubException.class);
    failure.expectMessage("Found Program Meme");

    testDAO.destroy(access, program.getId());
  }

  /**
   [#170299297] As long as program has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    Program program = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    ProgramSequence programSequence = test.insert(ProgramSequence.create(program, 4, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(programSequence, 0));
    ProgramVoice voice = test.insert(ProgramVoice.create(program, InstrumentType.Percussive, "drums"));
    ProgramVoiceTrack track = test.insert(ProgramVoiceTrack.create(voice, "Kick"));
    test.insert(ProgramSequenceChord.create(programSequence, 0, "D"));
    ProgramSequencePattern pattern = test.insert(ProgramSequencePattern.create(programSequence, voice, ProgramSequencePatternType.Loop, 8, "jam"));
    test.insert(ProgramSequencePatternEvent.create(pattern, track, 0, 1, "C", 1));

    testDAO.destroy(access, program.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM)
      .where(PROGRAM.ID.eq(program.getId()))
      .fetchOne(0, int.class));
  }
}

