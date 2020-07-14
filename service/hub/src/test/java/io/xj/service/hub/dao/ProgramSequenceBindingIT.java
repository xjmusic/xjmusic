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

import static io.xj.service.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceBindingIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequenceBindingDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceBinding sequenceBinding1a_0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme1;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
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

    // Library "palm tree" has program "Ants" and program "Ants"
    fake.library1 = test.insert(Library.create(fake.account1, "palm tree", Instant.now()));
    fake.program1 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fake.programSequence1 = test.insert(ProgramSequence.create(fake.program1, 4, "Ants", 0.583, "D minor", 120.0));
    sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.create(fake.programSequence1, 0));
    sequenceBinding1a_0_meme0 = test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "chunk"));
    sequenceBinding1a_0_meme1 = test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(Program.create(fake.user3, fake.library1, ProgramType.Rhythm, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fake.programVoice3 = test.insert(ProgramVoice.create(fake.program2, InstrumentType.Percussive, "Drums"));

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.create(fake.account1, "boat", Instant.now()));
    fake.program3 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fake.programSequence3 = test.insert(ProgramSequence.create(fake.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fake.programSequence3, 0));
    fake.program4 = test.insert(Program.create(fake.user3, fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequenceBindingDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    ProgramSequenceBinding subject = ProgramSequenceBinding.create()
      .setProgramId(fake.program3.getId())
      .setProgramSequenceId(fake.programSequence3.getId())
      .setOffset(4L);

    ProgramSequenceBinding result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.programSequence3.getId(), result.getProgramSequenceId());
    assertEquals(Long.valueOf(4), result.getOffset());
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequenceBinding without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    ProgramSequenceBinding inputData = ProgramSequenceBinding.create()
      .setProgramId(fake.program3.getId())
      .setProgramSequenceId(fake.programSequence3.getId())
      .setOffset(4L);

    ProgramSequenceBinding result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.programSequence3.getId(), result.getProgramSequenceId());
    assertEquals(Long.valueOf(4), result.getOffset());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    ProgramSequenceBinding result = testDAO.readOne(hubAccess, sequenceBinding1a_0.getId());

    assertNotNull(result);
    assertEquals(sequenceBinding1a_0.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(Long.valueOf(0), result.getOffset());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, sequenceBinding1a_0.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceBinding> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.programSequence1.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequenceBinding> resultIt = result.iterator();
    assertEquals(Long.valueOf(0), resultIt.next().getOffset());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramSequenceBinding> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.programSequence3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    failure.expect(DAOException.class);
    failure.expectMessage("Found Meme on Sequence Binding");

    testDAO.destroy(hubAccess, sequenceBinding1a_0.getId());
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    testDAO.destroy(hubAccess, sequenceBinding1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    testDAO.destroy(hubAccess, sequenceBinding1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.create();
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    failure.expect(DAOException.class);
    failure.expectMessage("Sequence Binding in Program in Account you have hubAccess to does not exist");

    testDAO.destroy(hubAccess, sequenceBinding1a_0.getId());
  }

}

