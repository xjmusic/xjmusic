// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
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
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramDAO testDAO;
  private ProgramSequenceBinding sequenceBinding1a_0;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
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
    fake.library1 = test.insert(Library.create(fake.account1, "palm tree", Instant.now()));
    fake.program1 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    fake.programSequence1 = test.insert(ProgramSequence.create(fake.program1, 4, "Ants", 0.583, "D minor", 120.0));
    sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.create(fake.programSequence1, 0));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "chunk"));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Rhythm, ProgramState.Published, "nuts", "C#", 120.0, 0.6));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.create(fake.account1, "boat", Instant.now()));
    fake.program3 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fake.programSequence3 = test.insert(ProgramSequence.create(fake.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fake.programSequence3, 0));
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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program subject = Program.create()
      .setKey("G minor 7")
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(hubAccess, subject);

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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    Program inputData = Program.create()
      .setKey("G minor 7")
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState("Published")
      .setType("Main");

    Program result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  /**
   [#170290553] Clone sub-entities of program
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program inputData = Program.create()
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library2.getId())
      .setDensity(0.583)
      .setName("cannons fifty nine");
    test.insert(ProgramMeme.create(fake.program1, "cinnamon"));
    ProgramVoice voice = test.insert(ProgramVoice.create(fake.program1, InstrumentType.Percussive, "drums"));
    ProgramVoiceTrack track = test.insert(ProgramVoiceTrack.create(voice, "Kick"));
    test.insert(ProgramSequenceChord.create(fake.programSequence1, 0, "D"));
    ProgramSequencePattern pattern = test.insert(ProgramSequencePattern.create(fake.programSequence1, voice, ProgramSequencePatternType.Loop, 8, "jam"));
    test.insert(ProgramSequencePatternEvent.create(pattern, track, 0, 1, "C", 1));

    DAOCloner<Program> resultCloner = testDAO.clone(hubAccess, fake.program1.getId(), inputData);

    Program result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Program result = testDAO.readOne(hubAccess, fake.program2.getId());

    assertNotNull(result);
    assertEquals(ProgramType.Rhythm, result.getType());
    assertEquals(ProgramState.Published, result.getState());
    assertEquals(fake.program2.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.program1.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<Program> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User");

    Collection<Program> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    Program subject = Program.create()
      .setName("cannons")
      .setLibraryId(UUID.randomUUID());

    try {
      testDAO.update(hubAccess, fake.program1.getId(), subject);

    } catch (Exception e) {
      Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(ValueException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
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

    testDAO.update(hubAccess, fake.program1.getId(), subject);

    Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.library2.getId(), result.getLibraryId());
  }

  /**
   [#170390872] prevent user from changing program type of a Rhythm program, when it has any Tracks and/or Voices.
   */
  @Test
  public void update_failsToChangeTypeOfRhythmProgramWithVoice() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    test.insert(ProgramVoice.create(fake.program2, InstrumentType.Percussive, "Drums"));
    Program subject = new Program()
      .setId(fake.program2.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setUserId(fake.user3.getId())
      .setLibraryId(fake.library1.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");

    failure.expect(DAOException.class);
    failure.expectMessage("Found Voice in Program; Can't change type away from Rhythm");

    testDAO.update(hubAccess, fake.program2.getId(), subject);
  }

  /**
   [#156030760] Artist expects owner of Program or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a program originally belonging to Jenny
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Admin");
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

    testDAO.update(hubAccess, fake.program1.getId(), subject);

    Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    testDAO.destroy(hubAccess, fake.program2.getId());

    try {
      testDAO.readOne(HubAccess.internal(), fake.program2.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.program35 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));

    testDAO.destroy(hubAccess, fake.program35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM)
      .where(PROGRAM.ID.eq(fake.program35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.create();
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(DAOException.class);
    failure.expectMessage("Program belonging to you does not exist");

    testDAO.destroy(hubAccess, fake.program1.getId());
  }

  /**
   [#170299297] Cannot delete Programs that have a Meme
   */
  @Test
  public void destroy_failsIfHasMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Program program = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    test.insert(ProgramMeme.create(program, "frozen"));
    test.insert(ProgramMeme.create(program, "ham"));

    failure.expect(DAOException.class);
    failure.expectMessage("Found Program Meme");

    testDAO.destroy(hubAccess, program.getId());
  }

  /**
   [#170299297] As long as program has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program program = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6));
    ProgramSequence programSequence = test.insert(ProgramSequence.create(program, 4, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(programSequence, 0));
    ProgramVoice voice = test.insert(ProgramVoice.create(program, InstrumentType.Percussive, "drums"));
    ProgramVoiceTrack track = test.insert(ProgramVoiceTrack.create(voice, "Kick"));
    test.insert(ProgramSequenceChord.create(programSequence, 0, "D"));
    ProgramSequencePattern pattern = test.insert(ProgramSequencePattern.create(programSequence, voice, ProgramSequencePatternType.Loop, 8, "jam"));
    test.insert(ProgramSequencePatternEvent.create(pattern, track, 0, 1, "C", 1));

    testDAO.destroy(hubAccess, program.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM)
      .where(PROGRAM.ID.eq(program.getId()))
      .fetchOne(0, int.class));
  }
}

