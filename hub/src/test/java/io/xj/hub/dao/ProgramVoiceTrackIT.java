// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramVoiceTrackIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramVoiceTrackDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private ProgramVoiceTrack voiceTrack1a_0;
  private ProgramSequencePatternEvent voiceTrack1a_0_event0;
  private ProgramSequencePatternEvent voiceTrack1a_0_event1;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
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
    fake.account1 = test.insert(new Account()
      .id(UUID.randomUUID())
      .name("bananas")
    );
// John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("john")
      .email("john@email.com")
      .avatarUrl("http://pictures.com/john.gif")
    );
    test.insert(new UserRole()
      .id(UUID.randomUUID())
      .userId(fake.user2.getId())
      .type(UserRoleType.ADMIN));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(new User()
      .id(UUID.randomUUID())
      .name("jenny")
      .email("jenny@email.com")
      .avatarUrl("http://pictures.com/jenny.gif")
    );
    test.insert(new UserRole()
      .id(UUID.randomUUID())
      .userId(fake.user2.getId())
      .type(UserRoleType.USER)
    );
    test.insert(new AccountUser()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .userId(fake.user3.getId())
    );

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("palm tree")
    );
    fake.program1 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.MAIN)
      .state(ProgramState.PUBLISHED)
      .name("ANTS")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
    );
    fake.program1_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program1.getId())
      .total(4)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0)
    );
    fake.program2 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library1.getId())
      .type(ProgramType.RHYTHM)
      .state(ProgramState.PUBLISHED)
      .name("ANTS")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
    );
    fake.program2_voice1 = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("Drums")
    );
    fake.program2_sequence1_pattern1 = test.insert(new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(fake.program1_sequence1.getProgramId())
      .programSequenceId(fake.program1_sequence1.getId())
      .programVoiceId(fake.program2_voice1.getId())
      .type(ProgramSequencePatternType.LOOP)
      .total(4)
      .name("BOOMS")
    );
    voiceTrack1a_0 = test.insert(new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(fake.program2_voice1.getProgramId())
      .programVoiceId(fake.program2_voice1.getId())
      .name("JAMS")
    );
    voiceTrack1a_0_event0 = test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programSequencePatternId(fake.program2_sequence1_pattern1.getId())
      .programId(fake.program2_sequence1_pattern1.getProgramId())
      .programVoiceTrackId(voiceTrack1a_0.getId())
      .position(0.0)
      .duration(1.0)
      .note("C")
      .velocity(1.0)
    );
    voiceTrack1a_0_event1 = test.insert(new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programSequencePatternId(fake.program2_sequence1_pattern1.getId())
      .programId(fake.program2_sequence1_pattern1.getProgramId())
      .programVoiceTrackId(voiceTrack1a_0.getId())
      .position(1.0)
      .duration(1.0)
      .note("C")
      .velocity(1.0)
    );

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(new Library()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("boat")
    );
    fake.program3 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.MACRO)
      .state(ProgramState.PUBLISHED)
      .name("helm")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
    );
    fake.program3_sequence1 = test.insert(new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .total(16)
      .name("Ants")
      .density(0.583)
      .key("D minor")
      .tempo(120.0)
    );
    test.insert(new ProgramSequenceBinding()
      .id(UUID.randomUUID())
      .programId(fake.program3_sequence1.getProgramId())
      .programSequenceId(fake.program3_sequence1.getId())
      .offset(0)
    );
    fake.program4 = test.insert(new Program()
      .id(UUID.randomUUID())
      .libraryId(fake.library2.getId())
      .type(ProgramType.DETAIL)
      .state(ProgramState.PUBLISHED)
      .name("sail")
      .key("C#")
      .tempo(120.0)
      .density(0.6)
    );

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramVoiceTrackDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .programVoiceId(fake.program2_voice1.getId())
      .name("Jams");

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program2_voice1.getId(), result.getProgramVoiceId());
    assertEquals("JAMS", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programVoiceTrack without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(fake.program3.getId())
      .programVoiceId(fake.program2_voice1.getId())
      .name("Jams");

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program2_voice1.getId(), result.getProgramVoiceId());
    assertEquals("JAMS", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, voiceTrack1a_0.getId());

    assertNotNull(result);
    assertEquals(voiceTrack1a_0.getId(), result.getId());
    assertEquals(fake.program2.getId(), result.getProgramId());
    assertEquals("JAMS", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, voiceTrack1a_0.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    Collection<ProgramVoiceTrack> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program2_voice1.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramVoiceTrack> resultIt = result.iterator();
    assertEquals("JAMS", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User, Artist");

    Collection<ProgramVoiceTrack> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program2_voice1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    failure.expect(DAOException.class);
    failure.expectMessage("Found Events in Track");

    testDAO.destroy(hubAccess, voiceTrack1a_0.getId());
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), voiceTrack1a_0_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), voiceTrack1a_0_event1.getId());

    testDAO.destroy(hubAccess, voiceTrack1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK)
      .where(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK.ID.eq(voiceTrack1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), voiceTrack1a_0_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), voiceTrack1a_0_event1.getId());

    testDAO.destroy(hubAccess, voiceTrack1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK)
      .where(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK.ID.eq(voiceTrack1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = new Account()
      .id(UUID.randomUUID())
    ;
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), voiceTrack1a_0_event0.getId());
    injector.getInstance(ProgramSequencePatternEventDAO.class).destroy(HubAccess.internal(), voiceTrack1a_0_event1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Track in Voice in Program you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, voiceTrack1a_0.getId());
  }

  /**
   [#175423724] Update ProgramVoiceTrack to belong to a different ProgramVoice
   */
  @Test
  public void update_moveToDifferentVoice() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.program2_voice2 = test.insert(new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(fake.program2.getId())
      .type(InstrumentType.PERCUSSIVE)
      .name("Cans")
    );
    voiceTrack1a_0 = voiceTrack1a_0.programVoiceId(fake.program2_voice2.getId());

    testDAO.update(hubAccess, voiceTrack1a_0.getId(), voiceTrack1a_0);

    var result = testDAO.readOne(hubAccess, voiceTrack1a_0.getId());
    assertEquals(fake.program2_voice2.getId(), result.getProgramVoiceId());
  }

}

