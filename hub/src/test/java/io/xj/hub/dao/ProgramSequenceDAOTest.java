// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

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
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE;
import static io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceDAOTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequenceDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
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
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor", 120.0f));
    var sequenceBinding1a_0 = test.insert(buildProgramSequenceBinding(fake.program1_sequence1, 0));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "chunk"));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Rhythm, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor", 120.0f));
    test.insert(buildProgramSequenceBinding(fake.program3_sequence1, 0));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequenceDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramSequence();
    subject.setId(UUID.randomUUID());
    subject.setKey("G minor 7");
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");
    subject.setTempo(129.4f);
    subject.setTotal((short) 4);
    subject.setDensity(0.6f);

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequence without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var inputData = new ProgramSequence();
    inputData.setId(UUID.randomUUID());
    inputData.setKey("G minor 7");
    inputData.setProgramId(fake.program3.getId());
    inputData.setName("cannons");
    inputData.setTempo(129.4f);
    inputData.setTotal((short) 4f);
    inputData.setDensity(0.6f);

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
  }

  /**
   [#170290553] Clone sub-entities of programSequence
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var inputData = new ProgramSequence();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setDensity(0.583f);
    inputData.setTempo(120.0f);
    inputData.setKey("C#");
    inputData.setName("cannons fifty nine");
    test.insert(buildProgramMeme(fake.program1, "cinnamon"));
    var voice = test.insert(buildProgramVoice(fake.program1, InstrumentType.Drum, "drums"));
    var track = test.insert(buildProgramVoiceTrack(voice, "Kick"));
    test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "D"));
    var pattern = test.insert(buildProgramSequencePattern(fake.program1_sequence1, voice, 8, "jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));

    DAOCloner<ProgramSequence> resultCloner = testDAO.clone(hubAccess, fake.program1_sequence1.getId(), inputData);

    var result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    // Cloned ProgramSequence
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChord belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChord.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBinding belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBinding.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
    assertEquals(2, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBindingMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(2), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING_MEME)
      .join(PROGRAM_SEQUENCE_BINDING).on(PROGRAM_SEQUENCE_BINDING.ID.eq(PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_SEQUENCE_BINDING_ID))
      .where(PROGRAM_SEQUENCE_BINDING.PROGRAM_SEQUENCE_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePattern.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_PATTERN)
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePatternEvent.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_PATTERN_EVENT)
      .join(PROGRAM_SEQUENCE_PATTERN).on(PROGRAM_SEQUENCE_PATTERN.ID.eq(PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_SEQUENCE_PATTERN_ID))
      .where(PROGRAM_SEQUENCE_PATTERN.PROGRAM_SEQUENCE_ID.eq(result.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, fake.program3_sequence1.getId());

    assertNotNull(result);
    assertEquals(fake.program3_sequence1.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Ants", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.program3_sequence1.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequence> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequence> resultIt = result.iterator();
    assertEquals("Ants", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramSequence> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    var subject = new ProgramSequence();
    subject.setId(UUID.randomUUID());
    subject.setName("cannons");
    subject.setProgramId(UUID.randomUUID());

    try {
      testDAO.update(hubAccess, fake.program3_sequence1.getId(), subject);

    } catch (Exception e) {
      var result = testDAO.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
      assertNotNull(result);
      assertEquals("Ants", result.getName());
      assertEquals(fake.program3.getId(), result.getProgramId());
      assertSame(DAOException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramSequence();
    subject.setId(fake.program3_sequence1.getId());
    subject.setDensity(1.0f);
    subject.setKey("G minor 7");
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");
    subject.setTotal((short) 4);
    subject.setTempo(129.4f);

    testDAO.update(hubAccess, fake.program3_sequence1.getId(), subject);

    var result = testDAO.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  /**
   [#156030760] Artist expects owner of ProgramSequence or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a programSequence originally belonging to Jenny
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramSequence();
    subject.setId(fake.program3_sequence1.getId());
    subject.setKey("G minor 7");
    subject.setDensity(1.0f);
    subject.setProgramId(fake.program3.getId());
    subject.setTotal((short) 4);
    subject.setName("cannons");
    subject.setTempo(129.4f);

    testDAO.update(hubAccess, fake.program3_sequence1.getId(), subject);

    var result = testDAO.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.programSequence35 = test.insert(buildProgramSequence(fake.program2, 16, "Ants", 0.6f, "C#", 120.0f));

    testDAO.destroy(hubAccess, fake.programSequence35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.ID.eq(fake.programSequence35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(DAOException.class);
    failure.expectMessage("Sequence in Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, fake.program3_sequence1.getId());
  }

  /**
   [#170390872] Delete a **Sequence** even if it has children, as long as it has no sequence bindings
   */
  @Test
  public void destroy_succeedsEvenWhenHasPattern() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var programSequence = test.insert(buildProgramSequence(fake.program2, 16, "Ants", 0.6f, "C#", 120.0f));
    test.insert(buildProgramSequencePattern(programSequence, fake.program702_voice1, 4, "Jam"));

    testDAO.destroy(hubAccess, programSequence.getId());
  }

  /**
   [#175693261] Artist editing program should be able to delete sequences
   <p>
   DEPRECATES [#170390872] Delete a **Sequence** even if it has children, as long as it has no sequence bindings
   */
  @Test
  public void destroy_succeedsEvenWithChildren() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    var programVoice = test.insert(buildProgramVoice(fake.program3, InstrumentType.Drum, "Drums"));
    var track = test.insert(buildProgramVoiceTrack(programVoice, "KICK"));
    var programSequence = test.insert(buildProgramSequence(fake.program2, 16, "Ants", 0.6f, "C#", 120.0f));
    var programSequenceBinding = test.insert(buildProgramSequenceBinding(programSequence, 0));
    test.insert(buildProgramSequenceBindingMeme(programSequenceBinding, "chunk"));
    var pattern = test.insert(buildProgramSequencePattern(programSequence, fake.program702_voice1, 4, "Jam"));
    test.insert(buildProgramSequencePatternEvent(pattern, track, 0.0f, 1.0f, "C", 1.0f));
    var programSequenceChord = test.insert(buildProgramSequenceChord(programSequence, 0.0, "G"));
    test.insert(buildProgramSequenceChordVoicing(programSequenceChord, InstrumentType.Bass, "C5, Eb5, G5"));

    testDAO.destroy(hubAccess, programSequence.getId());
  }

}

