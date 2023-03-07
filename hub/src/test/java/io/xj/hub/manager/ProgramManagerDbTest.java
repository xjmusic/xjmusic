// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.app.AppEnvironment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.tables.Program.PROGRAM;
import static io.xj.hub.tables.ProgramMeme.PROGRAM_MEME;
import static io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE;
import static io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.hub.tables.ProgramVoice.PROGRAM_VOICE;
import static io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramManagerDbTest {
  private ProgramManager subject;
  private ProgramVoiceManager voiceManager;
  private ProgramSequenceChordManager chordManager;
  private ProgramSequenceChordVoicingManager voicingManager;
  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = AppEnvironment.getDefault();
    test = HubIntegrationTestFactory.build(env);
    voiceManager = new ProgramVoiceManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    chordManager = new ProgramSequenceChordManagerImpl(test.getEntityFactory(), voiceManager, test.getSqlStoreProvider());
    voicingManager = new ProgramSequenceChordVoicingManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Accounts
    fake.account1 = test.insert(buildAccount("bananas"));
    fake.account2 = test.insert(buildAccount("apples"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "palm tree" has a program "fonds" and program "nuts"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0f, 0.6f));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));
    var sequenceBinding1a_0 = test.insert(buildProgramSequenceBinding(fake.program1_sequence1, 0));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "chunk"));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "nuts", "C#", 120.0f, 0.6f));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor"));
    test.insert(buildProgramSequenceBinding(fake.program3_sequence1, 0));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Library in different account
    fake.library3 = test.insert(buildLibrary(fake.account2, "car"));

    // Instantiate the test subject
    subject = new ProgramManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Program input = new Program();
    input.setKey("G minor 7");
    input.setLibraryId(fake.library2.getId());
    input.setName("cannons");
    input.setTempo(129.4f);
    input.setDensity(0.6f);
    input.setState(ProgramState.Published);
    input.setType(ProgramType.Main);

    Program result = subject.create(access, input);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  /**
   * Artist expects to of a Main-type program without crashing the entire platform https://www.pivotaltracker.com/story/show/156144567
   * NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Program input = new Program();
    input.setId(UUID.randomUUID());
    input.setKey("G minor 7");
    input.setLibraryId(fake.library2.getId());
    input.setName("cannons");
    input.setTempo(129.4f);
    input.setDensity(0.6f);
    input.setState(ProgramState.Published);
    input.setType(ProgramType.Main);

    Program result = subject.create(access, input);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
  }

  /**
   * Programs/Instruments can be cloned/moved between accounts https://www.pivotaltracker.com/story/show/181878883
   */
  @Test
  public void clone_toLibraryInDifferentAccount() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1, fake.account2));
    Program input = new Program();
    input.setLibraryId(fake.library3.getId());
    input.setName("porcupines");

    ManagerCloner<Program> resultCloner = subject.clone(access, fake.program1.getId(), input);

    Program result = subject.readOne(HubAccess.internal(), resultCloner.getClone().getId());
    assertEquals(fake.library3.getId(), result.getLibraryId());
    assertEquals("porcupines", result.getName());
  }

  /**
   * Programs/Instruments can be cloned/moved between accounts https://www.pivotaltracker.com/story/show/181878883
   */
  @Test
  public void clone_mainProgramIncludesChordVoicings() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1, fake.account1));
    var padVoice = test.insert(buildProgramVoice(fake.program1, InstrumentType.Pad, "Pad"));
    var padChord0 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "D"));
    var padChord1 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 1.0f, "Dbm"));
    test.insert(buildProgramSequenceChordVoicing(padChord0, padVoice, "D4,F#4,A4"));
    test.insert(buildProgramSequenceChordVoicing(padChord1, padVoice, "Db4,E4,Ab4"));
    Program input = new Program();
    input.setName("porcupines");

    ManagerCloner<Program> resultCloner = subject.clone(access, fake.program1.getId(), input);

    Program result = subject.readOne(HubAccess.internal(), resultCloner.getClone().getId());
    var resultVoice = voiceManager.readMany(HubAccess.internal(), List.of(result.getId())).stream().findFirst().orElseThrow();
    assertEquals("Pad", resultVoice.getName());
    //
    var resultChords = chordManager.readMany(HubAccess.internal(), List.of(result.getId()));
    var resultChord0 = resultChords.stream().filter(c -> c.getPosition() == 0.0f).findAny().orElseThrow();
    var resultChord1 = resultChords.stream().filter(c -> c.getPosition() == 1.0f).findAny().orElseThrow();
    //
    var resultVoicings = voicingManager.readMany(HubAccess.internal(), List.of(result.getId()));
    var resultVoicing0 = resultVoicings.stream().filter(v -> Objects.equals(resultChord0.getId(), v.getProgramSequenceChordId())).findAny().orElseThrow();
    assertEquals(resultVoice.getId(), resultVoicing0.getProgramVoiceId());
    assertEquals(resultChord0.getId(), resultVoicing0.getProgramSequenceChordId());
    var resultVoicing1 = resultVoicings.stream().filter(v -> Objects.equals(resultChord1.getId(), v.getProgramSequenceChordId())).findAny().orElseThrow();
    assertEquals(resultVoice.getId(), resultVoicing1.getProgramVoiceId());
    assertEquals(resultChord1.getId(), resultVoicing1.getProgramSequenceChordId());
  }

  /**
   * Clone sub-entities of program https://www.pivotaltracker.com/story/show/170290553
   * <p>
   * Cloned Program should have same Voices and Chord Voicings https://www.pivotaltracker.com/story/show/175808105
   * <p>
   * Artist expects to be able to clone Program without error https://www.pivotaltracker.com/story/show/175947247
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Program input = new Program();
    input.setLibraryId(fake.library2.getId());
    input.setName("cannons fifty nine");
    test.insert(buildProgramMeme(fake.program1, "cinnamon"));
    var voice = test.insert(buildProgramVoice(fake.program1, InstrumentType.Drum, "drums"));
    var track = test.insert(buildProgramVoiceTrack(voice, "Kick"));
    var programSequenceChord = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "D"));
    test.insert(buildProgramSequenceChordVoicing(programSequenceChord, voice, "D2,F#2,A2"));
    var pattern = test.insert(buildProgramSequencePattern(fake.program1_sequence1, voice, 8, "jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));

    ManagerCloner<Program> resultCloner = subject.clone(access, fake.program1.getId(), input);

    Program result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.6, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
    // Cloned ProgramMeme
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramMeme.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_MEME).where(PROGRAM_MEME.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramVoice.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramVoiceTrack belongs to ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramVoiceTrack.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_VOICE_TRACK).where(PROGRAM_VOICE_TRACK.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramSequence.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_SEQUENCE).where(PROGRAM_SEQUENCE.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceChord belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramSequenceChord.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_SEQUENCE_CHORD).where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceChordVoicing belongs to ProgramSequenceChord
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramSequenceChordVoicing.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_SEQUENCE_CHORD_VOICING).where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceBinding belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramSequenceBinding.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_SEQUENCE_BINDING).where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
    assertEquals(2, resultCloner.getChildClones().stream().filter(e -> ProgramSequenceBindingMeme.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(2), selectCount.from(PROGRAM_SEQUENCE_BINDING_MEME).where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramSequencePattern.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_SEQUENCE_PATTERN).where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
    // Cloned ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
    assertEquals(1, resultCloner.getChildClones().stream().filter(e -> ProgramSequencePatternEvent.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1), selectCount.from(PROGRAM_SEQUENCE_PATTERN_EVENT).where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.eq(result.getId())).fetchOne(0, int.class));
    }
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    Program result = subject.readOne(access, fake.program2.getId());

    assertNotNull(result);
    assertEquals(ProgramType.Beat, result.getType());
    assertEquals(ProgramState.Published, result.getState());
    assertEquals(fake.program2.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User");

    var e = assertThrows(ManagerException.class, () -> subject.readOne(access, fake.program1.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  /**
   * Fabricator should get distinct Chord Voicing Types https://www.pivotaltracker.com/story/show/176372189
   */
  @Test
  public void readManyWithChildEntities() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));

    test.insert(buildProgramMeme(fake.program1, "cinnamon"));
    var voice = test.insert(buildProgramVoice(fake.program1, InstrumentType.Drum, "drums"));
    var track = test.insert(buildProgramVoiceTrack(voice, "Kick"));
    var programSequenceChord = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "D"));
    test.insert(buildProgramSequenceChordVoicing(programSequenceChord, voice, "D2,F#2,A2"));
    var pattern = test.insert(buildProgramSequencePattern(fake.program1_sequence1, voice, 8, "jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));

    Collection<Object> results = subject.readManyWithChildEntities(access, ImmutableList.of(fake.program1.getId()));

    assertEquals(12, results.size());
    assertContains(Program.class, 1, results);
    assertContains(ProgramMeme.class, 1, results);
    assertContains(ProgramVoice.class, 1, results);
    assertContains(ProgramVoiceTrack.class, 1, results);
    assertContains(ProgramSequence.class, 1, results);
    assertContains(ProgramSequenceBinding.class, 1, results);
    assertContains(ProgramSequenceBindingMeme.class, 2, results);
    assertContains(ProgramSequencePattern.class, 1, results);
    assertContains(ProgramSequencePatternEvent.class, 1, results);
    assertContains(ProgramSequenceChord.class, 1, results);
    assertContains(ProgramSequenceChordVoicing.class, 1, results);
  }

  /**
   * Assert that the results contain an exact count of classes in the results
   *
   * @param type    of class to search for
   * @param total   count of instances to assert
   * @param results to search within
   * @param <N>     type of entity
   */
  private <N> void assertContains(Class<N> type, int total, Collection<Object> results) {
    assertEquals(String.format("Exactly %s count of %s class in results", total, type.getSimpleName()), total, results.stream().filter(r -> type.isAssignableFrom(r.getClass())).count());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User");

    Collection<Program> result = subject.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");
    Program input = new Program();
    input.setId(UUID.randomUUID());
    input.setName("cannons");
    input.setLibraryId(UUID.randomUUID());

    try {
      subject.update(access, fake.program1.getId(), input);

    } catch (Exception e) {
      Program result = subject.readOne(HubAccess.internal(), fake.program1.getId());
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(ManagerException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Program input = new Program();
    input.setId(fake.program1.getId());
    input.setDensity(1.0f);
    input.setKey("G minor 7");
    input.setLibraryId(fake.library2.getId());
    input.setName("cannons");
    input.setTempo(129.4f);
    input.setState(ProgramState.Published);
    input.setType(ProgramType.Main);

    subject.update(access, fake.program1.getId(), input);

    Program result = subject.readOne(HubAccess.internal(), fake.program1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.library2.getId(), result.getLibraryId());
  }

  /**
   * Artist should always be able to change program type https://www.pivotaltracker.com/story/show/175789099
   * <p>
   * DEPRECATES prevent user from changing the type of Beat program, when it has any Tracks and/or Voices. https://www.pivotaltracker.com/story/show/170390872
   */
  @Test
  public void update_artistCanAlwaysChangeType() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));
    Program input = new Program();
    input.setId(fake.program2.getId());
    input.setDensity(1.0f);
    input.setKey("G minor 7");
    input.setLibraryId(fake.library1.getId());
    input.setName("cannons");
    input.setTempo(129.4f);
    input.setState(ProgramState.Published);
    input.setType(ProgramType.Main);

    subject.update(access, fake.program2.getId(), input);
  }

  /**
   * Artist expects owner of Program or Instrument to always remain the same as when it was ofd, even after being updated by another user. https://www.pivotaltracker.com/story/show/156030760
   * DEPRECATED, future will be replaced by Instruments and Programs have author history https://www.pivotaltracker.com/story/show/166724453
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a program originally belonging to Jenny
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Program input = new Program();
    input.setId(fake.program1.getId());
    input.setKey("G minor 7");
    input.setDensity(1.0f);
    input.setLibraryId(fake.library2.getId());
    input.setName("cannons");
    input.setState(ProgramState.Published);
    input.setTempo(129.4f);
    input.setType(ProgramType.Main);

    subject.update(access, fake.program1.getId(), input);

    Program result = subject.readOne(HubAccess.internal(), fake.program1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    subject.destroy(access, fake.program2.getId());

    try {
      subject.readOne(HubAccess.internal(), fake.program2.getId());
      fail();
    } catch (ManagerException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Artist");
    fake.program35 = test.insert(buildProgram(fake.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0f, 0.6f));

    subject.destroy(access, fake.program35.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0), selectCount.from(PROGRAM).where(PROGRAM.ID.eq(fake.program35.getId())).and(PROGRAM.IS_DELETED.eq(false)).fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account2), "Artist");

    var e = assertThrows(ManagerException.class, () -> subject.destroy(access, fake.program1.getId()));
    assertEquals("Program belonging to you does not exist", e.getMessage());
  }

  /**
   * Cannot delete Programs that have a Meme https://www.pivotaltracker.com/story/show/170299297
   */
  @Test
  public void destroy_evenWithMemes() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Program program = test.insert(buildProgram(fake.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0f, 0.6f));
    test.insert(buildProgramMeme(program, "frozen"));
    test.insert(buildProgramMeme(program, "ham"));

    subject.destroy(access, program.getId());
  }

  /**
   * As long as program has no meme, destroy all other inner entities https://www.pivotaltracker.com/story/show/170299297
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Program program = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0f, 0.6f));
    var programSequence = test.insert(buildProgramSequence(program, 4, "Ants", 0.583f, "D minor"));
    test.insert(buildProgramSequenceBinding(programSequence, 0));
    var voice = test.insert(buildProgramVoice(program, InstrumentType.Drum, "drums"));
    var track = test.insert(buildProgramVoiceTrack(voice, "Kick"));
    test.insert(buildProgramSequenceChord(programSequence, 0.0f, "D"));
    var pattern = test.insert(buildProgramSequencePattern(programSequence, voice, 8, "jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));

    subject.destroy(access, program.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0), selectCount.from(PROGRAM).where(PROGRAM.ID.eq(program.getId())).and(PROGRAM.IS_DELETED.eq(false)).fetchOne(0, int.class));
    }
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    Collection<Program> result = subject.readMany(access, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

}

