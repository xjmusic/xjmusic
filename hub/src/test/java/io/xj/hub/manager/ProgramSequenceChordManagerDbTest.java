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
import io.xj.hub.tables.pojos.ProgramSequenceChord;
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
import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChord;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChordVoicing;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class ProgramSequenceChordManagerDbTest {
  private ProgramSequenceChordManager subject;

  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceChord sequenceChord1a_0;

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
    fake.program1_voiceBass = test.insert(buildProgramVoice(fake.program1, InstrumentType.Bass, "Bass"));
    fake.program1_voicePad = test.insert(buildProgramVoice(fake.program1, InstrumentType.Pad, "Pad"));
    fake.program1_voiceStripe = test.insert(buildProgramVoice(fake.program1, InstrumentType.Stripe, "Stripe"));
    fake.program1_voiceSticky = test.insert(buildProgramVoice(fake.program1, InstrumentType.Sticky, "Sticky"));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));
    sequenceChord1a_0 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C minor"));
    test.insert(buildProgramSequenceChord(fake.program1_sequence1, 2.0f, "D minor"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor"));
    fake.program3_chord1 = test.insert(buildProgramSequenceChord(fake.program3_sequence1, 0.0f, "G7 flat 6"));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test input
    ProgramVoiceManager programVoiceManager = new ProgramVoiceManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    subject = new ProgramSequenceChordManagerImpl(test.getEntityFactory(), programVoiceManager, test.getSqlStoreProvider());
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var input = new ProgramSequenceChord();
    input.setId(UUID.randomUUID());
    input.setProgramSequenceId(fake.program3_sequence1.getId());
    input.setProgramId(fake.program3.getId());
    input.setName("C Major");
    input.setPosition(2.0);

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(2.0, result.getPosition(), 0.01);
    assertEquals("C Major", result.getName());
  }

  /**
   * Lab should refuse to create or update chord to duplicate offset
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void create_refusedAtExistingOffset() {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var input = buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C minor");

    var e = assertThrows(ManagerException.class, () -> subject.create(access, input));

    assertEquals("Found Chord in sequence at position 0.000000", e.getMessage());
  }

  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var inputData = new ProgramSequenceChord();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramSequenceId(fake.program3_sequence1.getId());
    inputData.setProgramId(fake.program3.getId());
    inputData.setName("C Major");
    inputData.setPosition(2.0);

    var result = subject.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(2.0, result.getPosition(), 0.01);
    assertEquals("C Major", result.getName());
  }

  /**
   * In the chord search modal, click any voicing row (Bass, Stripe, etc.) to enable/disable whether that type of
   * voicing will be cloned with the chord. By default, Stripe and Sticky are disabled. The preference of which
   * voicings are cloned will be memorized for the program editor session. See attached preview.
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void cloneVoicings_onlyThoseSpecified() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var input = buildProgramSequenceChord(fake.program1_sequence1, 4.3f, "F- sus");
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceBass, "G3, Bb3, D4"));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceStripe, "G5, Bb5, D6"));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceSticky, "G4, Bb4, D5"));

    var result = subject.clone(access, sequenceChord1a_0.getId(), input, List.of(InstrumentType.Bass, InstrumentType.Stripe));

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(2),
        selectCount
          .from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(result.getClone().getId()))
          .fetchOne(0, int.class));
    }
  }

  /**
   * Chord Search while composing a main program
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void cloneVoicings() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var input = buildProgramSequenceChord(fake.program1_sequence1, 4.3f, "F- sus");
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceBass, "G3, Bb3, D4"));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceStripe, "G5, Bb5, D6"));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceSticky, "G4, Bb4, D5"));

    var result = subject.clone(access, sequenceChord1a_0.getId(), input, List.of(InstrumentType.Bass, InstrumentType.Stripe, InstrumentType.Sticky));

    assertEquals(fake.program1.getId(), result.getClone().getProgramId());
    assertEquals(fake.program1_sequence1.getId(), result.getClone().getProgramSequenceId());
    assertEquals("F- sus", result.getClone().getName());
    assertEquals(4.3, result.getClone().getPosition(), 0.1);
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(3),
        selectCount
          .from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(result.getClone().getId()))
          .fetchOne(0, int.class));
    }
  }

  /**
   * Chord Search while composing a main program
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void cloneVoicings_fromAnotherProgram() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceBass, "G3, Bb3, D4"));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceStripe, "G5, Bb5, D6"));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voiceSticky, "G4, Bb4, D5"));
    var program2_sequence1 = test.insert(buildProgramSequence(fake.program2, 4, "Ants", 0.583f, "D minor"));
    var input = buildProgramSequenceChord(program2_sequence1, 4.3f, "F- sus");

    var result = subject.clone(access, sequenceChord1a_0.getId(), input, List.of(InstrumentType.Bass, InstrumentType.Stripe, InstrumentType.Sticky));

    assertEquals(fake.program2.getId(), result.getClone().getProgramId());
    assertEquals(program2_sequence1.getId(), result.getClone().getProgramSequenceId());
    assertEquals("F- sus", result.getClone().getName());
    assertEquals(4.3, result.getClone().getPosition(), 0.1);
    try (var selectVoicing = test.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)) {
      var voicings = selectVoicing
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(result.getClone().getId()))
        .fetch();
      for (var voicing : voicings) {
        assertEquals(fake.program2.getId(), voicing.getProgramId());
        assertEquals(result.getClone().getId(), voicing.getProgramSequenceChordId());
      }
    }
  }

  /**
   * Lab should refuse to clone chord to duplicate offset
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void cloneVoicings_refusedAtExistingOffset() {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var input = buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "F- sus");

    var e = assertThrows(ManagerException.class, () -> subject.clone(access, sequenceChord1a_0.getId(), input, List.of(InstrumentType.Bass, InstrumentType.Stripe, InstrumentType.Sticky)));

    assertEquals("Found Chord in sequence at position 0.000000", e.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");

    var result = subject.readOne(access, sequenceChord1a_0.getId());

    assertNotNull(result);
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(0.0, result.getPosition(), 0.01);
    assertEquals("C minor", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceChord> result = subject.readMany(access, ImmutableList.of(fake.program1.getId()));

    assertEquals(2L, result.size());
  }

  /**
   * Chord Search while composing a main program
   * https://www.pivotaltracker.com/story/show/178921705
   * <p>
   * Only return 1 of any given chord name
   */
  @Test
  public void search() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
    test.reset();
    fake.account1 = test.insert(buildAccount("bananas"));
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program1_voiceBass = test.insert(buildProgramVoice(fake.program1, InstrumentType.Bass, "Bass"));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));

    var chordC = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C"));
    test.insert(buildProgramSequenceChordVoicing(chordC, fake.program1_voiceBass, "G3, Bb3, D4"));

    var chordCma1 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C major"));
    test.insert(buildProgramSequenceChordVoicing(chordCma1, fake.program1_voiceBass, "G3, Bb3, D4"));

    var chordCmi1 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C minor"));
    test.insert(buildProgramSequenceChordVoicing(chordCmi1, fake.program1_voiceBass, "G3, Bb3, D4"));

    var chordC7 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C7"));
    test.insert(buildProgramSequenceChordVoicing(chordC7, fake.program1_voiceBass, "G3, Bb3, D4"));

    var chordG = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "G"));
    test.insert(buildProgramSequenceChordVoicing(chordG, fake.program1_voiceBass, "G3, Bb3, D4"));

    var chordD = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "D"));
    test.insert(buildProgramSequenceChordVoicing(chordD, fake.program1_voiceBass, "G3, Bb3, D4"));

    assertEquals(4, subject.search(access, fake.library1.getId(), "c").size());
    assertEquals(4, subject.search(access, fake.library1.getId(), "C").size());
    assertEquals(2, subject.search(access, fake.library1.getId(), "C m").size());
    assertEquals(1, subject.search(access, fake.library1.getId(), "C maj").size());
    assertEquals(1, subject.search(access, fake.library1.getId(), "G").size());
  }

  /**
   * Chord Search requires at least one character of input text
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void search_requiresInputText() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    var e = assertThrows(ManagerException.class, () -> subject.search(access, fake.library1.getId(), ""));

    assertEquals("Search requires at least one character of text!", e.getMessage());
  }

  @Test
  public void update() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var input = buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "A# sus");

    var result = subject.update(access, sequenceChord1a_0.getId(), input);

    var updated = subject.readOne(access, sequenceChord1a_0.getId());
    assertEquals(sequenceChord1a_0.getId(), result.getId());
    assertEquals(sequenceChord1a_0.getId(), updated.getId());
    assertEquals(0.0, result.getPosition(), 0.01);
    assertEquals(0.0, updated.getPosition(), 0.01);
    assertEquals("A# sus", result.getName());
    assertEquals("A# sus", updated.getName());
  }

  /**
   * Lab should refuse to create or update chord to duplicate offset
   * https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void update_refusedAtExistingOffset() {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var input = buildProgramSequenceChord(fake.program1_sequence1, 2.0f, "A# sus");

    var e = assertThrows(ManagerException.class, () -> subject.update(access, sequenceChord1a_0.getId(), input));

    assertEquals("Found Chord in sequence at position 2.000000", e.getMessage());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");

    subject.destroy(access, sequenceChord1a_0.getId());

    assertEquals(1L, subject.readMany(access, ImmutableList.of(fake.program1.getId())).size());
  }

  /**
   * Artist editing main program deletes voicing along with chord https://www.pivotaltracker.com/story/show/175703981
   */
  @Test
  public void destroy_afterHasVoicing() throws Exception {
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, fake.program1_voicePad, "C5, Eb5, G5"));

    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");

    subject.destroy(access, sequenceChord1a_0.getId());

    assertEquals(1L, subject.readMany(access, ImmutableList.of(fake.program1.getId())).size());
  }

}

