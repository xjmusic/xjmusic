// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubIntegrationTestModule;
import io.xj.service.hub.testing.HubIntegrationTestProvider;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
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
  private ProgramMemeDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.create("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.create("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(UserRole.create(fake.user2, UserRoleType.Admin));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(UserRole.create(fake.user3, UserRoleType.User));
    test.insert(AccountUser.create(fake.account1, fake.user3));

    // Library "palm tree" has program "ANTS" and program "ANTS"
    fake.library1 = test.insert(Library.create(fake.account1, "palm tree", Instant.now()));
    fake.program1 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0, 0.6));
    fake.programMeme1 = test.insert(ProgramMeme.create(fake.program1, "ANTS"));
    fake.program2 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Rhythm, ProgramState.Published, "ANTS", "C#", 120.0, 0.6));
    fake.programVoice3 = test.insert(ProgramVoice.create(fake.program2, InstrumentType.Percussive, "Drums"));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.create(fake.account1, "boat", Instant.now()));
    fake.program3 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fake.programMeme3 = test.insert(ProgramMeme.create(fake.program3, "ANTS"));
    fake.program4 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramMemeDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    ProgramMeme subject = ProgramMeme.create()
      .setProgramId(fake.program3.getId())
      .setName("cannons");

    ProgramMeme result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }

  /**
   [#156144567] Artist expects to of a Main-type programMeme without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    ProgramMeme inputData = ProgramMeme.create()
      .setProgramId(fake.program3.getId())
      .setName("cannons");

    ProgramMeme result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }


  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    ProgramMeme result = testDAO.readOne(hubAccess, fake.programMeme3.getId());

    assertNotNull(result);
    assertEquals(fake.programMeme3.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("ANTS", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.programMeme3.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramMeme> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramMeme> resultIt = result.iterator();
    assertEquals("ANTS", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramMeme> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_cannotChangeProgram() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    ProgramMeme subject = ProgramMeme.create()
      .setName("cannons")
      .setProgramId(UUID.randomUUID());

    testDAO.update(hubAccess, fake.programMeme3.getId(), subject);

    ProgramMeme result = testDAO.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    ProgramMeme subject = new ProgramMeme()
      .setId(fake.programMeme3.getId())
      .setProgramId(fake.program3.getId())
      .setName("cannons");

    testDAO.update(hubAccess, fake.programMeme3.getId(), subject);

    ProgramMeme result = testDAO.readOne(HubAccess.internal(), fake.programMeme3.getId());
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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Admin");
    ProgramMeme subject = new ProgramMeme()
      .setId(fake.programMeme3.getId())
      .setProgramId(fake.program3.getId())
      .setName("cannons");

    testDAO.update(hubAccess, fake.programMeme3.getId(), subject);

    ProgramMeme result = testDAO.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.programMeme35 = test.insert(ProgramMeme.create(fake.program2, "ANTS"));

    testDAO.destroy(hubAccess, fake.programMeme35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_MEME)
      .where(PROGRAM_MEME.ID.eq(fake.programMeme35.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.create();
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(DAOException.class);
    failure.expectMessage("Meme in Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, fake.programMeme3.getId());
  }

}

