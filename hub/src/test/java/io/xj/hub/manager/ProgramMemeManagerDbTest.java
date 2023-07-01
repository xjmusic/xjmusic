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
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class ProgramMemeManagerDbTest {
  private ProgramMemeManager testManager;
  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @Autowired
  HubIntegrationTestFactory integrationTestFactory;

  @BeforeEach
  public void setUp() throws Exception {
    test = integrationTestFactory.build();
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
    testManager = new ProgramMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
  }

  @AfterAll
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");

    var result = testManager.create(
      access, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }

  /**
   * Artist can use numerals in meme name https://www.pivotaltracker.com/story/show/177587964
   */
  @Test
  public void create_numerals() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setName("3note");

    var result = testManager.create(
      access, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("3NOTE", result.getName());
  }

  /**
   * Artist can add !MEME values into Programs https://www.pivotaltracker.com/story/show/176474073
   */
  @Test
  public void create_notMeme() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setProgramId(fake.program3.getId());
    subject.setName("!busy");

    var result = testManager.create(
      access, subject);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("!BUSY", result.getName());
  }

  /**
   * Artist expects to of a Main-type programMeme without crashing the entire platform https://www.pivotaltracker.com/story/show/156144567
   * NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var inputData = new ProgramMeme();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setName("cannons");

    var result = testManager.create(
      access, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("CANNONS", result.getName());
  }


  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");

    var result = testManager.readOne(access, fake.programMeme3.getId());

    assertNotNull(result);
    assertEquals(fake.programMeme3.getId(), result.getId());
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals("ANTS", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User, Artist");

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(access, fake.programMeme3.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramMeme> result = testManager.readMany(access, ImmutableList.of(fake.program3.getId()));

    assertEquals(1L, result.size());
    Iterator<ProgramMeme> resultIt = result.iterator();
    assertEquals("ANTS", resultIt.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramMeme> result = testManager.readMany(access, ImmutableList.of(fake.program3.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_cannotChangeProgram() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User, Artist");
    var subject = new ProgramMeme();
    subject.setId(UUID.randomUUID());
    subject.setName("cannons");
    subject.setProgramId(UUID.randomUUID());

    testManager.update(access, fake.programMeme3.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(fake.programMeme3.getId());
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");

    testManager.update(access, fake.programMeme3.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
    assertEquals("CANNONS", result.getName());
    assertEquals(fake.program3.getId(), result.getProgramId());
  }

  /**
   * Artist expects owner of ProgramMeme or Instrument to always remain the same as when it was ofd, even after being updated by another user. https://www.pivotaltracker.com/story/show/156030760
   * DEPRECATED, future will be replaced by Instruments and Programs have author history https://www.pivotaltracker.com/story/show/166724453
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a programMeme originally belonging to Jenny
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var subject = new ProgramMeme();
    subject.setId(fake.programMeme3.getId());
    subject.setProgramId(fake.program3.getId());
    subject.setName("cannons");

    testManager.update(access, fake.programMeme3.getId(), subject);

    var result = testManager.readOne(HubAccess.internal(), fake.programMeme3.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Artist");
    fake.programMeme35 = test.insert(buildProgramMeme(fake.program2, "ANTS"));

    testManager.destroy(access, fake.programMeme35.getId());

    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(0),
        selectCount
          .from(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME)
          .where(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME.ID.eq(fake.programMeme35.getId()))
          .fetchOne(0, int.class));
    }
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account2), "Artist");

    var e = assertThrows(ManagerException.class, () -> testManager.destroy(access, fake.programMeme3.getId()));

    assertEquals("Meme in Program in Account you have access to does not exist", e.getMessage());
  }

}
