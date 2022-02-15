// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.*;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.assertj.core.util.Lists;
import org.jooq.exception.DataAccessException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.tables.InstrumentAudio.INSTRUMENT_AUDIO;
import static io.xj.hub.tables.InstrumentMeme.INSTRUMENT_MEME;
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

public class LibraryManagerImplTest {
  private LibraryManager subject;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "palm tree" has library "leaves" and library "coconuts"
    fake.account1 = test.insert(buildAccount("palm tree"));
    fake.library1a = test.insert(buildLibrary(fake.account1, "leaves"));
    fake.library1b = test.insert(buildLibrary(fake.account1, "coconuts"));

    // Account "boat" has library "helm" and library "sail"
    fake.account2 = test.insert(buildAccount("boat"));
    fake.library2a = test.insert(buildLibrary(fake.account2, "helm"));
    fake.library2b = test.insert(buildLibrary(fake.account2, "sail"));

    // Instantiate the test subject
    subject = injector.getInstance(LibraryManager.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Library result = subject.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void create_asEngineer() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Library inputData = new Library();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Library result = subject.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void create_asEngineer_failsWithoutAccountAccess() {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "Engineer");
    Library inputData = new Library();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    var e = assertThrows(ManagerException.class, () -> subject.create(access, inputData));

    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void create_FailsWithoutAccountID() {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setName("coconuts");

    var e = assertThrows(ManagerException.class, () -> subject.create(access, inputData));

    assertEquals("Account ID is required.", e.getMessage());
  }

  @Test
  public void clone_includesProgramsAndLibraries() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    var library = new Library();
    library.setName("no longer coconuts");
    library.setAccountId(fake.account1.getId());
    //
    fake.program1 = test.insert(buildProgram(fake.library1a, ProgramType.Beat, "cannons fifty nine"));
    test.insert(buildProgramMeme(fake.program1, "cinnamon"));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));
    var sequenceBinding1a_0 = test.insert(buildProgramSequenceBinding(fake.program1_sequence1, 0));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "chunk"));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    var voice = test.insert(buildProgramVoice(fake.program1, InstrumentType.Drum, "drums"));
    var track = test.insert(buildProgramVoiceTrack(voice, "Kick"));
    var programSequenceChord = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "D"));
    test.insert(buildProgramSequenceChordVoicing(programSequenceChord, InstrumentType.Sticky, "D2,F#2,A2"));
    var pattern = test.insert(buildProgramSequencePattern(fake.program1_sequence1, voice, 8, "jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));
    //
    fake.instrument202 = test.insert(buildInstrument(fake.library1a, InstrumentType.Drum, InstrumentMode.Events, InstrumentState.Published, "cannons fifty nine"));
    test.insert(buildInstrumentMeme(fake.instrument202, "chunk"));
    fake.audio1 = test.insert(buildInstrumentAudio(fake.instrument202, "Test audio", "fake.audio5.wav", 0.0f, 2.0f, 120.0f));

    ManagerCloner<Library> resultCloner = subject.clone(access, fake.library1a.getId(), library);

    assertNotNull(resultCloner);
    assertEquals(fake.account1.getId(), resultCloner.getClone().getAccountId());
    assertEquals("no longer coconuts", resultCloner.getClone().getName());
    //
    Program resultProgram = (Program) resultCloner.getChildClones().stream()
      .filter(e -> Program.class.equals(e.getClass()))
      .findFirst()
      .orElseThrow();
    assertNotNull(resultProgram);
    assertEquals(0.6, resultProgram.getDensity(), 0.1);
    assertEquals("C", resultProgram.getKey());
    assertEquals(resultCloner.getClone().getId(), resultProgram.getLibraryId());
    assertEquals("cannons fifty nine", resultProgram.getName());
    assertEquals(120, resultProgram.getTempo(), 0.1);
    assertEquals(ProgramType.Beat, resultProgram.getType());
    // Cloned ProgramMeme
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_MEME)
      .where(PROGRAM_MEME.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoice.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramVoiceTrack belongs to ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoiceTrack.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequence.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChord belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChord.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChordVoicing belongs to ProgramSequenceChord
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChordVoicing.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBinding belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBinding.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
    assertEquals(2, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBindingMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(2), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING_MEME)
      .where(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePattern.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePatternEvent.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.eq(resultProgram.getId()))
      .fetchOne(0, int.class));
    //
    Instrument resultInstrument = (Instrument) resultCloner.getChildClones().stream()
      .filter(e -> Instrument.class.equals(e.getClass()))
      .findFirst()
      .orElseThrow();
    assertEquals(resultCloner.getClone().getId(), resultInstrument.getLibraryId());
    assertEquals("cannons fifty nine", resultInstrument.getName());
    assertEquals(InstrumentType.Drum, resultInstrument.getType());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_MEME)
      .where(INSTRUMENT_MEME.INSTRUMENT_ID.eq(resultInstrument.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(resultInstrument.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Library result = subject.readOne(access, fake.library1b.getId());

    assertNotNull(result);
    assertEquals(fake.library1b.getId(), result.getId());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

    var e = assertThrows(ManagerException.class, () -> subject.readOne(access, fake.account1.getId()));

    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<Library> result = subject.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
    Iterator<Library> resultIt = result.iterator();
    assertEquals("leaves", resultIt.next().getName());
    assertEquals("coconuts", resultIt.next().getName());
  }

  @Test
  public void readMany_fromAllAccounts() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1, fake.account2), "User");

    Collection<Library> result = subject.readMany(access, Lists.newArrayList());

    assertEquals(4L, result.size());
    Iterator<Library> it = result.iterator();
    assertEquals("leaves", it.next().getName());
    assertEquals("coconuts", it.next().getName());
    assertEquals("helm", it.next().getName());
    assertEquals("sail", it.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

    Collection<Library> result = subject.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setName("cannons");

    var e = assertThrows(ManagerException.class, () -> subject.update(access, fake.library1a.getId(), inputData));

    assertEquals("Account ID is required.", e.getMessage());
  }

  @Test
  public void update_FailsWithoutName() {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setAccountId(fake.account1.getId());

    var e = assertThrows(ManagerException.class, () -> subject.update(access, fake.library1a.getId(), inputData));

    assertEquals("Name is required.", e.getMessage());
  }

  @Test
  public void update() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());

    subject.update(access, fake.library1a.getId(), inputData);

    Library result = subject.readOne(HubAccess.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void update_asEngineer() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Library inputData = new Library();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());

    subject.update(access, fake.library1a.getId(), inputData);

    Library result = subject.readOne(HubAccess.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Library.
   */
  @Test
  public void update_asEngineer_failsWithoutAccountAccess() {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "Engineer");
    Library inputData = new Library();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());

    var e = assertThrows(ManagerException.class, () -> subject.update(access, fake.library1a.getId(), inputData));

    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setName("cannons");
    inputData.setAccountId(UUID.randomUUID());

    assertThrows(DataAccessException.class, () -> subject.update(access, fake.library1a.getId(), inputData));

    Library result = subject.readOne(HubAccess.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("leaves", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account2.getId());

    subject.update(access, fake.library2a.getId(), inputData);

    Library result = subject.readOne(HubAccess.internal(), fake.library2a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account2.getId(), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Library inputData = new Library();
    inputData.setName("trunk");
    inputData.setAccountId(fake.account1.getId());

    subject.update(access, fake.library1a.getId(), inputData);

    Library result = subject.readOne(HubAccess.internal(), fake.library1a.getId());
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    subject.destroy(access, fake.library1a.getId());

    var e = assertThrows(ManagerException.class, () -> subject.readOne(HubAccess.internal(), fake.library1a.getId()));
    assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
  }

  @Test
  public void delete_artistCanDelete() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User,Artist");

    subject.destroy(access, fake.library1a.getId());

    var e = assertThrows(ManagerException.class, () -> subject.readOne(HubAccess.internal(), fake.library1a.getId()));
    assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
  }

  @Test
  public void delete_noProblemIfLibraryHasProgram() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    fake.user101 = test.insert(buildUser("bill", "bill@email.com", "https://pictures.com/bill.gif", "User"));
    test.insert(buildProgram(fake.library2b, ProgramType.Main, ProgramState.Published, "brilliant", "C#", 120.0f, 0.6f));

    subject.destroy(access, fake.library2b.getId());

    var e = assertThrows(ManagerException.class, () -> subject.readOne(HubAccess.internal(), fake.library2b.getId()));
    assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
  }

  @Test
  public void delete_noProblemIfLibraryHasInstrument() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    fake.user101 = test.insert(buildUser("bill", "bill@email.com", "https://pictures.com/bill.gif", "Admin"));
    test.insert(buildInstrument(fake.library2b, InstrumentType.Drum, InstrumentMode.Events, InstrumentState.Published, "brilliant"));

    subject.destroy(access, fake.library2b.getId());

    var e = assertThrows(ManagerException.class, () -> subject.readOne(HubAccess.internal(), fake.library2b.getId()));
    assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
  }
}
