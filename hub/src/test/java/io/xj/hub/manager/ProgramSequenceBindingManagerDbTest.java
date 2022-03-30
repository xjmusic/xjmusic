// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
public class ProgramSequenceBindingManagerDbTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequenceBindingManager testManager;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceBinding sequenceBinding1a_0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme1;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
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
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));
    sequenceBinding1a_0 = test.insert(buildProgramSequenceBinding(fake.program1_sequence1, 0));
    sequenceBinding1a_0_meme0 = test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "chunk"));
    sequenceBinding1a_0_meme1 = test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor"));
    test.insert(buildProgramSequenceBinding(fake.program3_sequence1, 0));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test subject
    testManager = injector.getInstance(ProgramSequenceBindingManager.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var subject = new ProgramSequenceBinding();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setProgramSequenceId(fake.program3_sequence1.getId());
    subject.setOffset(4);

    var result = testManager.create(
      access, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_sequence1.getId(), result.getProgramSequenceId());
    assertEquals(Integer.valueOf(4), result.getOffset());
  }

  /**
   https://www.pivotaltracker.com/story/show/156144567 Artist expects to of a Main-type programSequenceBinding without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var inputData = new ProgramSequenceBinding();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setProgramSequenceId(fake.program3_sequence1.getId());
    inputData.setOffset(4);

    var result = testManager.create(
      access, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_sequence1.getId(), result.getProgramSequenceId());
    assertEquals(Integer.valueOf(4), result.getOffset());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testManager.readOne(access, sequenceBinding1a_0.getId());

    assertNotNull(result);
    assertEquals(sequenceBinding1a_0.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(Integer.valueOf(0), result.getOffset());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");
    failure.expect(ManagerException.class);
    failure.expectMessage("does not exist");

    testManager.readOne(access, sequenceBinding1a_0.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceBinding> result = testManager.readMany(access, ImmutableList.of(fake.program1.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequenceBinding> resultIt = result.iterator();
    assertEquals(Integer.valueOf(0), resultIt.next().getOffset());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramSequenceBinding> result = testManager.readMany(access, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    failure.expect(ManagerException.class);
    failure.expectMessage("Found Meme on Sequence Binding");

    testManager.destroy(access, sequenceBinding1a_0.getId());
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    injector.getInstance(ProgramSequenceBindingMemeManager.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeManager.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    testManager.destroy(access, sequenceBinding1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING)
      .where(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    injector.getInstance(ProgramSequenceBindingMemeManager.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeManager.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    testManager.destroy(access, sequenceBinding1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING)
      .where(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account2), "Artist");
    injector.getInstance(ProgramSequenceBindingMemeManager.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeManager.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    failure.expect(ManagerException.class);
    failure.expectMessage("Sequence Binding in Program in Account you have access to does not exist");

    testManager.destroy(access, sequenceBinding1a_0.getId());
  }

}

