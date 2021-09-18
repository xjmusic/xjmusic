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
import io.xj.hub.tables.pojos.ProgramAuthorship;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramAuthorship;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramAuthorshipIT {
  private ProgramAuthorshipDAO testDAO;

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
    fake.user2 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif", "User,Artist"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "palm tree" has a program "ANTS" and program "ANTS"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.programAuthorship1 = test.insert(buildProgramAuthorship(fake.program1, fake.user2, "Writing stuff", 0.7f));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Rhythm, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program2_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.programAuthorship3 = test.insert(buildProgramAuthorship(fake.program3, fake.user2, "Writing stuff", 0.4f));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramAuthorshipDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var input = new ProgramAuthorship();
    input.setId(UUID.randomUUID());
    input.setProgramId(fake.program3.getId());
    input.setUserId(fake.user2.getId());
    input.setDescription("writing more stuff");
    input.setHours(0.85f);

    var result = testDAO.create(hubAccess, input);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.user2.getId(), result.getUserId());
    assertEquals("writing more stuff", result.getDescription());
    assertEquals(0.85f, result.getHours(), 0.01);
  }

  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user3, ImmutableList.of(fake.account1));
    var input = new ProgramAuthorship();
    input.setId(UUID.randomUUID());
    input.setProgramId(fake.program3.getId());
    input.setUserId(fake.user2.getId());
    input.setDescription("writing more stuff");
    input.setHours(0.85f);

    var result = testDAO.create(
      hubAccess, input);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.user2.getId(), result.getUserId());
    assertEquals("writing more stuff", result.getDescription());
    assertEquals(0.85f, result.getHours(), 0.01);
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, fake.programAuthorship3.getId());

    assertNotNull(result);
    assertEquals(fake.programAuthorship3.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.user2.getId(), result.getUserId());
    assertEquals("Writing stuff", result.getDescription());
    assertEquals(0.4f, result.getHours(), 0.01);
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramAuthorship> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void update_cannotBeChanged() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    var input = new ProgramAuthorship();
    input.setId(UUID.randomUUID());
    input.setDescription("writing more stuff");
    input.setProgramId(UUID.randomUUID());

    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, fake.programAuthorship3.getId(), input));
    assertEquals("Cannot update a program authorship!", e.getMessage());
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.programAuthorship35 = test.insert(buildProgramAuthorship(fake.program2, fake.user2, "writing extra stuff", 0.2f));

    testDAO.destroy(hubAccess, fake.programAuthorship35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramAuthorship.PROGRAM_AUTHORSHIP)
      .where(io.xj.hub.tables.ProgramAuthorship.PROGRAM_AUTHORSHIP.ID.eq(fake.programAuthorship35.getId()))
      .fetchOne(0, int.class));
  }

}

