// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
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
import io.xj.api.UserRoleType;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
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

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.IntegrationTestingFixtures.buildUserRole;
import static io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE;
import static io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequenceDAO testDAO;

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

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("palm tree"));
    fake.program1 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("ANTS")
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
      .name("ANTS")
      .key("C#")
      .tempo(120.0)
      .density(0.6));
    fake.program2_voice1 = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("Drums"));

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
    testDAO = injector.getInstance(ProgramSequenceDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = new ProgramSequence()
      .id(UUID.randomUUID())
      .key("G minor 7")
      .programId(fake.program3.getId())
      .name("cannons")
      .tempo(129.4)
      .total(4)
      .density(0.6)
      ;

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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = new ProgramSequence()
      .id(UUID.randomUUID())
      .key("G minor 7")
      .programId(fake.program3.getId())
      .name("cannons")
      .tempo(129.4)
      .total(4)
      .density(0.6)
      ;

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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var inputData = new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .density(0.583)
      .tempo(120.0)
      .key("C#")
      .name("cannons fifty nine")
      ;
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
    test.insert(new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
      .position(0.0)
      .name("D"));
    var pattern = test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");

    Collection<ProgramSequence> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    var subject = new ProgramSequence()
      .id(UUID.randomUUID())
      .name("cannons")
      .programId(UUID.randomUUID())
      ;

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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = new ProgramSequence()
      .id(fake.program3_sequence1.getId())
      .density(1.0)
      .key("G minor 7")
      .programId(fake.program3.getId())
      .name("cannons")
      .total(4)
      .tempo(129.4)
      ;

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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Admin");
    var subject = new ProgramSequence()
      .id(fake.program3_sequence1.getId())
      .key("G minor 7")
      .density(1.0)
      .programId(fake.program3.getId())
      .total(4)
      .name("cannons")
      .tempo(129.4)
      ;

    testDAO.update(hubAccess, fake.program3_sequence1.getId(), subject);

    var result = testDAO.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.programSequence35 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .total(16)
      .name("Ants")
      .density(0.6)
      .key("C#")
      .tempo(120.0));

    testDAO.destroy(hubAccess, fake.programSequence35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.ID.eq(fake.programSequence35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = new Account()
      .id(UUID.randomUUID())
      ;
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
    var programSequence = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .total(16)
      .name("Ants")
      .density(0.6)
      .key("C#")
      .tempo(120.0));
    test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .programVoiceId(fake.program2_voice1.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .name("Jam"));

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
    var programVoice = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("Drums"));
    var track = test.insert(new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(programVoice.getProgramId())
      .programVoiceId(programVoice.getId())
      .name("KICK"));
    var programSequence = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .total(16)
      .name("Ants")
      .density(0.6)
      .key("C#")
      .tempo(120.0));
    var programSequenceBinding = test.insert(new ProgramSequenceBinding()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .offset(0));
    test.insert(new ProgramSequenceBindingMeme()
      .id(UUID.randomUUID())
      .programId(programSequenceBinding.getProgramId())
      .programSequenceBindingId(programSequenceBinding.getId())
      .name("chunk"));
    var pattern = test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .programVoiceId(fake.program2_voice1.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .name("Jam"));
    test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(pattern.getProgramId())
      .programSequencePatternId(pattern.getId())
      .programVoiceTrackId(track.getId())
      .position(0.0)
      .duration(1.0)
      .note("C")
      .velocity(1.0));
    var programSequenceChord = test.insert(new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .position(0.0)
      .name("G"));
    test.insert(new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .type(InstrumentType.BASS)
      .programId(programSequenceChord.getProgramId())
      .programSequenceChordId(programSequenceChord.getId())
      .notes("C5, Eb5, G5"));

    testDAO.destroy(hubAccess, programSequence.getId());
  }

}

