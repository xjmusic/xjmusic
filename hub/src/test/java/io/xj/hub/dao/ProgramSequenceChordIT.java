// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
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
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChord;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceChordVoicing;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceChordIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequenceChordDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceChord sequenceChord1a_0;

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
    fake.user2 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "palm tree" has a program "Ants" and program "Ants"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor", 120.0f));
    sequenceChord1a_0 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C minor"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Rhythm, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program2_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor", 120.0f));
    fake.program3_chord1 = test.insert(buildProgramSequenceChord(fake.program3_sequence1, 0.0f, "G7 flat 6"));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequenceChordDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = new ProgramSequenceChord();
    subject.setId(UUID.randomUUID());
    subject.setProgramSequenceId(fake.program3_sequence1.getId());
    subject.setProgramId(fake.program3.getId());
    subject.setName("C Major");
    subject.setPosition(2.0);

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(2.0, result.getPosition(), 0.01);
    assertEquals("C Major", result.getName());
  }

  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = new ProgramSequenceChord();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramSequenceId(fake.program3_sequence1.getId());
    inputData.setProgramId(fake.program3.getId());
    inputData.setName("C Major");
    inputData.setPosition(2.0);

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(2.0, result.getPosition(), 0.01);
    assertEquals("C Major", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, sequenceChord1a_0.getId());

    assertNotNull(result);
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(0.0, result.getPosition(), 0.01);
    assertEquals("C minor", result.getName());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceChord> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    testDAO.destroy(hubAccess, sequenceChord1a_0.getId());

    assertEquals(0L, testDAO.readMany(hubAccess, ImmutableList.of(fake.program1.getId())).size());
  }

  /**
   [#175703981] Artist editing main program deletes voicing along with chord
   */
  @Test
  public void destroy_afterHasVoicing() throws Exception {
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, InstrumentType.Pad, "C5, Eb5, G5"));

    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    testDAO.destroy(hubAccess, sequenceChord1a_0.getId());

    assertEquals(0L, testDAO.readMany(hubAccess, ImmutableList.of(fake.program1.getId())).size());
  }

}

