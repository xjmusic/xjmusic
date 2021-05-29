// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Instrument;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramVoice;
import io.xj.User;
import io.xj.UserRole;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.testing.HubIntegrationTestModule;
import io.xj.hub.testing.HubIntegrationTestProvider;
import io.xj.hub.testing.HubTestConfiguration;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
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
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bananas")
      .build());

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("john")
      .setEmail("john@email.com")
      .setAvatarUrl("http://pictures.com/john.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.Admin)
      .build());

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("jenny")
      .setEmail("jenny@email.com")
      .setAvatarUrl("http://pictures.com/jenny.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user3.getId())
      .build());

    // Library "palm tree" has program "ANTS" and program "ANTS"
    fake.library1 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("palm tree")
      .build());
    fake.program1 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("ANTS")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    fake.programMeme1 = test.insert(ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setName("ANTS")
      .build());
    fake.program2 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Program.Type.Rhythm)
      .setState(Program.State.Published)
      .setName("ANTS")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    fake.program2_voice1 = test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program2.getId())
      .setType(Instrument.Type.Percussive)
      .setName("Drums")
      .build());

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("boat")
      .build());
    fake.program3 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Macro)
      .setState(Program.State.Published)
      .setName("helm")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    fake.programMeme3 = test.insert(ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setName("ANTS")
      .build());
    fake.program4 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Detail)
      .setState(Program.State.Published)
      .setName("sail")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());

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
    var subject = ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setName("cannons")
      .build();

    var result = testDAO.create(
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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setName("3note")
      .build();

    var result = testDAO.create(
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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setName("!busy")
      .build();

    var result = testDAO.create(
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
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    var inputData = ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setName("cannons")
      .build();

    var result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }


  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, fake.programMeme3.getId());

    assertNotNull(result);
    assertEquals(fake.programMeme3.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("ANTS", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "User, Artist");
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "User, Artist");

    Collection<ProgramMeme> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_cannotChangeProgram() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    var subject = ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("cannons")
      .setProgramId(UUID.randomUUID().toString())
      .build();

    testDAO.update(hubAccess, fake.programMeme3.getId(), subject);

    var result = testDAO.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    var subject = ProgramMeme.newBuilder()
      .setId(fake.programMeme3.getId())
      .setProgramId(fake.program3.getId())
      .setName("cannons")
      .build();

    testDAO.update(hubAccess, fake.programMeme3.getId(), subject);

    var result = testDAO.readOne(HubAccess.internal(), fake.programMeme3.getId());
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
    var subject = ProgramMeme.newBuilder()
      .setId(fake.programMeme3.getId())
      .setProgramId(fake.program3.getId())
      .setName("cannons")
      .build();

    testDAO.update(hubAccess, fake.programMeme3.getId(), subject);

    var result = testDAO.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.programMeme35 = test.insert(ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program2.getId())
      .setName("ANTS")
      .build());

    testDAO.destroy(hubAccess, fake.programMeme35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME)
      .where(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME.ID.eq(UUID.fromString(fake.programMeme35.getId())))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(DAOException.class);
    failure.expectMessage("Meme in Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, fake.programMeme3.getId());
  }

}

