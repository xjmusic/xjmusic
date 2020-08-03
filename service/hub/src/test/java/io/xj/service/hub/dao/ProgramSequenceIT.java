// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.pubsub.FileStoreModule;
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

import static io.xj.service.hub.tables.ProgramSequence.PROGRAM_SEQUENCE;
import static io.xj.service.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static io.xj.service.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.service.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD;
import static io.xj.service.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static org.junit.Assert.*;

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

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(Library.create(fake.account1, "palm tree", Instant.now()));
    fake.program1 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fake.program1_sequence1 = test.insert(ProgramSequence.create(fake.program1, 4, "Ants", 0.583, "D minor", 120.0));
    ProgramSequenceBinding sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.create(fake.program1_sequence1, 0));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "chunk"));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Rhythm, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fake.program2_voice1 = test.insert(ProgramVoice.create(fake.program2, InstrumentType.Percussive, "Drums"));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.create(fake.account1, "boat", Instant.now()));
    fake.program3 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fake.program3_sequence1 = test.insert(ProgramSequence.create(fake.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fake.program3_sequence1, 0));
    fake.program4 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

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
    ProgramSequence subject = ProgramSequence.create()
      .setKey("G minor 7")
      .setProgramId(fake.program3.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(4)
      .setDensity(0.6);

    ProgramSequence result = testDAO.create(hubAccess, subject);

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
    ProgramSequence inputData = ProgramSequence.create()
      .setKey("G minor 7")
      .setProgramId(fake.program3.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(4)
      .setDensity(0.6);

    ProgramSequence result = testDAO.create(hubAccess, inputData);

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
    ProgramSequence inputData = ProgramSequence.create()
      .setProgramId(fake.program3.getId())
      .setDensity(0.583)
      .setKey("C#")
      .setName("cannons fifty nine");
    test.insert(ProgramMeme.create(fake.program1, "cinnamon"));
    ProgramVoice voice = test.insert(ProgramVoice.create(fake.program1, InstrumentType.Percussive, "drums"));
    ProgramVoiceTrack track = test.insert(ProgramVoiceTrack.create(voice, "Kick"));
    test.insert(ProgramSequenceChord.create(fake.program1_sequence1, 0, "D"));
    ProgramSequencePattern pattern = test.insert(ProgramSequencePattern.create(fake.program1_sequence1, voice, ProgramSequencePatternType.Loop, 8, "jam"));
    test.insert(ProgramSequencePatternEvent.create(pattern, track, 0, 1, "C", 1));

    DAOCloner<ProgramSequence> resultCloner = testDAO.clone(hubAccess, fake.program1_sequence1.getId(), inputData);

    ProgramSequence result = resultCloner.getClone();
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

    ProgramSequence result = testDAO.readOne(hubAccess, fake.program3_sequence1.getId());

    assertNotNull(result);
    assertEquals(fake.program3_sequence1.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Ants", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramSequence> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    ProgramSequence subject = ProgramSequence.create()
      .setName("cannons")
      .setProgramId(UUID.randomUUID());

    try {
      testDAO.update(hubAccess, fake.program3_sequence1.getId(), subject);

    } catch (Exception e) {
      ProgramSequence result = testDAO.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
      assertNotNull(result);
      assertEquals("Ants", result.getName());
      assertEquals(fake.program3.getId(), result.getProgramId());
      assertSame(ValueException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    ProgramSequence subject = new ProgramSequence()
      .setId(fake.program3_sequence1.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setProgramId(fake.program3.getId())
      .setName("cannons")
      .setTotal(4)
      .setTempo(129.4);

    testDAO.update(hubAccess, fake.program3_sequence1.getId(), subject);

    ProgramSequence result = testDAO.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
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
    ProgramSequence subject = new ProgramSequence()
      .setId(fake.program3_sequence1.getId())
      .setKey("G minor 7")
      .setDensity(1.0)
      .setProgramId(fake.program3.getId())
      .setTotal(4)
      .setName("cannons")
      .setTempo(129.4);

    testDAO.update(hubAccess, fake.program3_sequence1.getId(), subject);

    ProgramSequence result = testDAO.readOne(HubAccess.internal(), fake.program3_sequence1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    failure.expect(DAOException.class);
    failure.expectMessage("Found binding of Sequence to Program");

    testDAO.destroy(hubAccess, fake.program3_sequence1.getId());
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.programSequence35 = test.insert(ProgramSequence.create(fake.program2, 16, "Ants", 0.6, "C#", 120.0));

    testDAO.destroy(hubAccess, fake.programSequence35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.ID.eq(fake.programSequence35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.create();
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
    ProgramSequence programSequence = test.insert(ProgramSequence.create(fake.program2, 16, "Ants", 0.6, "C#", 120.0));
    test.insert(ProgramSequencePattern.create(programSequence, fake.program2_voice1, ProgramSequencePatternType.Loop, 4, "Jam"));

    testDAO.destroy(hubAccess, programSequence.getId());
  }

  /**
   [#170390872] Delete a **Sequence** even if it has children, as long as it has no sequence bindings
   */
  @Test
  public void destroy_failsIfHasBinding() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    ProgramSequence programSequence = test.insert(ProgramSequence.create(fake.program2, 16, "Ants", 0.6, "C#", 120.0));
    test.insert(ProgramSequenceBinding.create(programSequence, 0));

    failure.expect(DAOException.class);
    failure.expectMessage("Found binding of Sequence to Program");

    testDAO.destroy(hubAccess, programSequence.getId());
  }

}

