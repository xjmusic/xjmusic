// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.AccountUser;
import io.xj.service.hub.model.InstrumentType;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.ProgramSequenceChord;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.ProgramSequencePatternEvent;
import io.xj.service.hub.model.ProgramSequencePatternType;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.ProgramVoiceTrack;
import io.xj.service.hub.model.User;
import io.xj.service.hub.model.UserRole;
import io.xj.service.hub.model.UserRoleType;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.hub.testing.InternalResources;
import io.xj.service.hub.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.service.hub.tables.ProgramSequence.PROGRAM_SEQUENCE;
import static io.xj.service.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static io.xj.service.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.service.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD;
import static io.xj.service.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private WorkManager workManager;
  private ProgramSequenceDAO testDAO;

  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fixture;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new IntegrationTestModule()));
    workManager = injector.getInstance(WorkManager.class);
    injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubModule(), new IntegrationTestModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fixture = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fixture.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fixture.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(fixture.user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    fixture.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(fixture.user3, UserRoleType.User));
    test.insert(AccountUser.create(fixture.account1, fixture.user3));

    // Library "palm tree" has program "Ants" and program "Ants"
    fixture.library1 = test.insert(Library.create(fixture.account1, "palm tree", InternalResources.now()));
    fixture.program1 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Main, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fixture.programSequence1 = test.insert(ProgramSequence.create(fixture.program1, 4, "Ants", 0.583, "D minor", 120.0));
    ProgramSequenceBinding sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.create(fixture.programSequence1, 0));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "chunk"));
    test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    fixture.program2 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Rhythm, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fixture.programVoice3 = test.insert(ProgramVoice.create(fixture.program2, InstrumentType.Percussive, "Drums"));

    // Library "boat" has program "helm" and program "sail"
    fixture.library2 = test.insert(Library.create(fixture.account1, "boat", InternalResources.now()));
    fixture.program3 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fixture.programSequence3 = test.insert(ProgramSequence.create(fixture.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fixture.programSequence3, 0));
    fixture.program4 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequenceDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    ProgramSequence subject = ProgramSequence.create()
      .setKey("G minor 7")
      .setProgramId(fixture.program3.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(4)
      .setDensity(0.6);

    ProgramSequence result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequence without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "User,Artist");
    ProgramSequence inputData = ProgramSequence.create()
      .setKey("G minor 7")
      .setProgramId(fixture.program3.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(4)
      .setDensity(0.6);

    ProgramSequence result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
  }

  /**
   [#170290553] Clone sub-entities of programSequence
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    ProgramSequence inputData = ProgramSequence.create()
      .setProgramId(fixture.program3.getId())
      .setDensity(0.583)
      .setKey("C#")
      .setName("cannons fifty nine");
    test.insert(ProgramMeme.create(fixture.program1, "cinnamon"));
    ProgramVoice voice = test.insert(ProgramVoice.create(fixture.program1, InstrumentType.Percussive, "drums"));
    ProgramVoiceTrack track = test.insert(ProgramVoiceTrack.create(voice, "Kick"));
    test.insert(ProgramSequenceChord.create(fixture.programSequence1, 0, "D"));
    ProgramSequencePattern pattern = test.insert(ProgramSequencePattern.create(fixture.programSequence1, voice, ProgramSequencePatternType.Loop, 8, "jam"));
    test.insert(ProgramSequencePatternEvent.create(pattern, track, 0, 1, "C", 1));

    DAOCloner<ProgramSequence> resultCloner = testDAO.clone(access, fixture.programSequence1.getId(), inputData);

    ProgramSequence result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fixture.program3.getId(), result.getProgramId());
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
    Access access = Access.create(ImmutableList.of(fixture.account1), "User, Artist");

    ProgramSequence result = testDAO.readOne(access, fixture.programSequence3.getId());

    assertNotNull(result);
    assertEquals(fixture.programSequence3.getId(), result.getId());
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals("Ants", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");
    failure.expect(HubException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fixture.programSequence3.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Admin");

    Collection<ProgramSequence> result = testDAO.readMany(access, ImmutableList.of(fixture.program3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequence> resultIt = result.iterator();
    assertEquals("Ants", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramSequence> result = testDAO.readMany(access, ImmutableList.of(fixture.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User, Artist");
    ProgramSequence subject = ProgramSequence.create()
      .setName("cannons")
      .setProgramId(UUID.randomUUID());

    try {
      testDAO.update(access, fixture.programSequence3.getId(), subject);

    } catch (Exception e) {
      ProgramSequence result = testDAO.readOne(Access.internal(), fixture.programSequence3.getId());
      assertNotNull(result);
      assertEquals("Ants", result.getName());
      assertEquals(fixture.program3.getId(), result.getProgramId());
      assertSame(ValueException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    ProgramSequence subject = new ProgramSequence()
      .setId(fixture.programSequence3.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setProgramId(fixture.program3.getId())
      .setName("cannons")
      .setTotal(4)
      .setTempo(129.4);

    testDAO.update(access, fixture.programSequence3.getId(), subject);

    ProgramSequence result = testDAO.readOne(Access.internal(), fixture.programSequence3.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fixture.program3.getId(), result.getProgramId());
  }

  /**
   [#156030760] Artist expects owner of ProgramSequence or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a programSequence originally belonging to Jenny
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Admin");
    ProgramSequence subject = new ProgramSequence()
      .setId(fixture.programSequence3.getId())
      .setKey("G minor 7")
      .setDensity(1.0)
      .setProgramId(fixture.program3.getId())
      .setTotal(4)
      .setName("cannons")
      .setTempo(129.4);

    testDAO.update(access, fixture.programSequence3.getId(), subject);

    ProgramSequence result = testDAO.readOne(Access.internal(), fixture.programSequence3.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    Access access = Access.create("Admin");

    failure.expect(HubException.class);
    failure.expectMessage("Found binding of Sequence to Program");

    testDAO.destroy(access, fixture.programSequence3.getId());
  }

  @Test
  public void destroy_asArtist() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Artist");
    fixture.programSequence35 = test.insert(ProgramSequence.create(fixture.program2, 16, "Ants", 0.6, "C#", 120.0));

    testDAO.destroy(access, fixture.programSequence35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE)
      .where(PROGRAM_SEQUENCE.ID.eq(fixture.programSequence35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fixture.account2 = Account.create();
    Access access = Access.create(ImmutableList.of(fixture.account2), "Artist");

    failure.expect(HubException.class);
    failure.expectMessage("Sequence in Program in Account you have access to does not exist");

    testDAO.destroy(access, fixture.programSequence3.getId());
  }

  /**
   [#170390872] Delete a **Sequence** even if it has children, as long as it has no sequence bindings
   */
  @Test
  public void destroy_succeedsEvenWhenHasPattern() throws Exception {
    Access access = Access.create("Admin");
    ProgramSequence programSequence = test.insert(ProgramSequence.create(fixture.program2, 16, "Ants", 0.6, "C#", 120.0));
    test.insert(ProgramSequencePattern.create(programSequence, fixture.programVoice3, ProgramSequencePatternType.Loop, 4, "Jam"));

    testDAO.destroy(access, programSequence.getId());
  }

  /**
   [#170390872] Delete a **Sequence** even if it has children, as long as it has no sequence bindings
   */
  @Test
  public void destroy_failsIfHasBinding() throws Exception {
    Access access = Access.create("Admin");
    ProgramSequence programSequence = test.insert(ProgramSequence.create(fixture.program2, 16, "Ants", 0.6, "C#", 120.0));
    test.insert(ProgramSequenceBinding.create(programSequence, 0));

    failure.expect(HubException.class);
    failure.expectMessage("Found binding of Sequence to Program");

    testDAO.destroy(access, programSequence.getId());
  }

}

