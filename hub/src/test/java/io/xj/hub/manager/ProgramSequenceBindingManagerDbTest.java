// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.lib.app.AppEnvironment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceBindingManagerDbTest {
  private ProgramSequenceBindingManager testManager;

  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceBinding sequenceBinding1a_0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme0;
  private ProgramSequenceBindingMeme sequenceBinding1a_0_meme1;

  @Before
  public void setUp() throws Exception {
    var env = AppEnvironment.getDefault();

    test = HubIntegrationTestFactory.build(env);
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
    testManager = new ProgramSequenceBindingManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
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
   * Artist expects to of a Main-type programSequenceBinding without crashing the entire platform https://www.pivotaltracker.com/story/show/156144567
   * NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
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
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");

    var result = testManager.readOne(access, sequenceBinding1a_0.getId());

    assertNotNull(result);
    assertEquals(sequenceBinding1a_0.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(Integer.valueOf(0), result.getOffset());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User, Artist");

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(access, sequenceBinding1a_0.getId()));
    assertTrue(e.getMessage().contains("does not exist"));
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceBinding> result = testManager.readMany(access, ImmutableList.of(fake.program1.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramSequenceBinding> resultIt = result.iterator();
    assertEquals(Integer.valueOf(0), resultIt.next().getOffset());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramSequenceBinding> result = testManager.readMany(access, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfHasChildEntity() {
    HubAccess access = HubAccess.create("Admin");


    var e = assertThrows(ManagerException.class, () -> testManager.destroy(access, sequenceBinding1a_0.getId()));
    assertTrue(e.getMessage().contains("Found Meme on Sequence Binding"));
  }

  @Test
  public void destroy_okWithNoChildEntities() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    new ProgramSequenceBindingMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    new ProgramSequenceBindingMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    testManager.destroy(access, sequenceBinding1a_0.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0),
        selectCount
          .from(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING)
          .where(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
          .fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Artist");
    new ProgramSequenceBindingMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    new ProgramSequenceBindingMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());

    testManager.destroy(access, sequenceBinding1a_0.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0),
        selectCount
          .from(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING)
          .where(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING.ID.eq(sequenceBinding1a_0.getId()))
          .fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account2), "Artist");
    new ProgramSequenceBindingMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), sequenceBinding1a_0_meme0.getId());
    new ProgramSequenceBindingMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider()).destroy(HubAccess.internal(), sequenceBinding1a_0_meme1.getId());


    var e = assertThrows(ManagerException.class, () -> testManager.destroy(access, sequenceBinding1a_0.getId()));
    assertTrue(e.getMessage().contains("Sequence Binding in Program in Account you have access to does not exist"));
  }

}

