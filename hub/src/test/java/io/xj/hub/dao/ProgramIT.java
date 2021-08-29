// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramMeme;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
import io.xj.api.ProgramSequenceBindingMeme;
import io.xj.api.ProgramSequenceChord;
import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.ProgramVoiceTrack;
import io.xj.api.User;
import io.xj.api.UserRole;
import io.xj.api.UserRoleType;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUserRole;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramIT {
  private ProgramDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(buildUserRole(fake.user2,UserRoleType.ADMIN));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(buildUserRole(fake.user3,UserRoleType.USER));
    test.insert(buildAccountUser(fake.account1,fake.user3));

    // Library "palm tree" has program "fonds" and program "nuts"
    fake.library1 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("palm tree"));
    fake.program1 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("fonds")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    fake.program1_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .total(4)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0));
    var sequenceBinding1a_0 = test.insert(new ProgramSequenceBinding()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
      .offset(0));
    test.insert(new ProgramSequenceBindingMeme()
      .id(UUID.randomUUID())
      .programId(sequenceBinding1a_0.getProgramId())
      .programSequenceBindingId(sequenceBinding1a_0.getId())
      .name("chunk"));
    test.insert(new ProgramSequenceBindingMeme()
      .id(UUID.randomUUID())
      .programId(sequenceBinding1a_0.getProgramId())
      .programSequenceBindingId(sequenceBinding1a_0.getId())
      .name("smooth"));
    fake.program2 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.RHYTHM)
      .state(ProgramState.PUBLISHED)
      .name("nuts")
      .key("C#")
      .tempo(120.0)
      .density(0.6));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("boat"));
    fake.program3 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.MACRO)
      .state(ProgramState.PUBLISHED)
      .name("helm")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    fake.program3_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .total(16)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0));
    test.insert(new ProgramSequenceBinding()
      .id(UUID.randomUUID())
      .programId(fake.program3_sequence1.getProgramId())
      .programSequenceId(fake.program3_sequence1.getId())
      .offset(0));
    fake.program4 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.DETAIL)
      .state(ProgramState.PUBLISHED)
      .name("sail")
      .key("C#")
      .tempo(120.0)
      .density(0.6));

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
    Program subject = new Program()
      .key("G minor 7")
      .libraryId(fake.library2.getId())
      .name("cannons")
      .tempo(129.4)
      .density(0.6)
      .state(ProgramState.PUBLISHED)
      .type(ProgramType.MAIN);

    Program result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.MAIN, result.getType());
  }

  /**
   [#156144567] Artist expects to of a Main-type program without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    Program inputData = new Program()
      .id(UUID.randomUUID())
      .key("G minor 7")
      .libraryId(fake.library2.getId())
      .name("cannons")
      .tempo(129.4)
      .density(0.6)
      .state(ProgramState.PUBLISHED)
      .type(ProgramType.MAIN);

    Program result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.MAIN, result.getType());
  }

  /**
   [#170290553] Clone sub-entities of program
   <p>
   [#175808105] Cloned Program should have same Voices and Chord Voicings
   <p>
   [#175947247] Artist expects to be able to clone Program without error
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program inputData = new Program()
      .libraryId(fake.library2.getId())
      .name("cannons fifty nine");
    test.insert(new ProgramMeme()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .name("cinnamon"));
    var voice = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("drums"));
    var track = test.insert(new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(voice.getProgramId())
      .programVoiceId(voice.getId())
      .name("Kick"));
    var programSequenceChord = test.insert(new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
      .position(0.0)
      .name("D"));
    test.insert(new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .type(InstrumentType.STICKY)
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceChordId(programSequenceChord.getId())
      .notes("D2,F#2,A2"));
    var pattern = test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programVoiceId(voice.getId())
      .programSequenceId(fake.program1_sequence1.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(8)
      .name("jam"));
    test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(pattern.getProgramId())
      .programSequencePatternId(pattern.getId())
      .programVoiceTrackId(track.getId())
      .position(0.0)
      .duration(1.0)
      .note("C")
      .velocity(1.0));

    DAOCloner<Program> resultCloner = testDAO.clone(hubAccess, fake.program1.getId(), inputData);

    Program result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.6, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    assertEquals(ProgramType.MAIN, result.getType());
    // Cloned ProgramMeme
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME)
      .where(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoice.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramVoice.PROGRAM_VOICE)
      .where(io.xj.hub.tables.ProgramVoice.PROGRAM_VOICE.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramVoiceTrack belongs to ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoiceTrack.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK)
      .where(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequence.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE)
      .where(io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChord belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChord.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD)
      .where(io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChordVoicing belongs to ProgramSequenceChord
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChordVoicing.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBinding belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBinding.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING)
      .where(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
    assertEquals(2, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBindingMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(2), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME)
      .where(io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePattern.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN)
      .where(io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePatternEvent.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.eq(result.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Program result = testDAO.readOne(hubAccess, fake.program2.getId());

    assertNotNull(result);
    assertEquals(ProgramType.RHYTHM, result.getType());
    assertEquals(ProgramState.PUBLISHED, result.getState());
    assertEquals(fake.program2.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");

    var e = assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, fake.program1.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  /**
   [#176372189] Fabricator should get distinct Chord Voicing Types
   */
  @Test
  public void readManyWithChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    new Program()
      .libraryId(fake.library2.getId())
      .name("cannons fifty nine");
    test.insert(new ProgramMeme()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .name("cinnamon"));
    var voice = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("drums"));
    var track = test.insert(new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(voice.getProgramId())
      .programVoiceId(voice.getId())
      .name("Kick"));
    var programSequenceChord = test.insert(new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
      .position(0.0)
      .name("D"));
    test.insert(new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .type(InstrumentType.STICKY)
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceChordId(programSequenceChord.getId())
      .notes("D2,F#2,A2"));
    var pattern = test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programVoiceId(voice.getId())
      .programSequenceId(fake.program1_sequence1.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(8)
      .name("jam"));
    test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(pattern.getProgramId())
      .programSequencePatternId(pattern.getId())
      .programVoiceTrackId(track.getId())
      .position(0.0)
      .duration(1.0)
      .note("C")
      .velocity(1.0));

    Collection<Object> results = testDAO.readManyWithChildEntities(hubAccess, ImmutableList.of(fake.program1.getId()));

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
   Assert that the results contain an exact count of classes in the results

   @param type    of class to search for
   @param total   count of instances to assert
   @param results to search within
   @param <N>     type of entity
   */
  private <N> void assertContains(Class<N> type, int total, Collection<Object> results) {
    assertEquals(String.format("Exactly %s count of %s class in results",
      total, type.getSimpleName()), total, results.stream()
      .filter(r -> type.isAssignableFrom(r.getClass()))
      .count());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");

    Collection<Program> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    Program subject = new Program()
      .id(UUID.randomUUID())
      .name("cannons")
      .libraryId(UUID.randomUUID());

    try {
      testDAO.update(hubAccess, fake.program1.getId(), subject);

    } catch (Exception e) {
      Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(DAOException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program subject = new Program()
      .id(fake.program1.getId())
      .density(1.0)
      .key("G minor 7")
      .libraryId(fake.library2.getId())
      .name("cannons")
      .tempo(129.4)
      .state(ProgramState.PUBLISHED)
      .type(ProgramType.MAIN);

    testDAO.update(hubAccess, fake.program1.getId(), subject);

    Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.library2.getId(), result.getLibraryId());
  }

  /**
   [#175789099] Artist should always be able to change program type
   <p>
   DEPRECATES [#170390872] prevent user from changing teh type of a Rhythm program, when it has any Tracks and/or Voices.
   */
  @Test
  public void update_artistCanAlwaysChangeType() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("Drums"));
    Program subject = new Program()
      .id(fake.program2.getId())
      .density(1.0)
      .key("G minor 7")
      .libraryId(fake.library1.getId())
      .name("cannons")
      .tempo(129.4)
      .state(ProgramState.PUBLISHED)
      .type(ProgramType.MAIN);

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
      .id(fake.program1.getId())
      .key("G minor 7")
      .density(1.0)
      .libraryId(fake.library2.getId())
      .name("cannons")
      .state(ProgramState.PUBLISHED)
      .tempo(129.4)
      .type(ProgramType.MAIN);

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
    fake.program35 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("fonds")
      .key("C#")
      .tempo(120.0)
      .density(0.6));

    testDAO.destroy(hubAccess, fake.program35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.Program.PROGRAM)
      .where(io.xj.hub.tables.Program.PROGRAM.ID.eq(fake.program35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = new Account()
      .id(UUID.randomUUID())
    ;
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    var e = assertThrows(DAOException.class, () -> testDAO.destroy(hubAccess, fake.program1.getId()));
    assertEquals("Program belonging to you does not exist", e.getMessage());
  }

  /**
   [#170299297] Cannot delete Programs that have a Meme
   */
  @Test
  public void destroy_failsIfHasMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Program program = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("fonds")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    test.insert(new ProgramMeme()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .name("frozen"));
    test.insert(new ProgramMeme()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .name("ham"));

    var e = assertThrows(DAOException.class, () -> testDAO.destroy(hubAccess, program.getId()));
    assertEquals("Found Program Memes", e.getMessage());
  }

  /**
   [#170299297] As long as program has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program program = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("fonds")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    var programSequence = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .total(4)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0));
    test.insert(new ProgramSequenceBinding()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .offset(0));
    var voice = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("drums"));
    var track = test.insert(new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(voice.getProgramId())
      .programVoiceId(voice.getId())
      .name("Kick"));
    test.insert(new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .position(0.0)
      .name("D"));
    var pattern = test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .programVoiceId(voice.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(8)
      .name("jam"));
    test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(pattern.getProgramId())
      .programSequencePatternId(pattern.getId())
      .programVoiceTrackId(track.getId())
      .position(0.0)
      .duration(1.0)
      .note("C")
      .velocity(1.0));

    testDAO.destroy(hubAccess, program.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.Program.PROGRAM)
      .where(io.xj.hub.tables.Program.PROGRAM.ID.eq(program.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<Program> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

}

