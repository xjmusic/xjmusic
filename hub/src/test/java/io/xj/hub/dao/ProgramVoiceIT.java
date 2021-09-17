// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramSequencePatternType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.ProgramVoice;
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
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequencePattern;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoiceTrack;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.tables.ProgramVoice.PROGRAM_VOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramVoiceIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramVoiceDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

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
    fake.account1 = test.insert(buildAccount("bananas"));
// John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "palm tree" has a program "Ants" and program "Ants"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor", 120.0f));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Rhythm, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program2_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));
    fake.program2_sequence1_pattern1 = test.insert(buildProgramSequencePattern(fake.program1_sequence1, fake.program2_voice1, ProgramSequencePatternType.Loop, 4, "BOOMS"));
    fake.program2_voice1_track0 = test.insert(buildProgramVoiceTrack(fake.program2_voice1, "KICK"));
    fake.program2_voice1_track1 = test.insert(buildProgramVoiceTrack(fake.program2_voice1, "SNARE"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor", 120.0f));
    test.insert(buildProgramSequenceBinding(fake.program3_sequence1, 0));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramVoiceDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = new ProgramVoice();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setType(InstrumentType.Pad);
    subject.setName("Jams");

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Jams", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programVoice without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = new ProgramVoice();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setType(InstrumentType.Pad);
    inputData.setName("Jams");

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("Jams", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, fake.program2_voice1.getId());

    assertNotNull(result);
    assertEquals(fake.program2_voice1.getId(), result.getId());
    assertEquals(fake.program2.getId(), result.getProgramId());
    assertEquals("Drums", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.program2_voice1.getId());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    Collection<ProgramVoice> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program2.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramVoice> resultIt = result.iterator();
    assertEquals("Drums", resultIt.next().getName());
  }

  @Test
  public void readMany_seesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramVoice> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program2.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    failure.expect(DAOException.class);
    failure.expectMessage("Found Pattern in Voice");

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    injector.getInstance(ProgramSequencePatternDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.ID.eq(fake.program2_voice1.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    injector.getInstance(ProgramSequencePatternDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_VOICE)
      .where(PROGRAM_VOICE.ID.eq(fake.program2_voice1.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");
    injector.getInstance(ProgramSequencePatternDAO.class).destroy(HubAccess.internal(), fake.program2_sequence1_pattern1.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track0.getId());
    injector.getInstance(ProgramVoiceTrackDAO.class).destroy(HubAccess.internal(), fake.program2_voice1_track1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Voice in Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, fake.program2_voice1.getId());
  }

}

