// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Account;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.model.InstrumentType;
import io.xj.lib.core.model.Library;
import io.xj.lib.core.model.Program;
import io.xj.lib.core.model.ProgramSequence;
import io.xj.lib.core.model.ProgramSequenceBinding;
import io.xj.lib.core.model.ProgramSequenceBindingMeme;
import io.xj.lib.core.model.ProgramState;
import io.xj.lib.core.model.ProgramType;
import io.xj.lib.core.model.ProgramVoice;
import io.xj.lib.core.model.User;
import io.xj.lib.core.model.UserRole;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.IntegrationTestProvider;
import io.xj.lib.core.testing.InternalResources;
import io.xj.lib.core.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;

import static io.xj.lib.core.Tables.PROGRAM_SEQUENCE_BINDING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceBindingIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private WorkManager workManager;
  private ProgramSequenceBindingDAO testDAO;

  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fixture;

  private ProgramSequenceBinding sequenceBinding1a_0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme1;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    workManager = injector.getInstance(WorkManager.class);
    injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
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

    // Library "palm tree" has program "Ants" and program "Ants"
    fixture.library1 = test.insert(Library.create(fixture.account1, "palm tree", InternalResources.now()));
    fixture.program1 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Main, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fixture.programSequence1 = test.insert(ProgramSequence.create(fixture.program1, 4, "Ants", 0.583, "D minor", 120.0));
    sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.create(fixture.programSequence1, 0));
    sequenceBinding1a_0_meme0 = test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "chunk"));
    sequenceBinding1a_0_meme1 = test.insert(ProgramSequenceBindingMeme.create(sequenceBinding1a_0, "smooth"));
    fixture.program2 = test.insert(Program.create(fixture.user3, fixture.library1, ProgramType.Rhythm, ProgramState.Published, "Ants", "C#", 120.0, 0.6));
    fixture.programVoice3 = test.insert(ProgramVoice.create(fixture.program2, InstrumentType.Percussive, "Drums"));

    // Library "boat" has program "helm" and program "sail"
    fixture.library2 = test.insert(Library.create(fixture.account1, "boat", InternalResources.now()));
    fixture.program3 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0, 0.6));
    fixture.programSequence3 = test.insert(ProgramSequence.create(fixture.program3, 16, "Ants", 0.583, "D minor", 120.0));
    test.insert(ProgramSequenceBinding.create(fixture.programSequence3, 0));
    fixture.program4 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0, 0.6));

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramSequenceBindingDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "Artist");
    ProgramSequenceBinding subject = ProgramSequenceBinding.create()
      .setProgramId(fixture.program3.getId())
      .setProgramSequenceId(fixture.programSequence3.getId())
      .setOffset(4L);

    ProgramSequenceBinding result = testDAO.create(access, subject);

    assertNotNull(result);
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals(fixture.programSequence3.getId(), result.getProgramSequenceId());
    assertEquals(Long.valueOf(4), result.getOffset());
  }

  /**
   [#156144567] Artist expects to of a Main-type programSequenceBinding without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    Access access = Access.create(fixture.user2, ImmutableList.of(fixture.account1), "User,Artist");
    ProgramSequenceBinding inputData = ProgramSequenceBinding.create()
      .setProgramId(fixture.program3.getId())
      .setProgramSequenceId(fixture.programSequence3.getId())
      .setOffset(4L);

    ProgramSequenceBinding result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fixture.program3.getId(), result.getProgramId());
    assertEquals(fixture.programSequence3.getId(), result.getProgramSequenceId());
    assertEquals(Long.valueOf(4), result.getOffset());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "User, Artist");

    ProgramSequenceBinding result = testDAO.readOne(access, sequenceBinding1a_0.getId());

    assertNotNull(result);
    assertEquals(sequenceBinding1a_0.getId(), result.getId());
    assertEquals(fixture.program1.getId(), result.getProgramId());
    assertEquals(Long.valueOf(0), result.getOffset());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, sequenceBinding1a_0.getId());
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Admin");

    Collection<ProgramSequenceBinding> result = testDAO.readMany(access, ImmutableList.of(fixture.programSequence1.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequenceBinding> resultIt = result.iterator();
    assertEquals(Long.valueOf(0), resultIt.next().getOffset());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User, Artist");

    Collection<ProgramSequenceBinding> result = testDAO.readMany(access, ImmutableList.of(fixture.programSequence3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() throws Exception {
    Access access = Access.create("Admin");

    failure.expect(CoreException.class);
    failure.expectMessage("Found Meme on Sequence Binding");

    testDAO.destroy(access, sequenceBinding1a_0.getId());
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    Access access = Access.create("Admin");
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(Access.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(Access.internal(), sequenceBinding1a_0_meme1.getId());

    testDAO.destroy(access, sequenceBinding1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_asArtist() throws Exception {
    Access access = Access.create(ImmutableList.of(fixture.account1), "Artist");
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(Access.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(Access.internal(), sequenceBinding1a_0_meme1.getId());

    testDAO.destroy(access, sequenceBinding1a_0.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(PROGRAM_SEQUENCE_BINDING)
      .where(PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fixture.account2 = Account.create();
    Access access = Access.create(ImmutableList.of(fixture.account2), "Artist");
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(Access.internal(), sequenceBinding1a_0_meme0.getId());
    injector.getInstance(ProgramSequenceBindingMemeDAO.class).destroy(Access.internal(), sequenceBinding1a_0_meme1.getId());

    failure.expect(CoreException.class);
    failure.expectMessage("Sequence Binding in Program in Account you have access to does not exist");

    testDAO.destroy(access, sequenceBinding1a_0.getId());
  }

}

