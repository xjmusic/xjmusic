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
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.ProgramVoice;
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

import static io.xj.service.hub.tables.ProgramMeme.PROGRAM_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramMemeIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private WorkManager workManager;
  private ProgramMemeDAO testDAO;

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

    // Library "palm tree" has program "ANTS" and program "ANTS"
    fixture.library1 = test.insert(Library.create(fixture.account1, "palm tree", InternalResources.now()));
    fixture.program1 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0, 0.6));
    fixture.programMeme1 = test.insert(ProgramMeme.create(fixture.program1, "ANTS"));
    fixture.program2 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Rhythm, ProgramState.Published, "ANTS", "C#", 120.0, 0.6));
    fixture.programVoice3 = test.insert(ProgramVoice.create(fixture.program2, InstrumentType.Percussive, "Drums"));

    // Library "boat" has program "helm" and program "sail"
    fixture.library2 = test.insert(Library.create(fixture.account1, "boat", InternalResources.now()));
    fixture.program3 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fixture.programMeme3 = test.insert(ProgramMeme.create(fixture.program3, "ANTS"));
    fixture.program4 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramMemeDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    ProgramMeme subject = ProgramMeme.create()
      .setProgramId(fixture.program3.getId())
      .setName("cannons");

    ProgramMeme result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programMeme without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "User,Artist");
    ProgramMeme inputData = ProgramMeme.create()
      .setProgramId(fixture.program3.getId())
      .setName("cannons");

    ProgramMeme result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }


  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User, Artist");

    ProgramMeme result = testDAO.readOne(access, fixture.programMeme3.getId());

    assertNotNull(result);
    assertEquals(fixture.programMeme3.getId(), result.getId());
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals("ANTS", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");
    failure.expect(HubException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fixture.programMeme3.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Admin");

    Collection<ProgramMeme> result = testDAO.readMany(access, ImmutableList.of(fixture.program3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramMeme> resultIt = result.iterator();
    assertEquals("ANTS", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramMeme> result = testDAO.readMany(access, ImmutableList.of(fixture.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_cannotChangeProgram() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User, Artist");
    ProgramMeme subject = ProgramMeme.create()
      .setName("cannons")
      .setProgramId(UUID.randomUUID());

    testDAO.update(access, fixture.programMeme3.getId(), subject);

    ProgramMeme result = testDAO.readOne(Access.internal(), fixture.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fixture.program3.getId(), result.getProgramId());
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    ProgramMeme subject = new ProgramMeme()
      .setId(fixture.programMeme3.getId())
      .setProgramId(fixture.program3.getId())
      .setName("cannons");

    testDAO.update(access, fixture.programMeme3.getId(), subject);

    ProgramMeme result = testDAO.readOne(Access.internal(), fixture.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fixture.program3.getId(), result.getProgramId());
  }

  /**
   [#156030760] Artist expects owner of ProgramMeme or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a programMeme originally belonging to Jenny
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Admin");
    ProgramMeme subject = new ProgramMeme()
      .setId(fixture.programMeme3.getId())
      .setProgramId(fixture.program3.getId())
      .setName("cannons");

    testDAO.update(access, fixture.programMeme3.getId(), subject);

    ProgramMeme result = testDAO.readOne(Access.internal(), fixture.programMeme3.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Artist");
    fixture.programMeme35 = test.insert(ProgramMeme.create(fixture.program2, "ANTS"));

    testDAO.destroy(access, fixture.programMeme35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_MEME)
      .where(PROGRAM_MEME.ID.eq(fixture.programMeme35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fixture.account2 = Account.create();
    Access access = Access.create(ImmutableList.of(fixture.account2), "Artist");

    failure.expect(HubException.class);
    failure.expectMessage("Meme in Program in Account you have access to does not exist");

    testDAO.destroy(access, fixture.programMeme3.getId());
  }

}

