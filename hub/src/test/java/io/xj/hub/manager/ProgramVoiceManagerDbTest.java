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
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.app.AppEnvironment;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.tables.ProgramVoice.PROGRAM_VOICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@SpringBootTest
public class ProgramVoiceManagerDbTest {
  private ProgramVoiceManager subject;

  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;
  private HubSqlStoreProvider sqlStoreProvider;

  @BeforeEach
  public void setUp() throws Exception {
    var env = AppEnvironment.getDefault();
    test = HubIntegrationTestFactory.build(env);
    sqlStoreProvider = test.getSqlStoreProvider();
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
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));
    fake.program2_sequence1_pattern1 = test.insert(buildProgramSequencePattern(fake.program1_sequence1, fake.program702_voice1, 4, "BOOMS"));
    fake.program2_voice1_track0 = test.insert(buildProgramVoiceTrack(fake.program702_voice1, "KICK"));
    fake.program2_voice1_track1 = test.insert(buildProgramVoiceTrack(fake.program702_voice1, "SNARE"));
    fake.program2_sequence1_pattern1_event0 = test.insert(buildProgramSequencePatternEvent(fake.program2_sequence1_pattern1, fake.program2_voice1_track1, 0, 1, "C", 1));
    fake.program2_sequence1_pattern1_event1 = test.insert(buildProgramSequencePatternEvent(fake.program2_sequence1_pattern1, fake.program2_voice1_track1, 1, 1, "G", 1));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor"));
    test.insert(buildProgramSequenceBinding(fake.program3_sequence1, 0));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test entity
    subject = new ProgramVoiceManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @AfterEach
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var entity = new ProgramVoice();
    entity.setId(UUID.randomUUID());
    entity.setProgramId(fake.program3.getId());
    entity.setType(InstrumentType.Pad);
    entity.setName("Jams");

    var result = subject.create(access, entity);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Jams", result.getName());
  }

  @Test
  public void add() throws Exception {
    DSLContext db = sqlStoreProvider.getDSL();

    var result = subject.add(db, fake.program3.getId(), InstrumentType.Bass);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Bass", result.getName());
    var confirmed = subject.readOne(HubAccess.internal(), result.getId());
    assertEquals(confirmed.getId(), result.getId());
  }

  /**
   * Artist expects to of a Main-type programVoice without crashing the entire platform https://www.pivotaltracker.com/story/show/156144567
   * NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var inputData = new ProgramVoice();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setType(InstrumentType.Pad);
    inputData.setName("Jams");

    var result = subject.create(
      access, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Jams", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");

    var result = subject.readOne(access, fake.program702_voice1.getId());

    assertNotNull(result);
    assertEquals(fake.program702_voice1.getId(), result.getId());
    assertEquals(fake.program2.getId(), result.getProgramId());
    assertEquals("Drums", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User, Artist");

    var e = assertThrows(ManagerException.class, () -> subject.readOne(access, fake.program702_voice1.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");

    Collection<ProgramVoice> result = subject.readMany(access, ImmutableList.of(fake.program2.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramVoice> resultIt = result.iterator();
    assertEquals("Drums", resultIt.next().getName());
  }

  @Test
  public void readMany_seesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramVoice> result = subject.readMany(access, ImmutableList.of(fake.program2.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_okWithChildEntities() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    subject.destroy(access, fake.program702_voice1.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0), selectCount.from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(fake.program702_voice1.getId()))
        .fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_okWithMainChordVoicings() throws Exception {
    HubAccess access = HubAccess.internal();
    var program5 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "Main Jam", "C minor", 140, 0.6f));
    var program5_voiceBass = test.insert(buildVoice(program5, InstrumentType.Bass, "Bass"));
    var program5_sequence0 = test.insert(buildSequence(program5, 16, "Intro", 0.5f, "G major"));
    var program5_sequence0_chord0 = test.insert(buildChord(program5_sequence0, 0.0, "G major"));
    test.insert(buildVoicing(program5_sequence0_chord0, program5_voiceBass, "G3, B3, D4"));
    var program5_sequence0_chord1 = test.insert(buildChord(program5_sequence0, 8.0, "Ab minor"));
    test.insert(buildVoicing(program5_sequence0_chord1, program5_voiceBass, "Ab3, Db3, F4"));
    var program5_sequence0_chord2 = test.insert(buildChord(program5_sequence0, 75.0, "G-9")); // this ChordEntity should be ignored, because it's past the end of the main-pattern total https://www.pivotaltracker.com/story/show/154090557
    test.insert(buildVoicing(program5_sequence0_chord2, program5_voiceBass, "G3, Bb3, D4, A4"));

    subject.destroy(access, program5_voiceBass.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0), selectCount.from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(program5_voiceBass.getId()))
        .fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Artist");
    new ProgramSequencePatternManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    new ProgramVoiceTrackManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    new ProgramVoiceTrackManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    subject.destroy(access, fake.program702_voice1.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0), selectCount.from(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.ID.eq(fake.program702_voice1.getId()))
        .fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account2), "Artist");
    new ProgramSequencePatternManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    new ProgramVoiceTrackManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    new ProgramVoiceTrackManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    var e = assertThrows(ManagerException.class, () -> subject.destroy(access, fake.program702_voice1.getId()));
    assertEquals("Voice in Program in Account you have access to does not exist", e.getMessage());
  }

}

