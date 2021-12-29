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
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceBindingMemeDAOTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequenceBindingMemeDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceBinding sequenceBinding1a_0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme0;

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
    sequenceBinding1a_0 = test.insert(buildProgramSequenceBinding(fake.program1_sequence1, 0));
    sequenceBinding1a_0_meme0 = test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "chunk"));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor", 120.0f));
    fake.program3_binding1 = test.insert(buildProgramSequenceBinding(fake.program3_sequence1, 0));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequenceBindingMemeDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramSequenceBindingMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setProgramId(fake.program3_binding1.getProgramId());
    subject.setProgramSequenceBindingId(fake.program3_binding1.getId());
    subject.setName("Blue");

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_binding1.getId(), result.getProgramSequenceBindingId());
    assertEquals("BLUE", result.getName());
  }

  /**
   [#177587964] Artist can use numerals in meme name
   */
  @Test
  public void create_numerals() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramSequenceBindingMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setProgramId(fake.program3_binding1.getProgramId());
    subject.setProgramSequenceBindingId(fake.program3_binding1.getId());
    subject.setName("Blue5");

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_binding1.getId(), result.getProgramSequenceBindingId());
    assertEquals("BLUE5", result.getName());
  }

  /**
   [#176474073] Artist can add !MEME values into Programs
   */
  @Test
  public void create_notMeme() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramSequenceBindingMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setProgramId(fake.program3_binding1.getProgramId());
    subject.setProgramSequenceBindingId(fake.program3_binding1.getId());
    subject.setName("!Blue");

    var result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_binding1.getId(), result.getProgramSequenceBindingId());
    assertEquals("!BLUE", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequenceBindingMeme without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var inputData = new ProgramSequenceBindingMeme();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setProgramId(fake.program3_binding1.getProgramId());
    inputData.setProgramSequenceBindingId(fake.program3_binding1.getId());
    inputData.setName("Blue");

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_binding1.getId(), result.getProgramSequenceBindingId());
    assertEquals("BLUE", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, sequenceBinding1a_0_meme0.getId());

    assertNotNull(result);
    assertEquals(sequenceBinding1a_0_meme0.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(sequenceBinding1a_0.getId(), result.getProgramSequenceBindingId());
    assertEquals("chunk", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, sequenceBinding1a_0_meme0.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceBindingMeme> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program1.getId()));

    assertEquals(2L, result.size());
    Iterator<ProgramSequenceBindingMeme> resultIt = result.iterator();
    assertEquals("chunk", resultIt.next().getName());
    assertEquals("smooth", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramSequenceBindingMeme> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(DAOException.class);
    failure.expectMessage("Meme belongs to Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, sequenceBinding1a_0_meme0.getId());
  }

}

