// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.service.hub.HubException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.AccountUser;
import io.xj.service.hub.model.InstrumentType;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
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

import static io.xj.service.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramVoiceTrackIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private WorkManager workManager;
  private ProgramVoiceTrackDAO testDAO;

  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fixture;

  private ProgramVoiceTrack voiceTrack1a_0;
  private ProgramSequencePatternEvent voiceTrack1a_0_event0;
  private ProgramSequencePatternEvent voiceTrack1a_0_event1;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new IntegrationTestModule()));
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
    fixture.program2 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Rhythm, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fixture.programVoice3 = test.insert(ProgramVoice.create(fixture.program2, InstrumentType.Percussive, "Drums"));
    fixture.programSequencePattern1 = test.insert(ProgramSequencePattern.create(fixture.programSequence1, fixture.programVoice3, ProgramSequencePatternType.Loop, 4, "BOOMS"));
    voiceTrack1a_0 = test.insert(ProgramVoiceTrack.create(fixture.programVoice3, "JAMS"));
    voiceTrack1a_0_event0 = test.insert(ProgramSequencePatternEvent.create(fixture.programSequencePattern1, voiceTrack1a_0, 0, 1, "C", 1));
    voiceTrack1a_0_event1 = test.insert(ProgramSequencePatternEvent.create(fixture.programSequencePattern1, voiceTrack1a_0, 1, 1, "C", 1));

    // Library "boat" has program "helm" and program "sail"
    fixture.library2 = test.insert(Library.create(fixture.account1, "boat", InternalResources.now()));
    fixture.program3 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fixture.programSequence3 = test.insert(ProgramSequence.create(fixture.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fixture.programSequence3, 0));
    fixture.program4 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramVoiceTrackDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    ProgramVoiceTrack subject = ProgramVoiceTrack.create()
      .setProgramId(fixture.program3.getId())
      .setProgramVoiceId(fixture.programVoice3.getId())
      .setName("Jams");

    ProgramVoiceTrack result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals(fixture.programVoice3.getId(), result.getProgramVoiceId());
    assertEquals("JAMS", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programVoiceTrack without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "User,Artist");
    ProgramVoiceTrack inputData = ProgramVoiceTrack.create()
      .setProgramId(fixture.program3.getId())
      .setProgramVoiceId(fixture.programVoice3.getId())
      .setName("Jams");

    ProgramVoiceTrack result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals(fixture.programVoice3.getId(), result.getProgramVoiceId());
    assertEquals("JAMS", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User, Artist");

    ProgramVoiceTrack result = testDAO.readOne(access, voiceTrack1a_0.getId());

    assertNotNull(result);
    assertEquals(voiceTrack1a_0.getId(), result.getId());
    assertEquals(fixture.program2.getId(), result.getProgramId());
    assertEquals("JAMS", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");
    failure.expect(HubException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, voiceTrack1a_0.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User, Artist");

    Collection<ProgramVoiceTrack> result = testDAO.readMany(access, ImmutableList.of(fixture.programVoice3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramVoiceTrack> resultIt = result.iterator();
    assertEquals("JAMS", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramVoiceTrack> result = testDAO.readMany(access, ImmutableList.of(fixture.programVoice3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    Access access = Access.create("Admin");

    failure.expect(HubException.class);
    failure.expectMessage("Found Events in Track");

    testDAO.destroy(access, voiceTrack1a_0.getId());
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    Access access = Access.create("Admin");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(Access.internal(), voiceTrack1a_0_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(Access.internal(), voiceTrack1a_0_event1.getId());

    testDAO.destroy(access, voiceTrack1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.ID.eq(voiceTrack1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_asArtist() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(Access.internal(), voiceTrack1a_0_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(Access.internal(), voiceTrack1a_0_event1.getId());

    testDAO.destroy(access, voiceTrack1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_VOICE_TRACK)
      .where(PROGRAM_VOICE_TRACK.ID.eq(voiceTrack1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fixture.account2 = Account.create();
    Access access = Access.create(ImmutableList.of(fixture.account2), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(Access.internal(), voiceTrack1a_0_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(Access.internal(), voiceTrack1a_0_event1.getId());

    failure.expect(HubException.class);
    failure.expectMessage("Track in Voice in Program you have access to does not exist");

    testDAO.destroy(access, voiceTrack1a_0.getId());
  }

}

