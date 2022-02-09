// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

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
import io.xj.hub.tables.pojos.ProgramMeme;
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
public class ProgramMemeManagerImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramMemeManager testManager;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
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

    // Library "palm tree" has a program "ANTS" and program "ANTS"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.programMeme1 = test.insert(buildProgramMeme(fake.program1, "ANTS"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.programMeme3 = test.insert(buildProgramMeme(fake.program3, "ANTS"));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testManager = injector.getInstance(ProgramMemeManager.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");

    var result = testManager.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }

  /**
   [#177587964] Artist can use numerals in meme name
   */
  @Test
  public void create_numerals() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setName("3note");

    var result = testManager.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("3NOTE", result.getName());
  }

  /**
   [#176474073] Artist can add !MEME values into Programs
   */
  @Test
  public void create_notMeme() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setName("!busy");

    var result = testManager.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("!BUSY", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programMeme without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var inputData = new ProgramMeme();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setName("cannons");

    var result = testManager.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }


  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testManager.readOne(hubAccess, fake.programMeme3.getId());

    assertNotNull(result);
    assertEquals(fake.programMeme3.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("ANTS", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");
    failure.expect(ManagerException.class);
    failure.expectMessage("does not exist");

    testManager.readOne(hubAccess, fake.programMeme3.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramMeme> result = testManager.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramMeme> resultIt = result.iterator();
    assertEquals("ANTS", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramMeme> result = testManager.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_cannotChangeProgram() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setName("cannons");
    subject.setProgramId(UUID.randomUUID());

    testManager.update(hubAccess, fake.programMeme3.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(fake.programMeme3.getId());
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");

    testManager.update(hubAccess, fake.programMeme3.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  /**
   [#156030760] Artist expects owner of ProgramMeme or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a programMeme originally belonging to Jenny
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(fake.programMeme3.getId());
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");

    testManager.update(hubAccess, fake.programMeme3.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.programMeme35 = test.insert(buildProgramMeme(fake.program2, "ANTS"));

    testManager.destroy(hubAccess, fake.programMeme35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME)
      .where(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME.ID.eq(fake.programMeme35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(ManagerException.class);
    failure.expectMessage("Meme in Program in Account you have hubAccess to does not exist");

    testManager.destroy(hubAccess, fake.programMeme3.getId());
  }

}

