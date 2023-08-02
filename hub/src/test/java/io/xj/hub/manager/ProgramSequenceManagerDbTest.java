// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBindingMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChord;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChordVoicing;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequencePattern;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequencePatternEvent;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoiceTrack;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE;
import static io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class ProgramSequenceManagerDbTest {
  ProgramSequenceManager testManager;

  HubIntegrationTest test;
  IntegrationTestingFixtures fake;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @Autowired
  HubIntegrationTestFactory integrationTestFactory;

  @BeforeEach
  public void setUp() throws Exception {
    test = integrationTestFactory.build();
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "palm tree" has a program "Ants" and program "Ants"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));
    var sequenceBinding1a_0 = test.insert(buildProgramSequenceBinding(fake.program1_sequence1, 0));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "chunk"));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor"));
    test.insert(buildProgramSequenceBinding(fake.program3_sequence1, 0));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testManager = new ProgramSequenceManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    var subject = new ProgramSequence();
    subject.setId(UUID.randomUUID());
    subject.setKey("G minor 7");
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");
    subject.setTotal((short) 4);
    subject.setDensity(0.6f);

    var result = testManager.create(
      access, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("cannons", result.getName());
  }

  /**
   * Artist expects to of a Main-type programSequence without crashing the entire platform https://www.pivotaltracker.com/story/show/156144567
   * NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    var inputData = new ProgramSequence();
    inputData.setId(UUID.randomUUID());
    inputData.setKey("G minor 7");
    inputData.setProgramId(fake.program3.getId());
    inputData.setName("cannons");
    inputData.setTotal((short) 4f);
    inputData.setDensity(0.6f);

    var result = testManager.create(
      access, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("cannons", result.getName());
  }

  /**
   * Voicing Lists missing when cloning Main Program Sequence
   * https://www.pivotaltracker.com/story/show/182286657
   * <p>
   * Clone sub-entities of programSequence https://www.pivotaltracker.com/story/show/170290553
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    var inputData = new ProgramSequence();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setDensity(0.583f);
    inputData.setKey("C#");
    inputData.setName("cannons fifty nine");
    test.insert(buildProgramMeme(fake.program1, "cinnamon"));
    var voice = test.insert(buildProgramVoice(fake.program1, InstrumentType.Pad, "Test"));
    var track = test.insert(buildProgramVoiceTrack(voice, "Test"));
    var chord = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "D"));
    test.insert(buildProgramSequenceChordVoicing(chord, voice, "C3,E3,G3,C4,E4,G4"));
    var pattern = test.insert(buildProgramSequencePattern(fake.program1_sequence1, voice, 8, "jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));

    ManagerCloner<ProgramSequence> resultCloner = testManager.clone(access, fake.program1_sequence1.getId(), inputData);

    var result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("cannons fifty nine", result.getName());
    // Cloned ProgramSequence
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1),
        selectCount.from(PROGRAM_SEQUENCE)
          .where(PROGRAM_SEQUENCE.ID.eq(result.getId()))
          .fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceChord belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChord.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1),
        selectCount.from(PROGRAM_SEQUENCE_CHORD)
          .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(result.getId()))
          .fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceChordVoicing belongs to ProgramSequenceChord and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChordVoicing.class.equals(e.getClass())).count());
    try (
      var selectCount = test.getDSL().selectCount();
      var joinVoicing = selectCount.from(PROGRAM_SEQUENCE_CHORD_VOICING)
        .join(PROGRAM_SEQUENCE_CHORD).on(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(PROGRAM_SEQUENCE_CHORD.ID))
    ) {
      assertEquals(Integer.valueOf(1),
        joinVoicing
          .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(result.getId()))
          .fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceBinding belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBinding.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1),
        selectCount.from(PROGRAM_SEQUENCE_BINDING)
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.eq(result.getId()))
          .fetchOne(0, int.class));
    }
    // Cloned ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
    assertEquals(2, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBindingMeme.class.equals(e.getClass())).count());
    try (
      var selectCount = test.getDSL().selectCount();
      var joinMeme = selectCount.from(PROGRAM_SEQUENCE_BINDING_MEME)
        .join(PROGRAM_SEQUENCE_BINDING).on(PROGRAM_SEQUENCE_BINDING.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID))
    ) {
      assertEquals(Integer.valueOf(2),
        joinMeme
          .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.eq(result.getId()))
          .fetchOne(0, int.class));
    }
    // Cloned ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePattern.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1),
        selectCount.from(PROGRAM_SEQUENCE_PATTERN)
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(result.getId()))
          .fetchOne(0, int.class));
    }
    // Cloned ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePatternEvent.class.equals(e.getClass())).count());
    try (
      var selectCount = test.getDSL().selectCount();
      var joinEvent = selectCount.from(PROGRAM_SEQUENCE_PATTERN_EVENT)
        .join(PROGRAM_SEQUENCE_PATTERN).on(PROGRAM_SEQUENCE_PATTERN.ID.eq(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID))
    ) {
      assertEquals(Integer.valueOf(1),
        joinEvent
          .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(result.getId()))
          .fetchOne(0, int.class));
    }
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User, Artist");

    var result = testManager.readOne(access, fake.program3_sequence1.getId());

    assertNotNull(result);
    assertEquals(fake.program3_sequence1.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Ants", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(buildAccount("Testing")), "User, Artist");

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(access, fake.program3_sequence1.getId()));

    assertEquals("Record does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Admin");

    Collection<ProgramSequence> result = testManager.readMany(access, List.of(fake.program3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequence> resultIt = result.iterator();
    assertEquals("Ants", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramSequence> result = testManager.readMany(access, List.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "User, Artist");
    var subject = new ProgramSequence();
    subject.setId(UUID.randomUUID());
    subject.setName("cannons");
    subject.setProgramId(UUID.randomUUID());

    var e = assertThrows(ManagerException.class, () -> testManager.update(access, fake.program3_sequence1.getId(), subject));

    var result = testManager.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
    assertNotNull(result);
    assertEquals("Ants", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertSame(ManagerException.class, e.getClass());
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    var subject = new ProgramSequence();
    subject.setId(fake.program3_sequence1.getId());
    subject.setDensity(1.0f);
    subject.setKey("G minor 7");
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");
    subject.setTotal((short) 4);

    testManager.update(access, fake.program3_sequence1.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  /**
   * Artist expects owner of ProgramSequence or Instrument to always remain the same as when it was ofd, even after being updated by another user. https://www.pivotaltracker.com/story/show/156030760
   * DEPRECATED, future will be replaced by Instruments and Programs have author history https://www.pivotaltracker.com/story/show/166724453
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a programSequence originally belonging to Jenny
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), List.of(fake.account1));
    var subject = new ProgramSequence();
    subject.setId(fake.program3_sequence1.getId());
    subject.setKey("G minor 7");
    subject.setDensity(1.0f);
    subject.setProgramId(fake.program3.getId());
    subject.setTotal((short) 4);
    subject.setName("cannons");

    testManager.update(access, fake.program3_sequence1.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account1), "Artist");
    fake.programSequence35 = test.insert(buildProgramSequence(fake.program2, 16, "Ants", 0.6f, "C#"));

    testManager.destroy(access, fake.programSequence35.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0),
        selectCount
          .from(PROGRAM_SEQUENCE)
          .where(PROGRAM_SEQUENCE.ID.eq(fake.programSequence35.getId()))
          .fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), List.of(fake.account2), "Artist");

    var e = assertThrows(ManagerException.class, () -> testManager.destroy(access, fake.program3_sequence1.getId()));

    assertEquals("Sequence in Program in Account you have access to does not exist", e.getMessage());
  }

  /**
   * Delete a **Sequence** even if it has children, as long as it has no sequence bindings https://www.pivotaltracker.com/story/show/170390872
   */
  @Test
  public void destroy_succeedsEvenWhenHasPattern() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    var programSequence = test.insert(buildProgramSequence(fake.program2, 16, "Ants", 0.6f, "C#"));
    test.insert(buildProgramSequencePattern(programSequence, fake.program702_voice1, 4, "Jam"));

    testManager.destroy(access, programSequence.getId());
  }

  /**
   * Artist editing program should be able to delete sequences https://www.pivotaltracker.com/story/show/175693261
   * <p>
   * DEPRECATES Delete a **Sequence** even if it has children, as long as it has no sequence bindings https://www.pivotaltracker.com/story/show/170390872
   */
  @Test
  public void destroy_succeedsEvenWithChildren() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    var programVoice = test.insert(buildProgramVoice(fake.program3, InstrumentType.Stripe, "Stripe"));
    var track = test.insert(buildProgramVoiceTrack(programVoice, "KICK"));
    var programSequence = test.insert(buildProgramSequence(fake.program2, 16, "Ants", 0.6f, "C#"));
    var programSequenceBinding = test.insert(buildProgramSequenceBinding(programSequence, 0));
    test.insert(buildProgramSequenceBindingMeme(programSequenceBinding, "chunk"));
    var pattern = test.insert(buildProgramSequencePattern(programSequence, fake.program702_voice1, 4, "Jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));
    var programSequenceChord = test.insert(buildProgramSequenceChord(programSequence, 0.0, "G"));
    test.insert(buildProgramSequenceChordVoicing(programSequenceChord, programVoice, "C5, Eb5, G5"));

    testManager.destroy(access, programSequence.getId());
  }

}

